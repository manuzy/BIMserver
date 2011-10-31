/**
 * Copyright (C) 2011  BIMserver.org
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
 */
package org.bimserver.models.ifc2x3.impl;

import org.bimserver.models.ifc2x3.Ifc2x3Package;
import org.bimserver.models.ifc2x3.IfcVolumetricFlowRateMeasure;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ifc Volumetric Flow Rate Measure</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.bimserver.models.ifc2x3.impl.IfcVolumetricFlowRateMeasureImpl#getWrappedValue <em>Wrapped Value</em>}</li>
 *   <li>{@link org.bimserver.models.ifc2x3.impl.IfcVolumetricFlowRateMeasureImpl#getWrappedValueAsString <em>Wrapped Value As String</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class IfcVolumetricFlowRateMeasureImpl extends WrappedValueImpl implements IfcVolumetricFlowRateMeasure {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IfcVolumetricFlowRateMeasureImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return Ifc2x3Package.Literals.IFC_VOLUMETRIC_FLOW_RATE_MEASURE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public float getWrappedValue() {
		return (Float) eGet(Ifc2x3Package.Literals.IFC_VOLUMETRIC_FLOW_RATE_MEASURE__WRAPPED_VALUE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWrappedValue(float newWrappedValue) {
		eSet(Ifc2x3Package.Literals.IFC_VOLUMETRIC_FLOW_RATE_MEASURE__WRAPPED_VALUE, newWrappedValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getWrappedValueAsString() {
		return (String) eGet(Ifc2x3Package.Literals.IFC_VOLUMETRIC_FLOW_RATE_MEASURE__WRAPPED_VALUE_AS_STRING, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWrappedValueAsString(String newWrappedValueAsString) {
		eSet(Ifc2x3Package.Literals.IFC_VOLUMETRIC_FLOW_RATE_MEASURE__WRAPPED_VALUE_AS_STRING, newWrappedValueAsString);
	}

} //IfcVolumetricFlowRateMeasureImpl