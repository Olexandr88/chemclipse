/*******************************************************************************
 * Copyright (c) 2016, 2024 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Matthias Mailänder - auto detection for chromatography files
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.mzxml.converter;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.chemclipse.converter.core.AbstractFileContentMatcher;
import org.eclipse.chemclipse.converter.core.IFileContentMatcher;
import org.eclipse.chemclipse.msd.converter.supplier.mzxml.internal.io.AbstractReaderVersion;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ChromatogramFileContentMatcher extends AbstractFileContentMatcher implements IFileContentMatcher {

	@Override
	public boolean checkFileFormat(File file) {

		if(file.length() > HUNDRED_MB) {
			return true;
		}
		boolean isValidFormat = false;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			NodeList root = document.getElementsByTagNameNS("*", AbstractReaderVersion.NODE_MZXML);
			if(root.getLength() != 1) {
				return isValidFormat;
			}
			NodeList scanList = document.getElementsByTagName(AbstractReaderVersion.NODE_SCAN);
			if(scanList.getLength() > 1) {
				isValidFormat = true;
			}
		} catch(Exception e) {
			// fail silently
		}
		return isValidFormat;
	}
}
