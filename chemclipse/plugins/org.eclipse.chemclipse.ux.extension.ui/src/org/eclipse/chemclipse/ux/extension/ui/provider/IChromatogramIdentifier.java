/*******************************************************************************
 * Copyright (c) 2013, 2015 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.ui.provider;

import java.io.File;

public interface IChromatogramIdentifier {

	/**
	 * Check whether the file is a supplied chromatogram or not.
	 * 
	 * @param file
	 * @return boolean
	 */
	boolean isChromatogram(File file);

	/**
	 * Check whether the file is a supplied chromatogram directory or not.
	 * 
	 * @param file
	 * @return boolean
	 */
	boolean isChromatogramDirectory(File file);
}
