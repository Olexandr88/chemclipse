/*******************************************************************************
 * Copyright (c) 2014, 2023 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Matthias Mailänder - functional reader for specification version 1.05
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.mzdata.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.msd.converter.io.AbstractChromatogramMSDReader;
import org.eclipse.chemclipse.msd.converter.io.IChromatogramMSDReader;
import org.eclipse.chemclipse.msd.converter.supplier.mzdata.internal.io.ChromatogramReaderVersion104;
import org.eclipse.chemclipse.msd.converter.supplier.mzdata.internal.io.ChromatogramReaderVersion105;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.xxd.converter.supplier.io.exception.UnknownVersionException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ChromatogramReader extends AbstractChromatogramMSDReader implements IChromatogramMSDReader {

	public static IChromatogramMSDReader getReader(final File file) throws IOException {

		IChromatogramMSDReader chromatogramReader = null;
		final char[] charBuffer = new char[350];
		try (final FileReader fileReader = new FileReader(file)) {
			fileReader.read(charBuffer);
		}
		final String header = new String(charBuffer);
		if(header.contains(ChromatogramReaderVersion105.VERSION)) {
			chromatogramReader = new ChromatogramReaderVersion105();
		} else if(header.contains(ChromatogramReaderVersion104.VERSION)) {
			chromatogramReader = new ChromatogramReaderVersion104();
		} else {
			throw new UnknownVersionException();
		}
		//
		return chromatogramReader;
	}

	@Override
	public IChromatogramMSD read(final File file, final IProgressMonitor monitor) throws IOException, InterruptedException {

		final IChromatogramMSDReader chromatogramReader = getReader(file);
		return chromatogramReader.read(file, monitor);
	}

	@Override
	public IChromatogramOverview readOverview(final File file, final IProgressMonitor monitor) throws IOException {

		final IChromatogramMSDReader chromatogramReader = getReader(file);
		return chromatogramReader.readOverview(file, monitor);
	}
}
