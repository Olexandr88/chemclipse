/*******************************************************************************
 * Copyright (c) 2010, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.normalizer.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.normalizer.Activator;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.normalizer.settings.ISupplierFilterSettings;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.normalizer.settings.SupplierFilterSettings;
import org.eclipse.chemclipse.support.preferences.IPreferenceSupplier;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class PreferenceSupplier implements IPreferenceSupplier {

	public static final String P_NORMALIZATION_BASE = "normalizationBase";
	public static final float DEF_NORMALIZATION_BASE = 1000.0f;
	public static final float MIN_NORMALIZATION_BASE = 1.0f;
	public static final float MAX_NORMALIZATION_BASE = Float.MAX_VALUE;
	private static IPreferenceSupplier preferenceSupplier;

	public static IPreferenceSupplier INSTANCE() {

		if(preferenceSupplier == null) {
			preferenceSupplier = new PreferenceSupplier();
		}
		return preferenceSupplier;
	}

	@Override
	public IScopeContext getScopeContext() {

		return InstanceScope.INSTANCE;
	}

	@Override
	public String getPreferenceNode() {

		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public Map<String, String> getDefaultValues() {

		Map<String, String> defaultValues = new HashMap<String, String>();
		defaultValues.put(P_NORMALIZATION_BASE, Float.toString(DEF_NORMALIZATION_BASE));
		return defaultValues;
	}

	@Override
	public IEclipsePreferences getPreferences() {

		return getScopeContext().getNode(getPreferenceNode());
	}

	/**
	 * Returns the chromatogram filter settings.
	 * 
	 * @return IChromatogramFilterSettings
	 */
	public static ISupplierFilterSettings getChromatogramFilterSettings() {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
		ISupplierFilterSettings chromatogramFilterSettings = new SupplierFilterSettings();
		/*
		 * Get the actual preference.
		 * If it's not available, a default value will be returned.
		 */
		chromatogramFilterSettings.setNormalizationBase(preferences.getFloat(P_NORMALIZATION_BASE, DEF_NORMALIZATION_BASE));
		return chromatogramFilterSettings;
	}
}
