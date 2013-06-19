package org.bimserver.database.actions;

/******************************************************************************
 * Copyright (C) 2009-2013  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bimserver.BimServer;
import org.bimserver.GeometryGenerator;
import org.bimserver.GeometryGenerator.GeometryCache;
import org.bimserver.RenderException;
import org.bimserver.SummaryMap;
import org.bimserver.changes.Change;
import org.bimserver.changes.CreateObjectChange;
import org.bimserver.changes.RemoveObjectChange;
import org.bimserver.database.BimserverDatabaseException;
import org.bimserver.database.BimserverLockConflictException;
import org.bimserver.database.DatabaseSession;
import org.bimserver.database.PostCommitAction;
import org.bimserver.database.Query;
import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifc.IfcModel;
import org.bimserver.interfaces.SConverter;
import org.bimserver.mail.MailSystem;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.log.LogFactory;
import org.bimserver.models.log.NewRevisionAdded;
import org.bimserver.models.store.ConcreteRevision;
import org.bimserver.models.store.Project;
import org.bimserver.models.store.Revision;
import org.bimserver.models.store.User;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.webservices.LongTransaction;
import org.bimserver.webservices.NoTransactionException;
import org.bimserver.webservices.authorization.Authorization;
import org.slf4j.LoggerFactory;

public class CommitTransactionDatabaseAction extends GenericCheckinDatabaseAction {

	private final String comment;
	private final LongTransaction longTransaction;
	private Revision revision;
	private Authorization authorization;
	private BimServer bimServer;
	private final GeometryCache geometryCache = new GeometryCache();

	public CommitTransactionDatabaseAction(BimServer bimServer, DatabaseSession databaseSession, AccessMethod accessMethod, Authorization authorization, LongTransaction longTransaction, String comment) {
		super(databaseSession, accessMethod, null);
		this.bimServer = bimServer;
		this.authorization = authorization;
		this.longTransaction = longTransaction;
		this.comment = comment;
	}

	@Override
	public ConcreteRevision execute() throws UserException, BimserverLockConflictException, BimserverDatabaseException {
		Project project = getProjectByPoid(longTransaction.getPoid());
		User user = getUserByUoid(authorization.getUoid());
		if (project == null) {
			throw new UserException("Project with poid " + longTransaction.getPoid() + " not found");
		}
		if (!authorization.hasRightsOnProjectOrSuperProjects(user, project)) {
			throw new UserException("User has no rights to checkin models to this project");
		}
		if (!MailSystem.isValidEmailAddress(user.getUsername())) {
			throw new UserException("Users must have a valid e-mail address to checkin");
		}
		long size = 0;
		if (project.getLastRevision() != null) {
			for (ConcreteRevision concreteRevision : project.getLastRevision().getConcreteRevisions()) {
				size += concreteRevision.getSize();
			}
		}
		for (Change change : longTransaction.getChanges()) {
			if (change instanceof CreateObjectChange) {
				size++;
			} else if (change instanceof RemoveObjectChange) {
				size--;
			}
		}
		Revision oldLastRevision = project.getLastRevision();
		CreateRevisionResult result = createNewConcreteRevision(getDatabaseSession(), size, project, user, comment.trim());
		ConcreteRevision concreteRevision = result.getConcreteRevision();
		revision = concreteRevision.getRevisions().get(0);
		project.setLastRevision(revision);
		final NewRevisionAdded newRevisionAdded = LogFactory.eINSTANCE.createNewRevisionAdded();
		newRevisionAdded.setDate(new Date());
		newRevisionAdded.setExecutor(user);
		newRevisionAdded.setRevision(concreteRevision.getRevisions().get(0));
		newRevisionAdded.setProject(project);
		newRevisionAdded.setAccessMethod(getAccessMethod());

		IfcModelInterface ifcModel = new IfcModel();
		if (oldLastRevision != null) {
			getDatabaseSession().getMap(ifcModel, new Query(project.getId(), oldLastRevision.getId()));
		}
		
		getDatabaseSession().addPostCommitAction(new PostCommitAction() {
			@Override
			public void execute() throws UserException {
				bimServer.getNotificationsManager().notify(new SConverter().convertToSObject(newRevisionAdded));
				try {
					bimServer.getLongTransactionManager().remove(longTransaction.getTid());
				} catch (NoTransactionException e) {
					e.printStackTrace();
				}
			}
		});

		SummaryMap summaryMap = null;
		if (oldLastRevision != null && oldLastRevision.getConcreteRevisions().size() == 1 && oldLastRevision.getConcreteRevisions().get(0).getSummary() != null) {
			summaryMap = new SummaryMap(oldLastRevision.getConcreteRevisions().get(0).getSummary());
		} else {
			summaryMap = new SummaryMap();
		}

		// First create all new objects
		Map<Long, IdEObject> created = new HashMap<Long, IdEObject>();
		Map<Long, IdEObject> deleted = new HashMap<Long, IdEObject>();
		for (Change change : longTransaction.getChanges()) {
			if (change instanceof CreateObjectChange) {
				change.execute(ifcModel, project, concreteRevision, getDatabaseSession(), created, deleted);
				summaryMap.add(((CreateObjectChange)change).geteClass(), 1);
			}
		}
		// Then do the rest
		for (Change change : longTransaction.getChanges()) {
			if (!(change instanceof CreateObjectChange)) {
				if (change instanceof RemoveObjectChange) {
					summaryMap.remove(((RemoveObjectChange)change).geteClass(), 1);
				}
				change.execute(ifcModel, project, concreteRevision, getDatabaseSession(), created, deleted);
			}
		}
		
		if (bimServer.getServerSettingsCache().getServerSettings().isGenerateGeometryOnCheckin()) {
			setProgress("Generating Geometry...", -1);
			LoggerFactory.getLogger(CommitTransactionDatabaseAction.class).info("Size: " + ifcModel.size());
			try {
				new GeometryGenerator().generateGeometry(authorization.getUoid(), bimServer.getPluginManager(), getDatabaseSession(), ifcModel, project.getId(), concreteRevision.getId(), revision, true, geometryCache);
			} catch (RenderException e) {
				throw new UserException(e);
			}
			revision.setHasGeometry(true);
		}
		
		concreteRevision.setSummary(summaryMap.toRevisionSummary(getDatabaseSession()));

		getDatabaseSession().store(newRevisionAdded);
		getDatabaseSession().store(concreteRevision);
		getDatabaseSession().store(project);
		return concreteRevision;
	}

	public Revision getRevision() {
		return revision;
	}
}
