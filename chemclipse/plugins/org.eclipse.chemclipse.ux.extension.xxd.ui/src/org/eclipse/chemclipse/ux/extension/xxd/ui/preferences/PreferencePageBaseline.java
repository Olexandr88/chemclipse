/*******************************************************************************
 * Copyright (c) 2017, 2024 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.preferences;

import org.eclipse.chemclipse.swt.ui.support.Colors;
import org.eclipse.chemclipse.ux.extension.xxd.ui.Activator;
import org.eclipse.chemclipse.ux.extension.xxd.ui.l10n.ExtensionMessages;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swtchart.extensions.charts.ChartOptions;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePageBaseline extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePageBaseline() {

		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setTitle(ExtensionMessages.baseline);
		setDescription("");
	}

	@Override
	public void createFieldEditors() {

		addField(new ComboFieldEditor(PreferenceSupplier.P_BASELINE_CHART_COMPRESSION_TYPE, ExtensionMessages.compressionType, ChartOptions.COMPRESSION_TYPES, getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceSupplier.P_COLOR_SCHEME_DISPLAY_BASELINE, ExtensionMessages.displayColorScheme, Colors.getAvailableColorSchemes(), getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}
