/*******************************************************************************
 * Copyright (c) 2018, 2023 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.preferences;

import org.eclipse.chemclipse.support.ui.preferences.fieldeditors.SpinnerFieldEditor;
import org.eclipse.chemclipse.ux.extension.xxd.ui.Activator;
import org.eclipse.chemclipse.ux.extension.xxd.ui.messages.ExtensionMessages;
import org.eclipse.chemclipse.ux.extension.xxd.ui.messages.IExtensionMessages;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.extensions.charts.ChartOptions;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ChromatogramAxisIntensity extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ChromatogramAxisIntensity() {

		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setTitle(ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.CHROMATOGRAM_INTENSITY_Y_AXIS));
		setDescription("");
	}

	@Override
	public void createFieldEditors() {

		addField(new StringFieldEditor(PreferenceConstants.P_TITLE_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.TITLE) + ":", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_FORMAT_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.FORMAT) + ":", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.SHOW), getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_POSITION_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.POSITION) + ":", ChartOptions.POSITIONS, getFieldEditorParent()));
		if(Display.isSystemDarkTheme()) {
			addField(new ColorFieldEditor(PreferenceConstants.P_COLOR_Y_AXIS_INTENSITY_DARKTHEME, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.COLOR) + ":", getFieldEditorParent()));
		} else {
			addField(new ColorFieldEditor(PreferenceConstants.P_COLOR_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.COLOR) + ":", getFieldEditorParent()));
		}
		addField(new StringFieldEditor(PreferenceConstants.P_FONT_NAME_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.FONT_NAME) + ":", getFieldEditorParent()));
		addField(new SpinnerFieldEditor(PreferenceConstants.P_FONT_SIZE_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.FONT_SIZE) + ":", PreferenceConstants.MIN_FONT_SIZE, PreferenceConstants.MAX_FONT_SIZE, getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_FONT_STYLE_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.FONT_STYLE) + ":", ChartOptions.FONT_STYLES, getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_GRIDLINE_STYLE_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.GRID_LINE_STYLE) + ":", ChartOptions.LINE_STYLES, getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceConstants.P_GRIDLINE_COLOR_Y_AXIS_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.GRID_LINE_COLOR) + ":", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_SHOW_Y_AXIS_TITLE_INTENSITY, ExtensionMessages.INSTANCE().getMessage(IExtensionMessages.SHOW_AXIS_TITLE), getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}
