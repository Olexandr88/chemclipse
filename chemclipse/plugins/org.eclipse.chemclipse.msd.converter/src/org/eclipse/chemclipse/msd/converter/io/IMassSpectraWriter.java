/*******************************************************************************
 * Copyright (c) 2012, 2015 Philip (eselmeister) Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip (eselmeister) Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.chemclipse.converter.exceptions.FileIsNotWriteableException;
import org.eclipse.chemclipse.msd.model.core.IMassSpectra;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;

public interface IMassSpectraWriter {

	/**
	 * Writes the given mass spectrum to the file.
	 * 
	 * @param file
	 * @param massSpectrum
	 * @param append
	 * @throws FileNotFoundException
	 * @throws FileIsNotWriteableException
	 * @throws IOException
	 */
	void write(File file, IScanMSD massSpectrum, boolean append) throws FileNotFoundException, FileIsNotWriteableException, IOException;

	/**
	 * Writes the given mass spectra to the file.
	 * 
	 * @param file
	 * @param massSpectra
	 * @param append
	 * @throws FileNotFoundException
	 * @throws FileIsNotWriteableException
	 * @throws IOException
	 */
	void write(File file, IMassSpectra massSpectra, boolean append) throws FileNotFoundException, FileIsNotWriteableException, IOException;

	/**
	 * Writes the mass spectrum with the given file writer.
	 * 
	 * @param fileWriter
	 * @param massSpectrum
	 * @throws IOException
	 */
	void writeMassSpectrum(FileWriter fileWriter, IScanMSD massSpectrum) throws IOException;
}
