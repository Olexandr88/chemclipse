/*******************************************************************************
 * Copyright (c) 2014, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.io;

import org.eclipse.chemclipse.msd.model.core.IIonTransitionSettings;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.TestPathHelper;

public class ChromatogramReader_2_MSD_0701_ITest extends ChromatogramReaderMSDTestCase {

	@Override
	protected void setUp() throws Exception {

		pathImport = TestPathHelper.getAbsolutePath(TestPathHelper.TESTFILE_IMPORT_CHROMATOGRAM_2_MSD_0701);
		super.setUp();
	}

	public void testReader_1() {

		assertEquals(207, chromatogram.getNumberOfScans());
	}

	public void testReader_2() {

		assertEquals("Chromatogram2-0701", chromatogram.getName());
	}

	public void testReader_3() {

		assertEquals(2030, chromatogram.getNumberOfScanIons());
	}

	public void testReader_4() {

		assertEquals(925500, chromatogram.getStartRetentionTime());
	}

	public void testReader_5() {

		assertEquals(1031906, chromatogram.getStopRetentionTime());
	}

	public void testReader_6() {

		assertEquals(62.8d, chromatogram.getStartIon());
	}

	public void testReader_7() {

		assertEquals(456.3d, chromatogram.getStopIon());
	}

	public void testReader_8() {

		assertEquals(3.237204E7f, chromatogram.getMaxSignal());
	}

	public void testReader_9() {

		assertEquals(143985.0f, chromatogram.getMinSignal());
	}

	public void testReader_10() {

		assertEquals(925500, chromatogram.getScanDelay());
	}

	public void testReader_11() {

		assertEquals(4985, chromatogram.getScanInterval());
	}

	public void testChromatogramReader_12() {

		IScanMSD massSpectrum = chromatogram.getSupplierScan(1);
		//
		assertEquals(6, massSpectrum.getNumberOfIons());
		assertEquals(159.0d, massSpectrum.getLowestIon().getIon());
		assertEquals(41303.0f, massSpectrum.getLowestIon().getAbundance());
		assertEquals(456.3d, massSpectrum.getHighestIon().getIon());
		assertEquals(58610.0f, massSpectrum.getHighestIon().getAbundance());
		assertEquals(377.4d, massSpectrum.getHighestAbundance().getIon());
		assertEquals(61234.0f, massSpectrum.getHighestAbundance().getAbundance());
		assertEquals(925500, massSpectrum.getRetentionTime());
		assertEquals(0.0f, massSpectrum.getRetentionIndex());
	}

	public void testChromatogramReader_13() {

		IScanMSD massSpectrum = chromatogram.getSupplierScan(92);
		//
		assertEquals(11, massSpectrum.getNumberOfIons());
		assertEquals(135.0d, massSpectrum.getLowestIon().getIon());
		assertEquals(24425.0f, massSpectrum.getLowestIon().getAbundance());
		assertEquals(330.0d, massSpectrum.getHighestIon().getIon());
		assertEquals(89695.0f, massSpectrum.getHighestIon().getAbundance());
		assertEquals(265.0d, massSpectrum.getHighestAbundance().getIon());
		assertEquals(176973.0f, massSpectrum.getHighestAbundance().getAbundance());
		assertEquals(972500, massSpectrum.getRetentionTime());
		assertEquals(0.0f, massSpectrum.getRetentionIndex());
	}

	public void testChromatogramReader_14() {

		IScanMSD massSpectrum = chromatogram.getSupplierScan(147);
		//
		assertEquals(10, massSpectrum.getNumberOfIons());
		assertEquals(62.9d, massSpectrum.getLowestIon().getIon());
		assertEquals(5871.0f, massSpectrum.getLowestIon().getAbundance());
		assertEquals(231.0d, massSpectrum.getHighestIon().getIon());
		assertEquals(47918.0f, massSpectrum.getHighestIon().getAbundance());
		assertEquals(81.1d, massSpectrum.getHighestAbundance().getIon());
		assertEquals(105117.0f, massSpectrum.getHighestAbundance().getAbundance());
		assertEquals(1001031, massSpectrum.getRetentionTime());
		assertEquals(0.0f, massSpectrum.getRetentionIndex());
	}

	public void testChromatogramReader_15() {

		IScanMSD massSpectrum = chromatogram.getSupplierScan(207);
		//
		assertEquals(15, massSpectrum.getNumberOfIons());
		assertEquals(78.9d, massSpectrum.getLowestIon().getIon());
		assertEquals(96610.0f, massSpectrum.getLowestIon().getAbundance());
		assertEquals(245.0d, massSpectrum.getHighestIon().getIon());
		assertEquals(43242.0f, massSpectrum.getHighestIon().getAbundance());
		assertEquals(78.9d, massSpectrum.getHighestAbundance().getIon());
		assertEquals(96610.0f, massSpectrum.getHighestAbundance().getAbundance());
		assertEquals(1031906, massSpectrum.getRetentionTime());
		assertEquals(0.0f, massSpectrum.getRetentionIndex());
	}

	public void testReader_16() {

		assertEquals(0, chromatogram.getNumberOfPeaks());
	}

	public void testReader_17() {

		assertEquals(0.0d, chromatogram.getPeakIntegratedArea());
	}

	public void testReader_18() {

		assertEquals(0.0d, chromatogram.getChromatogramIntegratedArea());
	}

	public void testReader_19() {

		assertEquals(0.0d, chromatogram.getBackgroundIntegratedArea());
	}

	public void testReader_20() {

		assertEquals(0.0d, chromatogram.getSampleWeight());
	}

	public void testReader_21() {

		IIonTransitionSettings ionTransitionSettings = chromatogram.getIonTransitionSettings();
		assertEquals(0, ionTransitionSettings.size());
	}
}
