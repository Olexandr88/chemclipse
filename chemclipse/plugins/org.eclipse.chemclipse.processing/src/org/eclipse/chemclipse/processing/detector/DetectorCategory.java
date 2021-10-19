/*******************************************************************************
 * Copyright (c) 2019, 2021 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.processing.detector;

import org.eclipse.chemclipse.processing.Messages;
import org.eclipse.chemclipse.support.text.ILabel;

public enum DetectorCategory implements ILabel {
	PEAK(Messages.getString("DetectorCategory.PEAK"));

	private String label;

	private DetectorCategory(String label) {

		this.label = label;
	}

	@Override
	public String label() {

		return label;
	}
}