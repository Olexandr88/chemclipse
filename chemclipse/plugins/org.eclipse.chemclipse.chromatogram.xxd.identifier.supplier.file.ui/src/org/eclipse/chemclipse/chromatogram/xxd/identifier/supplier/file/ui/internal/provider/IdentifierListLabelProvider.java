/*******************************************************************************
 * Copyright (c) 2018, 2023 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.identifier.supplier.file.ui.internal.provider;

import org.eclipse.chemclipse.chromatogram.xxd.identifier.supplier.file.model.IdentifierFile;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImageProvider;
import org.eclipse.chemclipse.support.ui.provider.AbstractChemClipseLabelProvider;
import org.eclipse.swt.graphics.Image;

public class IdentifierListLabelProvider extends AbstractChemClipseLabelProvider {

	public static final String[] TITLES = { //
			"Name", //
			"Type", //
			"File Name", //
			"File Path" //
	};
	//
	public static final int[] BOUNDS = { //
			100, //
			150, //
			150, //
			250 //
	};

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if(columnIndex == 0) {
			return ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_FILE, IApplicationImageProvider.SIZE_16x16);
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {

		String text = "";
		if(element instanceof IdentifierFile identifierFile) {
			switch(columnIndex) {
				case 0:
					text = identifierFile.getSeparationColumn().getName();
					break;
				case 1:
					text = identifierFile.getSeparationColumn().getSeparationColumnType().label();
					break;
				case 2:
					text = identifierFile.getFile().getName();
					break;
				case 3:
					text = identifierFile.getFile().getAbsolutePath();
					break;
			}
		}
		return text;
	}

	@Override
	public Image getImage(Object element) {

		return ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_FILE, IApplicationImageProvider.SIZE_16x16);
	}
}
