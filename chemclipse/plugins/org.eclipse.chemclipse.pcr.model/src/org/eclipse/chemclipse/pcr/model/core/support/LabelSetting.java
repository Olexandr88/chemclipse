/*******************************************************************************
 * Copyright (c) 2021 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Matthias Mailänder - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.pcr.model.core.support;

public enum LabelSetting {
	SAMPLENAME("Sample ID"), //
	COORDINATE("Coordinate"), //
	COORDINATE_SAMPLENAME("Coordinate + Sample ID"); //

	private String label = "";

	private LabelSetting(String label) {

		this.label = label;
	}

	public String getLabel() {

		return label;
	}

	public static String[][] getOptions() {

		LabelSetting[] labelSettings = values();
		String[][] elements = new String[labelSettings.length][2];
		//
		int counter = 0;
		for(LabelSetting labelSetting : labelSettings) {
			elements[counter][0] = labelSetting.getLabel();
			elements[counter][1] = labelSetting.name();
			counter++;
		}
		//
		return elements;
	}
}
