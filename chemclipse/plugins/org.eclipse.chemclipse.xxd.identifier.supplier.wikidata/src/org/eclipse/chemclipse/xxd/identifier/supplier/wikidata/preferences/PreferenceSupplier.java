/*******************************************************************************
 * Copyright (c) 2023, 2024 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Matthias Mailänder - initial API and implementation
 * Philip Wenig - preference initializer
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.identifier.supplier.wikidata.preferences;

import org.eclipse.chemclipse.support.preferences.AbstractPreferenceSupplier;
import org.eclipse.chemclipse.support.preferences.IPreferenceSupplier;
import org.eclipse.chemclipse.xxd.identifier.supplier.wikidata.Activator;

public class PreferenceSupplier extends AbstractPreferenceSupplier implements IPreferenceSupplier {

	private static IPreferenceSupplier preferenceSupplier = null;

	public static IPreferenceSupplier INSTANCE() {

		if(preferenceSupplier == null) {
			preferenceSupplier = new PreferenceSupplier();
		}
		return preferenceSupplier;
	}

	@Override
	public String getPreferenceNode() {

		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public void initializeDefaults() {

	}
}