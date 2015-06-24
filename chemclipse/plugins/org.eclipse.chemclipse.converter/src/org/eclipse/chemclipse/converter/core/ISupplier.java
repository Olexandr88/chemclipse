/*******************************************************************************
 * Copyright (c) 2008, 2015 Philip (eselmeister) Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip (eselmeister) Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.converter.core;

public interface ISupplier {

	/**
	 * The id of the extension point: e.g.
	 * (org.eclipse.chemclipse.msd.converter.supplier.agilent)
	 * 
	 * @return String
	 */
	String getId();

	/**
	 * A short description of the functionality of the extension point.
	 * 
	 * @return String
	 */
	String getDescription();

	/**
	 * The filter that will be shown in the FileDialog.
	 * 
	 * @return String
	 */
	String getFilterName();

	/**
	 * The file extension, e.g. Agilent (.MS) will be returned.<br/>
	 * If the file extension has a value, it starts in every case with a point.
	 * 
	 * @return String
	 */
	String getFileExtension();

	/**
	 * The default file name, e.g. Agilent (DATA).
	 * 
	 * @return String
	 */
	String getFileName();

	/**
	 * The chromatogram directory extension, e.g. Agilent (.D) will be returned.<br/>
	 * If the filter extension has a value, it starts in every case with a
	 * point.
	 * 
	 * @return String
	 */
	String getDirectoryExtension();

	/**
	 * Describes whether the chromatogram is exportable or not.
	 * 
	 * @return boolean
	 */
	boolean isExportable();

	/**
	 * Describes whether the chromatogram is importable or not.
	 * 
	 * @return boolean
	 */
	boolean isImportable();
}
