/*******************************************************************************
 * Copyright (c) 2008, 2016 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.amdis.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.chemclipse.converter.exceptions.FileIsEmptyException;
import org.eclipse.chemclipse.converter.exceptions.FileIsNotReadableException;
import org.eclipse.chemclipse.converter.exceptions.FileIsNotWriteableException;
import org.eclipse.chemclipse.converter.exceptions.NoConverterAvailableException;
import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.msd.converter.processing.massspectrum.IMassSpectrumImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.TestPathHelper;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.model.IVendorLibraryMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.implementation.Ion;
import org.eclipse.chemclipse.msd.model.implementation.ScanMSD;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;

public class MassSpectrumExportConverter_DB_1_ITest extends MassSpectrumExportConverterTestCase {

	@Override
	protected void setUp() throws Exception {

		exportFile = new File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTDIR_EXPORT) + File.separator + TestPathHelper.TESTFILE_EXPORT_DB_MSL);
		importFile = new File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTDIR_EXPORT) + File.separator + TestPathHelper.TESTFILE_EXPORT_DB_MSL);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testExport_1() throws AbundanceLimitExceededException, IonLimitExceededException, FileNotFoundException, FileIsNotWriteableException, IOException, NoConverterAvailableException, FileIsNotReadableException, FileIsEmptyException {

		IIon ion;
		IScanMSD ms = new ScanMSD();
		for(int i = 1; i <= 6; i++) {
			ion = new Ion(i, i * 10);
			ms.addIon(ion);
		}
		exportConverter.convert(exportFile, ms, false, new NullProgressMonitor());
		IMassSpectrumImportConverterProcessingInfo processingInfo = importConverter.convert(importFile, new NullProgressMonitor());
		try {
			massSpectra = processingInfo.getMassSpectra();
		} catch(TypeCastException e) {
			assertTrue("TypeCastException", false);
		}
		IScanMSD massSpectrum = massSpectra.getMassSpectrum(1);
		IVendorLibraryMassSpectrum amdisMS = null;
		if(massSpectrum instanceof IVendorLibraryMassSpectrum) {
			amdisMS = (IVendorLibraryMassSpectrum)massSpectrum;
		}
		assertNotNull("IAmdisMassSpectrum", amdisMS);
		assertEquals("Name", "NO IDENTIFIER AVAILABLE", amdisMS.getLibraryInformation().getName());
		assertEquals("CAS Number", "", amdisMS.getLibraryInformation().getCasNumber());
		assertEquals("Comments", "", amdisMS.getLibraryInformation().getComments());
		assertEquals("Retention Time", 0, amdisMS.getRetentionTime());
		assertEquals("Retention Index", 0.0f, amdisMS.getRetentionIndex());
		assertEquals("Ion", 6, amdisMS.getNumberOfIons());
		assertEquals("Lowest Ion", 1.0d, amdisMS.getLowestIon().getIon());
		assertEquals("Lowest Ion Abundance", 167.0f, amdisMS.getLowestIon().getAbundance());
		assertEquals("Highest Abundance Ion", 6.0d, amdisMS.getHighestAbundance().getIon());
		assertEquals("Highest Abundance", 1000.0f, amdisMS.getHighestAbundance().getAbundance());
	}
}
