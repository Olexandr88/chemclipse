/*******************************************************************************
 * Copyright (c) 2018, 2022 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.wsd.model.core.support;

import java.util.List;
import java.util.Set;

import org.eclipse.chemclipse.model.core.IPeakIntensityValues;
import org.eclipse.chemclipse.model.core.MarkedTraceModus;
import org.eclipse.chemclipse.model.exceptions.ChromatogramIsNullException;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.implementation.PeakIntensityValues;
import org.eclipse.chemclipse.model.signals.ITotalScanSignal;
import org.eclipse.chemclipse.model.signals.ITotalScanSignalExtractor;
import org.eclipse.chemclipse.model.signals.ITotalScanSignals;
import org.eclipse.chemclipse.model.signals.TotalScanSignal;
import org.eclipse.chemclipse.model.signals.TotalScanSignalExtractor;
import org.eclipse.chemclipse.model.signals.TotalScanSignals;
import org.eclipse.chemclipse.model.signals.TotalScanSignalsModifier;
import org.eclipse.chemclipse.model.support.BackgroundAbundanceRange;
import org.eclipse.chemclipse.model.support.IBackgroundAbundanceRange;
import org.eclipse.chemclipse.model.support.IScanRange;
import org.eclipse.chemclipse.numeric.core.IPoint;
import org.eclipse.chemclipse.numeric.core.Point;
import org.eclipse.chemclipse.numeric.equations.Equations;
import org.eclipse.chemclipse.numeric.equations.LinearEquation;
import org.eclipse.chemclipse.wsd.model.core.IChromatogramPeakWSD;
import org.eclipse.chemclipse.wsd.model.core.IChromatogramWSD;
import org.eclipse.chemclipse.wsd.model.core.IPeakModelWSD;
import org.eclipse.chemclipse.wsd.model.core.IScanWSD;
import org.eclipse.chemclipse.wsd.model.core.implementation.ChromatogramPeakWSD;
import org.eclipse.chemclipse.wsd.model.core.implementation.PeakModelWSD;
import org.eclipse.chemclipse.wsd.model.core.implementation.ScanSignalWSD;
import org.eclipse.chemclipse.wsd.model.core.implementation.ScanWSD;
import org.eclipse.chemclipse.wsd.model.xwc.ExtractedWavelengthSignalExtractor;
import org.eclipse.chemclipse.wsd.model.xwc.IExtractedWavelengthSignal;
import org.eclipse.chemclipse.wsd.model.xwc.IExtractedWavelengthSignalExtractor;
import org.eclipse.chemclipse.wsd.model.xwc.IExtractedWavelengthSignals;
import org.eclipse.chemclipse.wsd.model.xwc.ITotalWavelengthSignalExtractor;
import org.eclipse.chemclipse.wsd.model.xwc.TotalWavelengthSignalExtractor;

public class PeakBuilderWSD {

	private PeakBuilderWSD() {

		throw new IllegalStateException("This utility class is meant to only host static functions.");
	}

	public static IChromatogramPeakWSD createPeak(IChromatogramWSD chromatogram, IScanRange scanRange, boolean calculatePeakIncludedBackground) throws PeakException {

		/*
		 * Validate the given objects.
		 */
		validateChromatogram(chromatogram);
		validateScanRange(scanRange);
		checkScanRange(chromatogram, scanRange);
		ITotalScanSignal totalScanSignal;
		IBackgroundAbundanceRange backgroundAbundanceRange;
		/*
		 * Get the total signals and determine the start and stop background
		 * abundance.
		 */
		ITotalScanSignals totalScanSignals = getTotalScanSignals(chromatogram, scanRange);
		/*
		 * Retrieve the start and stop signals of the peak to calculate its
		 * chromatogram and eventually peak internal background, if the start
		 * abundance is higher than the stop abundance or vice versa.
		 */
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStartScan());
		float startBackgroundAbundance = totalScanSignal.getTotalSignal();
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStopScan());
		float stopBackgroundAbundance = totalScanSignal.getTotalSignal();
		/*
		 * The abundance of base or startBackground/stopBackground (depends
		 * which is the lower value) is the chromatogram background.<br/> Then a
		 * peak included background could be calculated or not.<br/> This
		 * background is not the background of the chromatogram. It's the
		 * background of the peak.<br/> Think of, a peak could be skewed, means
		 * it starts with an abundance of zero and stops with a higher
		 * abundance.<br/> To include or exclude the background abundance in the
		 * IPeakModel affects the calculation of its width at different heights.
		 */
		if(calculatePeakIncludedBackground) {
			backgroundAbundanceRange = new BackgroundAbundanceRange(startBackgroundAbundance, stopBackgroundAbundance);
		} else {
			float base = Math.min(startBackgroundAbundance, stopBackgroundAbundance);
			backgroundAbundanceRange = new BackgroundAbundanceRange(base, base);
		}
		/*
		 * Calculate the intensity values.
		 */
		LinearEquation backgroundEquation = getBackgroundEquation(totalScanSignals, scanRange, backgroundAbundanceRange);
		ITotalScanSignals peakIntensityTotalScanSignals = adjustTotalScanSignals(totalScanSignals, backgroundEquation);
		IPeakIntensityValues peakIntensityValues = getPeakIntensityValues(peakIntensityTotalScanSignals);
		/*
		 * Create the peak.
		 */
		IScanWSD peakScanWSD = getPeakScan(chromatogram, totalScanSignals, backgroundEquation);
		IPeakModelWSD peakModel = new PeakModelWSD(peakScanWSD, peakIntensityValues, backgroundAbundanceRange.getStartBackgroundAbundance(), backgroundAbundanceRange.getStopBackgroundAbundance());
		return new ChromatogramPeakWSD(peakModel, chromatogram);
	}

	public static IChromatogramPeakWSD createPeak(IChromatogramWSD chromatogram, IScanRange scanRange, boolean calculatePeakIncludedBackground, Set<Integer> includedWavelengths, MarkedTraceModus filterMode) throws PeakException {

		/*
		 * Get the total signals and determine the start and stop background
		 * abundance.
		 */
		ITotalScanSignals totalWavelengthSignals = getTotalIonSignals(chromatogram, scanRange, new MarkedWavelengths(includedWavelengths, filterMode));
		/*
		 * Retrieve the start and stop signals of the peak to calculate its
		 * chromatogram and eventually peak internal background, if the start
		 * abundance is higher than the stop abundance or vice versa.
		 */
		ITotalScanSignal totalWavelengthSignal = totalWavelengthSignals.getTotalScanSignal(scanRange.getStartScan());
		float startBackgroundAbundance = totalWavelengthSignal.getTotalSignal();
		totalWavelengthSignal = totalWavelengthSignals.getTotalScanSignal(scanRange.getStopScan());
		float stopBackgroundAbundance = totalWavelengthSignal.getTotalSignal();
		/*
		 * The abundance of base or startBackground/stopBackground (depends
		 * which is the lower value) is the chromatogram background.<br/> Then a
		 * peak included background could be calculated or not.<br/> This
		 * background is not the background of the chromatogram. It's the
		 * background of the peak.<br/> Think of, a peak could be skewed, means
		 * it starts with an abundance of zero and stops with a higher
		 * abundance.<br/> To include or exclude the background abundance in the
		 * IPeakModel affects the calculation of its width at different heights.
		 */
		IBackgroundAbundanceRange backgroundAbundanceRange;
		if(calculatePeakIncludedBackground) {
			backgroundAbundanceRange = new BackgroundAbundanceRange(startBackgroundAbundance, stopBackgroundAbundance);
		} else {
			float base = Math.min(startBackgroundAbundance, stopBackgroundAbundance);
			backgroundAbundanceRange = new BackgroundAbundanceRange(base, base);
		}
		LinearEquation backgroundEquation = getBackgroundEquation(totalWavelengthSignals, scanRange, backgroundAbundanceRange);
		/*
		 * Calculate the intensity values.
		 */
		ITotalScanSignals peakIntensityTotalIonSignals = adjustTotalScanSignals(totalWavelengthSignals, backgroundEquation);
		IPeakIntensityValues peakIntensityValues = getPeakIntensityValues(peakIntensityTotalIonSignals);
		IScanWSD peakScan = getPeakScan(chromatogram, peakIntensityTotalIonSignals, backgroundEquation);
		/*
		 * Create the peak.
		 */
		IPeakModelWSD peakModel = new PeakModelWSD(peakScan, peakIntensityValues, backgroundAbundanceRange.getStartBackgroundAbundance(), backgroundAbundanceRange.getStopBackgroundAbundance());
		return new ChromatogramPeakWSD(peakModel, chromatogram);
	}

	private static ITotalScanSignals getTotalIonSignals(IChromatogramWSD chromatogram, IScanRange scanRange, MarkedWavelengths excludedWavelengths) {

		if(chromatogram == null || scanRange == null || excludedWavelengths == null) {
			throw new PeakException("The given values must not be null.");
		}
		/*
		 * Try to get the signals.
		 */
		try {
			ITotalWavelengthSignalExtractor totalWavelengthSignalExtractor = new TotalWavelengthSignalExtractor(chromatogram);
			return totalWavelengthSignalExtractor.getTotalScanSignals(scanRange.getStartScan(), scanRange.getStopScan(), excludedWavelengths);
		} catch(ChromatogramIsNullException e) {
			throw new PeakException("The chromatogram must not be null.");
		}
	}

	public static IChromatogramPeakWSD createPeak(IChromatogramWSD chromatogram, IScanRange scanRange, float startIntensity, float stopIntensity, Set<Integer> traces) throws PeakException {

		/*
		 * Validate the given objects.
		 */
		validateChromatogram(chromatogram);
		validateScanRange(scanRange);
		checkScanRange(chromatogram, scanRange);
		/*
		 * Filter the extracted signals.
		 */
		IExtractedWavelengthSignals extractedWavelengthSignals = getExtractedWavelengthSignals(chromatogram, scanRange, traces);
		/*
		 * Retrieve the start and stop signals of the peak to calculate its
		 * chromatogram and eventually peak internal background, if the start
		 * abundance is higher than the stop abundance or vice versa.
		 */
		try {
			/*
			 * Background
			 */
			IBackgroundAbundanceRange backgroundAbundanceRange = new BackgroundAbundanceRange(startIntensity, stopIntensity);
			/*
			 * Calculate the intensity values.
			 */
			LinearEquation backgroundEquation = getBackgroundEquation(extractedWavelengthSignals, scanRange, backgroundAbundanceRange);
			ITotalScanSignals peakIntensityTotalScanSignals = adjustTotalScanSignals(extractedWavelengthSignals, backgroundEquation);
			IPeakIntensityValues peakIntensityValues = getPeakIntensityValues(peakIntensityTotalScanSignals);
			/*
			 * Create the peak.
			 */
			IExtractedWavelengthSignal extractedWavelengthSignalMax = null;
			for(int i = extractedWavelengthSignals.getStartScan(); i < extractedWavelengthSignals.getStopScan(); i++) {
				IExtractedWavelengthSignal extractedWavelengthSignal = extractedWavelengthSignals.getExtractedWavelengthSignal(i);
				if(extractedWavelengthSignalMax == null) {
					extractedWavelengthSignalMax = extractedWavelengthSignal;
				} else {
					extractedWavelengthSignalMax = extractedWavelengthSignalMax.getTotalSignal() < extractedWavelengthSignal.getTotalSignal() ? extractedWavelengthSignal : extractedWavelengthSignalMax;
				}
			}
			//
			if(extractedWavelengthSignalMax != null) {
				IScanWSD peakScanWSD = new ScanWSD();
				int retentionTime = extractedWavelengthSignalMax.getRetentionTime();
				peakScanWSD.setRetentionTime(retentionTime);
				for(int trace : traces) {
					peakScanWSD.addScanSignal(new ScanSignalWSD(trace, extractedWavelengthSignalMax.getAbundance(trace)));
				}
				double adjustedTotalSignal = peakScanWSD.getTotalSignal() - backgroundEquation.calculateY(retentionTime);
				peakScanWSD.adjustTotalSignal((float)adjustedTotalSignal);
				IPeakModelWSD peakModel = new PeakModelWSD(peakScanWSD, peakIntensityValues, backgroundAbundanceRange.getStartBackgroundAbundance(), backgroundAbundanceRange.getStopBackgroundAbundance());
				return new ChromatogramPeakWSD(peakModel, chromatogram);
			} else {
				throw new PeakException();
			}
		} catch(Exception e) {
			throw new PeakException();
		}
	}

	public static IChromatogramPeakWSD createPeak(IChromatogramWSD chromatogram, IScanRange scanRange, float startIntensity, float stopIntensity) throws PeakException {

		BackgroundAbundanceRange backgroundAbundanceRange = new BackgroundAbundanceRange(startIntensity, stopIntensity);
		validateChromatogram(chromatogram);
		validateScanRange(scanRange);
		validateBackgroundAbundanceRange(backgroundAbundanceRange);
		ITotalScanSignals totalScanSignals = getTotalScanSignals(chromatogram, scanRange);
		/*
		 * Adjust and correct the background abundance range, if needed.
		 */
		LinearEquation backgroundEquation = getBackgroundEquation(totalScanSignals, scanRange, backgroundAbundanceRange);
		ITotalScanSignals peakIntensityTotalIonSignals = adjustTotalScanSignals(totalScanSignals, backgroundEquation);
		IPeakIntensityValues peakIntensityValues = getPeakIntensityValues(peakIntensityTotalIonSignals);
		/*
		 * Create the peak.
		 */
		IScanWSD peakScanWSD = getPeakScan(chromatogram, totalScanSignals, backgroundEquation);
		IPeakModelWSD peakModel = new PeakModelWSD(peakScanWSD, peakIntensityValues, backgroundAbundanceRange.getStartBackgroundAbundance(), backgroundAbundanceRange.getStopBackgroundAbundance());
		return new ChromatogramPeakWSD(peakModel, chromatogram);
	}

	public static IChromatogramPeakWSD createPeak(IChromatogramWSD chromatogram, IScanRange scanRange, IBackgroundAbundanceRange backgroundAbundanceRange, boolean checkBackgroundAbundanceRange) throws PeakException {

		validateChromatogram(chromatogram);
		validateScanRange(scanRange);
		validateBackgroundAbundanceRange(backgroundAbundanceRange);
		ITotalScanSignals totalScanSignals = getTotalScanSignals(chromatogram, scanRange);
		/*
		 * Adjust and correct the background abundance range, if needed.
		 */
		if(checkBackgroundAbundanceRange) {
			backgroundAbundanceRange = checkBackgroundAbundanceRange(totalScanSignals, scanRange, backgroundAbundanceRange);
		}
		LinearEquation backgroundEquation = getBackgroundEquation(totalScanSignals, scanRange, backgroundAbundanceRange);
		ITotalScanSignals peakIntensityTotalIonSignals = adjustTotalScanSignals(totalScanSignals, backgroundEquation);
		IPeakIntensityValues peakIntensityValues = getPeakIntensityValues(peakIntensityTotalIonSignals);
		/*
		 * Create the peak.
		 */
		IScanWSD peakScanWSD = getPeakScan(chromatogram, totalScanSignals, backgroundEquation);
		IPeakModelWSD peakModel = new PeakModelWSD(peakScanWSD, peakIntensityValues, backgroundAbundanceRange.getStartBackgroundAbundance(), backgroundAbundanceRange.getStopBackgroundAbundance());
		return new ChromatogramPeakWSD(peakModel, chromatogram);
	}

	protected static void validateChromatogram(IChromatogramWSD chromatogram) throws PeakException {

		if(chromatogram == null) {
			throw new PeakException("The chromatogram must not be null.");
		}
	}

	protected static void validateScanRange(IScanRange scanRange) throws PeakException {

		if(scanRange == null) {
			throw new PeakException("The scan range must not be null.");
		}
	}

	protected static void checkScanRange(IChromatogramWSD chromatogram, IScanRange scanRange) throws PeakException {

		if(chromatogram == null || scanRange == null) {
			throw new PeakException("The given chromatogram or scanRange must not be null.");
		}
		/*
		 * Validate the ion range against the chromatogram.
		 */
		if(scanRange.getStartScan() < 1 || scanRange.getStopScan() > chromatogram.getNumberOfScans()) {
			throw new PeakException("The given scan range is out of chromatogram borders.");
		}
	}

	protected static ITotalScanSignals getTotalScanSignals(IChromatogramWSD chromatogram, IScanRange scanRange) throws PeakException {

		if(chromatogram == null || scanRange == null) {
			throw new PeakException("The given values must not be null.");
		}
		/*
		 * Try to get the signals.
		 */
		try {
			ITotalScanSignalExtractor totalScanSignalExtractor = new TotalScanSignalExtractor(chromatogram);
			return totalScanSignalExtractor.getTotalScanSignals(scanRange.getStartScan(), scanRange.getStopScan());
		} catch(ChromatogramIsNullException e) {
			throw new PeakException("The chromatogram must not be null.");
		}
	}

	protected static IExtractedWavelengthSignals getExtractedWavelengthSignals(IChromatogramWSD chromatogram, IScanRange scanRange, Set<Integer> traces) {

		IExtractedWavelengthSignals extractedWavelengthSignals = getExtractedWavelengthSignals(chromatogram, scanRange);
		for(IExtractedWavelengthSignal extractedWavelengthSignal : extractedWavelengthSignals.getExtractedWavelengthSignals()) {
			for(int i = extractedWavelengthSignal.getStartWavelength(); i <= extractedWavelengthSignal.getStopWavelength(); i++) {
				/*
				 * Skip if trace shall be used.
				 */
				if(traces.contains(i)) {
					continue;
				}
				/*
				 * Set to 0.
				 */
				extractedWavelengthSignal.setAbundance(i, 0);
			}
		}
		//
		return extractedWavelengthSignals;
	}

	protected static IExtractedWavelengthSignals getExtractedWavelengthSignals(IChromatogramWSD chromatogram, IScanRange scanRange) throws PeakException {

		if(chromatogram == null || scanRange == null) {
			throw new PeakException("The given values must not be null.");
		}
		/*
		 * Try to get the signals.
		 */
		try {
			IExtractedWavelengthSignalExtractor wavelengthSignalExtractor = new ExtractedWavelengthSignalExtractor(chromatogram);
			return wavelengthSignalExtractor.getExtractedWavelengthSignals(scanRange.getStartScan(), scanRange.getStopScan());
		} catch(ChromatogramIsNullException e) {
			throw new PeakException("The chromatogram must not be null.");
		}
	}

	protected static LinearEquation getBackgroundEquation(ITotalScanSignals totalScanSignals, IScanRange scanRange, IBackgroundAbundanceRange backgroundAbundanceRange) throws PeakException {

		if(totalScanSignals == null || scanRange == null || backgroundAbundanceRange == null) {
			throw new PeakException("The given totalIonSignals, scanRange or backgroundAbundanceRange must not be null.");
		}
		ITotalScanSignal totalScanSignal;
		// P1
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStartScan());
		IPoint p1 = new Point(totalScanSignal.getRetentionTime(), backgroundAbundanceRange.getStartBackgroundAbundance());
		// P2
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStopScan());
		IPoint p2 = new Point(totalScanSignal.getRetentionTime(), backgroundAbundanceRange.getStopBackgroundAbundance());
		/*
		 * Create the background abundance equation.
		 */
		return Equations.createLinearEquation(p1, p2);
	}

	protected static LinearEquation getBackgroundEquation(IExtractedWavelengthSignals extractedWavelengthSignals, IScanRange scanRange, IBackgroundAbundanceRange backgroundAbundanceRange) throws Exception {

		if(extractedWavelengthSignals == null || scanRange == null || backgroundAbundanceRange == null) {
			throw new PeakException("The given signals, scanRange or backgroundAbundanceRange must not be null.");
		}
		//
		IExtractedWavelengthSignal extractedWavelengthSignal;
		// P1
		extractedWavelengthSignal = extractedWavelengthSignals.getExtractedWavelengthSignal(scanRange.getStartScan());
		IPoint p1 = new Point(extractedWavelengthSignal.getRetentionTime(), backgroundAbundanceRange.getStartBackgroundAbundance());
		// P2
		extractedWavelengthSignal = extractedWavelengthSignals.getExtractedWavelengthSignal(scanRange.getStopScan());
		IPoint p2 = new Point(extractedWavelengthSignal.getRetentionTime(), backgroundAbundanceRange.getStopBackgroundAbundance());
		/*
		 * Create the background abundance equation.
		 */
		return Equations.createLinearEquation(p1, p2);
	}

	protected static ITotalScanSignals adjustTotalScanSignals(ITotalScanSignals totalScanSignals, LinearEquation backgroundEquation) throws PeakException {

		if(totalScanSignals == null || backgroundEquation == null) {
			throw new PeakException("The given totalIonSignals or backgroundEquation must not be null.");
		}
		ITotalScanSignal totalScanSignal;
		/*
		 * Make a deep copy of totalIonSignals, normalize the values to
		 * IPeakIntensityValues.MAX_INTENSITY.
		 */
		ITotalScanSignals peakIntensityTotalScanSignals = totalScanSignals.makeDeepCopy();
		int start = peakIntensityTotalScanSignals.getStartScan();
		int stop = peakIntensityTotalScanSignals.getStopScan();
		float adjustedSignal;
		for(int scan = start; scan <= stop; scan++) {
			totalScanSignal = peakIntensityTotalScanSignals.getTotalScanSignal(scan);
			adjustedSignal = (float)(totalScanSignal.getTotalSignal() - backgroundEquation.calculateY(totalScanSignal.getRetentionTime()));
			/*
			 * Check, that the total ion signal is >= 0!
			 */
			if(adjustedSignal < 0.0f) {
				adjustedSignal = 0.0f;
			}
			totalScanSignal.setTotalSignal(adjustedSignal);
		}
		TotalScanSignalsModifier.normalize(peakIntensityTotalScanSignals, IPeakIntensityValues.MAX_INTENSITY);
		return peakIntensityTotalScanSignals;
	}

	protected static ITotalScanSignals adjustTotalScanSignals(IExtractedWavelengthSignals extractedWavelengthSignals, LinearEquation backgroundEquation) throws Exception {

		if(extractedWavelengthSignals == null || backgroundEquation == null) {
			throw new PeakException("The given wavelength signals or backgroundEquation must not be null.");
		}
		/*
		 * Make a deep copy of totalIonSignals, normalize the values to
		 * IPeakIntensityValues.MAX_INTENSITY.
		 */
		int start = extractedWavelengthSignals.getStartScan();
		int stop = extractedWavelengthSignals.getStopScan();
		ITotalScanSignals peakIntensityTotalScanSignals = new TotalScanSignals(start, stop);
		//
		for(int scan = start; scan <= stop; scan++) {
			IExtractedWavelengthSignal extractedWavelengthSignal = extractedWavelengthSignals.getExtractedWavelengthSignal(scan);
			int retentionTime = extractedWavelengthSignal.getRetentionTime();
			float adjustedSignal = (float)(extractedWavelengthSignal.getTotalSignal() - backgroundEquation.calculateY(retentionTime));
			/*
			 * Check, that the total ion signal is >= 0!
			 */
			if(adjustedSignal < 0.0f) {
				adjustedSignal = 0.0f;
			}
			//
			ITotalScanSignal totalScanSignal = new TotalScanSignal(retentionTime, 0.0f, adjustedSignal);
			peakIntensityTotalScanSignals.add(totalScanSignal);
		}
		//
		TotalScanSignalsModifier.normalize(peakIntensityTotalScanSignals, IPeakIntensityValues.MAX_INTENSITY);
		return peakIntensityTotalScanSignals;
	}

	protected static IPeakIntensityValues getPeakIntensityValues(ITotalScanSignals peakIntensityTotalIonSignals) throws PeakException {

		if(peakIntensityTotalIonSignals == null) {
			throw new PeakException("The peakIntensityTotalIonSignals must not be null.");
		}
		/*
		 * Build a new peakIntensityValues object and add the normalized total
		 * ion signals.
		 */
		IPeakIntensityValues peakIntensityValues = new PeakIntensityValues();
		List<ITotalScanSignal> signals = peakIntensityTotalIonSignals.getTotalScanSignals();
		for(ITotalScanSignal signal : signals) {
			peakIntensityValues.addIntensityValue(signal.getRetentionTime(), signal.getTotalSignal());
		}
		return peakIntensityValues;
	}

	private static IScanWSD getPeakScan(IChromatogramWSD chromatogram, ITotalScanSignals totalScanSignals, LinearEquation backgroundEquation) {

		if(chromatogram == null || totalScanSignals == null || backgroundEquation == null) {
			throw new PeakException("The chromatogram, totalScanSignals or backgroundEquation must not be null.");
		}
		/*
		 * Get the peak mass spectrum and subtract the background.
		 */
		IScanWSD peakScanWSD = null;
		ITotalScanSignal totalScanSignal = totalScanSignals.getMaxTotalScanSignal();
		if(totalScanSignal != null) {
			int scan = chromatogram.getScanNumber(totalScanSignal.getRetentionTime());
			IScanWSD scanWSD = chromatogram.getSupplierScan(scan);
			float actualSignal = scanWSD.getTotalSignal();
			float backgroundSignal = (float)backgroundEquation.calculateY(totalScanSignal.getRetentionTime());
			float correctedSignal = actualSignal - backgroundSignal;
			float percentage = (100.0f / correctedSignal) * actualSignal;
			/*
			 * Adjust the peak scan.
			 */
			peakScanWSD = new ScanWSD(scanWSD, percentage);
		}
		//
		return peakScanWSD;
	}

	protected static void validateBackgroundAbundanceRange(IBackgroundAbundanceRange backgroundAbundanceRange) throws PeakException {

		if(backgroundAbundanceRange == null) {
			throw new PeakException("The background abundance range must not be null.");
		}
	}

	protected static IBackgroundAbundanceRange checkBackgroundAbundanceRange(ITotalScanSignals totalScanSignals, IScanRange scanRange, IBackgroundAbundanceRange backgroundAbundanceRange) throws PeakException {

		ITotalScanSignal totalScanSignal;
		float startBackgroundAbundance = 0.0f;
		float stopBackgroundAbundance = 0.0f;
		boolean adjustBackgroundAbundance = false;
		if(totalScanSignals == null || scanRange == null || backgroundAbundanceRange == null) {
			throw new PeakException("The given values must not be null.");
		}
		/*
		 * Check the start background abundance.
		 */
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStartScan());
		if(totalScanSignal != null) {
			float background = backgroundAbundanceRange.getStartBackgroundAbundance();
			float signal = totalScanSignal.getTotalSignal();
			if(background <= signal) {
				startBackgroundAbundance = background;
			} else {
				startBackgroundAbundance = signal;
				adjustBackgroundAbundance = true;
			}
		} else {
			adjustBackgroundAbundance = true;
		}
		/*
		 * Check the stop background abundance.
		 */
		totalScanSignal = totalScanSignals.getTotalScanSignal(scanRange.getStopScan());
		if(totalScanSignal != null) {
			float background = backgroundAbundanceRange.getStopBackgroundAbundance();
			float signal = totalScanSignal.getTotalSignal();
			if(background <= signal) {
				stopBackgroundAbundance = background;
			} else {
				stopBackgroundAbundance = signal;
				adjustBackgroundAbundance = true;
			}
		} else {
			adjustBackgroundAbundance = true;
		}
		/*
		 * If necessary correct the background abundance range.
		 */
		if(adjustBackgroundAbundance) {
			backgroundAbundanceRange = new BackgroundAbundanceRange(startBackgroundAbundance, stopBackgroundAbundance);
		}
		return backgroundAbundanceRange;
	}
}
