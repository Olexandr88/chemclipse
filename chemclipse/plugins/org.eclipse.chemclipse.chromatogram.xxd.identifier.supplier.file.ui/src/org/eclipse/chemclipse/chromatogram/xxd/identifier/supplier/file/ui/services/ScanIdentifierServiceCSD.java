/*******************************************************************************
 * Copyright (c) 2022 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.identifier.supplier.file.ui.services;

import org.eclipse.chemclipse.chromatogram.xxd.identifier.supplier.file.ui.preferences.PreferencePageUnknownCSD;
import org.eclipse.chemclipse.model.types.DataType;
import org.eclipse.chemclipse.swt.ui.services.IScanIdentifierService;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(service = {IScanIdentifierService.class}, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ScanIdentifierServiceCSD implements IScanIdentifierService {

	@Override
	public DataType getDataType() {

		return DataType.CSD;
	}

	@Override
	public Class<? extends IWorkbenchPreferencePage> getPreferencePage() {

		return PreferencePageUnknownCSD.class;
	}
}