/*******************************************************************************
 * Copyright (c) 2013, 2023 Lablicate GmbH.
 * 
 * All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.converter.ui.preferences;

import org.eclipse.chemclipse.converter.preferences.PreferenceSupplier;
import org.eclipse.chemclipse.converter.ui.Activator;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {

		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setTitle("Converter");
		setDescription("");
	}

	@Override
	protected void createFieldEditors() {

		addField(new DirectoryFieldEditor(PreferenceSupplier.P_CHROMATOGRAM_EXPORT_FOLDER, "Chromatogram Export Folder", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceSupplier.P_METHOD_EXPLORER_PATH_ROOT_FOLDER, "Methods Folder", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceSupplier.P_SELECTED_METHOD_NAME, "Method Name", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {

	}
}