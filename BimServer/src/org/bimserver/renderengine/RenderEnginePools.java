package org.bimserver.renderengine;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
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
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bimserver.BimServer;
import org.bimserver.emf.Schema;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.renderengine.RenderEngine;
import org.bimserver.plugins.renderengine.RenderEngineException;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.shared.exceptions.PluginException;

public class RenderEnginePools {

	private BimServer bimServer;
	private final Map<Schema, Map<String, RenderEnginePool>> pools = new HashMap<>();

	public RenderEnginePools(BimServer bimServer) throws RenderEngineException {
		this.bimServer = bimServer;
		int nrRenderEngineProcesses = this.bimServer.getServerSettingsCache().getServerSettings().getRenderEngineProcesses();
		
		Collection<RenderEnginePlugin> renderEnginePlugins = bimServer.getPluginManager().getAllRenderEnginePlugins(true).values();
		for (Schema schema : Schema.getIfcSchemas()) {
			HashMap<String, RenderEnginePool> map = new HashMap<>();
			pools.put(schema, map);
			for (RenderEnginePlugin renderEnginePlugin : renderEnginePlugins) {
				RenderEnginePool renderEnginePool = new RenderEnginePool(nrRenderEngineProcesses, new RenderEngineFactory(){
					@Override
					public RenderEngine createRenderEngine() throws RenderEngineException {
						return renderEnginePlugin.createRenderEngine(new PluginConfiguration(), schema.name());
					}});
				map.put(renderEnginePlugin.getClass().getName(), renderEnginePool);
			}
		}
	}

	public RenderEnginePool getRenderEnginePool(Schema schema, String className) throws PluginException {
		if (pools.containsKey(schema)) {
			Map<String, RenderEnginePool> map = pools.get(schema);
			if (map.containsKey(className)) {
				return map.get(className);
			} else {
				// Maybe this plugin has been installed after startup, let's have a look in the PluginManager
				RenderEnginePlugin renderEnginePlugin = bimServer.getPluginManager().getRenderEnginePlugin(className, true);
				if (renderEnginePlugin == null) {
					throw new PluginException("No render engine found for className " + className);
				} else {
					int nrRenderEngineProcesses = this.bimServer.getServerSettingsCache().getServerSettings().getRenderEngineProcesses();
					RenderEnginePool renderEnginePool = new RenderEnginePool(nrRenderEngineProcesses, new RenderEngineFactory(){
						@Override
						public RenderEngine createRenderEngine() throws RenderEngineException {
							return renderEnginePlugin.createRenderEngine(new PluginConfiguration(), schema.name());
						}});
					map.put(className, renderEnginePool);
					return renderEnginePool;
				}
			}
		} else {
			throw new PluginException("No render engine found for schema " + schema);
		}
	}
}