/*******************************************************************************
 * Copyright (c) 2020, 2022 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.model.identifier;

import org.eclipse.chemclipse.support.text.ILabel;

public enum DeltaCalculation implements ILabel {
	NONE("None"), //
	RETENTION_TIME_MS("Retention Time [ms]"), //
	RETENTION_TIME_MIN("Retention Time [min]"), //
	RETENTION_INDEX("Retention Index"); //

	private String label = "";

	private DeltaCalculation(String label) {

		this.label = label;
	}

	public String label() {

		return label;
	}

	public static String[][] getOptions() {

		return ILabel.getOptions(values());
	}
}