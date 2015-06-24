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
package org.eclipse.chemclipse.msd.model.implementation;

import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.chemclipse.model.core.IPeakIntensityValues;
import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.implementation.PeakIntensityValues;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.IPeakIon;
import org.eclipse.chemclipse.msd.model.core.IPeakMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.IPeakModelMSD;
import org.eclipse.chemclipse.msd.model.core.IVendorMassSpectrum;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;

import junit.framework.TestCase;

public class ChromatogramPeaksTestCase extends TestCase {

	private IPeakModelMSD peakModel;
	private IPeakMassSpectrum peakMaximum;
	private IPeakIon peakIon;
	private TreeMap<Float, Float> fragmentValues;
	private IPeakIntensityValues intensityValues;
	private TreeMap<Integer, Float> scanValues;
	private float startBackgroundAbundance = 0.0f;
	private float stopBackgroundAbundance = 0.0f;
	private IChromatogramMSD chromatogram;
	private IIon ion;
	private IVendorMassSpectrum supplierMassSpectrum;
	private IChromatogramPeakMSD peak1;
	private IChromatogramPeakMSD peak2;
	private IChromatogramPeakMSD peak3;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		createChromatogram();
		createPeak1();
		createPeak2();
		createPeak3();
	}

	private void createChromatogram() throws AbundanceLimitExceededException, IonLimitExceededException {

		chromatogram = new ChromatogramMSD();
		fragmentValues = new TreeMap<Float, Float>();
		fragmentValues.put(104.0f, 2300.0f);
		fragmentValues.put(103.0f, 580.0f);
		fragmentValues.put(51.0f, 260.0f);
		fragmentValues.put(50.0f, 480.0f);
		fragmentValues.put(78.0f, 236.0f);
		fragmentValues.put(77.0f, 25.0f);
		fragmentValues.put(74.0f, 380.0f);
		fragmentValues.put(105.0f, 970.0f);
		/*
		 * Add some more ion values, for example as a background.<br/>
		 * 580 + 420 + 760 > 1760
		 */
		fragmentValues.put(43.0f, 580.0f);
		fragmentValues.put(18.0f, 420.0f);
		fragmentValues.put(28.0f, 760.0f);
		startBackgroundAbundance = 1760.0f;
		stopBackgroundAbundance = 1760.5f;
		/*
		 * Add Scan 1 (500) to 17 (16500)
		 */
		for(int i = 1; i <= 17; i++) {
			supplierMassSpectrum = new VendorMassSpectrum();
			for(Entry<Float, Float> entry : fragmentValues.entrySet()) {
				ion = new Ion(entry.getKey(), entry.getValue());
				supplierMassSpectrum.addIon(ion);
			}
			chromatogram.addScan(supplierMassSpectrum);
		}
		chromatogram.setScanDelay(500);
		chromatogram.setScanInterval(1000);
		chromatogram.recalculateRetentionTimes();
	}

	private void createPeak1() throws IllegalArgumentException, PeakException, AbundanceLimitExceededException, IonLimitExceededException {

		peakMaximum = new PeakMassSpectrum();
		// remove some ions.
		fragmentValues.remove(43.0f);
		fragmentValues.remove(18.0f);
		fragmentValues.remove(28.0f);
		for(Entry<Float, Float> entry : fragmentValues.entrySet()) {
			peakIon = new PeakIon(entry.getKey(), entry.getValue());
			peakMaximum.addIon(peakIon);
		}
		// ----------------------IntensityValues
		intensityValues = new PeakIntensityValues();
		/*
		 * Add Peak (1500) to 16 (15500)
		 */
		scanValues = new TreeMap<Integer, Float>();
		scanValues.put(1500, 0.0f);
		scanValues.put(2500, 5.0f);
		scanValues.put(3500, 10.0f);
		scanValues.put(4500, 15.0f);
		scanValues.put(5500, 20.0f);
		scanValues.put(6500, 30.0f);
		scanValues.put(7500, 46.0f);
		scanValues.put(8500, 82.0f);
		scanValues.put(9500, 100.0f);
		scanValues.put(10500, 86.0f);
		scanValues.put(11500, 64.0f);
		scanValues.put(12500, 43.0f);
		scanValues.put(13500, 30.0f);
		scanValues.put(14500, 15.0f);
		scanValues.put(15500, 4.0f);
		for(Entry<Integer, Float> entry : scanValues.entrySet()) {
			intensityValues.addIntensityValue(entry.getKey(), entry.getValue());
		}
		// ----------------------IntensityValues
		peakModel = new PeakModelMSD(peakMaximum, intensityValues, startBackgroundAbundance, stopBackgroundAbundance);
		peak1 = new ChromatogramPeakMSD(peakModel, chromatogram);
	}

	private void createPeak2() throws IllegalArgumentException, PeakException, AbundanceLimitExceededException, IonLimitExceededException {

		peakMaximum = new PeakMassSpectrum();
		// remove some ions.
		fragmentValues.remove(74.0f);
		fragmentValues.remove(77.0f);
		for(Entry<Float, Float> entry : fragmentValues.entrySet()) {
			peakIon = new PeakIon(entry.getKey(), entry.getValue());
			peakMaximum.addIon(peakIon);
		}
		// ----------------------IntensityValues
		intensityValues = new PeakIntensityValues();
		/*
		 * Add Peak (1500) to 16 (15500)
		 */
		scanValues = new TreeMap<Integer, Float>();
		scanValues.put(5500, 4.0f);
		scanValues.put(6500, 10.0f);
		scanValues.put(7500, 30.0f);
		scanValues.put(8500, 90.0f);
		scanValues.put(9500, 92.0f);
		scanValues.put(10500, 95.0f);
		scanValues.put(11500, 100.0f);
		scanValues.put(12500, 60.0f);
		scanValues.put(13500, 20.0f);
		scanValues.put(14500, 10.0f);
		scanValues.put(15500, 2.0f);
		for(Entry<Integer, Float> entry : scanValues.entrySet()) {
			intensityValues.addIntensityValue(entry.getKey(), entry.getValue());
		}
		// ----------------------IntensityValues
		peakModel = new PeakModelMSD(peakMaximum, intensityValues, startBackgroundAbundance, stopBackgroundAbundance);
		peak2 = new ChromatogramPeakMSD(peakModel, chromatogram);
	}

	private void createPeak3() throws IllegalArgumentException, PeakException, AbundanceLimitExceededException, IonLimitExceededException {

		peakMaximum = new PeakMassSpectrum();
		// remove some ions.
		fragmentValues.remove(103.0f);
		fragmentValues.remove(104.0f);
		for(Entry<Float, Float> entry : fragmentValues.entrySet()) {
			peakIon = new PeakIon(entry.getKey(), entry.getValue());
			peakMaximum.addIon(peakIon);
		}
		// ----------------------IntensityValues
		intensityValues = new PeakIntensityValues();
		/*
		 * Add Peak (1500) to 16 (15500)
		 */
		scanValues = new TreeMap<Integer, Float>();
		scanValues.put(3500, 0.0f);
		scanValues.put(4500, 20.0f);
		scanValues.put(5500, 35.0f);
		scanValues.put(6500, 60.0f);
		scanValues.put(7500, 100.0f);
		scanValues.put(8500, 82.0f);
		scanValues.put(9500, 40.0f);
		scanValues.put(10500, 10.0f);
		for(Entry<Integer, Float> entry : scanValues.entrySet()) {
			intensityValues.addIntensityValue(entry.getKey(), entry.getValue());
		}
		// ----------------------IntensityValues
		peakModel = new PeakModelMSD(peakMaximum, intensityValues, startBackgroundAbundance, stopBackgroundAbundance);
		peak3 = new ChromatogramPeakMSD(peakModel, chromatogram);
	}

	@Override
	protected void tearDown() throws Exception {

		peakModel = null;
		peakMaximum = null;
		peakIon = null;
		fragmentValues = null;
		intensityValues = null;
		scanValues = null;
		chromatogram = null;
		super.tearDown();
	}

	protected IChromatogramPeakMSD getPeak1() {

		return peak1;
	}

	protected IChromatogramPeakMSD getPeak2() {

		return peak2;
	}

	protected IChromatogramPeakMSD getPeak3() {

		return peak3;
	}
}
