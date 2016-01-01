/*******************************************************************************
 * Copyright (c) 2014, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.amdiscalri.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.chemclipse.chromatogram.filter.processing.IPeakFilterProcessingInfo;
import org.eclipse.chemclipse.chromatogram.filter.processing.PeakFilterProcessingInfo;
import org.eclipse.chemclipse.chromatogram.filter.result.IPeakFilterResult;
import org.eclipse.chemclipse.chromatogram.filter.result.PeakFilterResult;
import org.eclipse.chemclipse.chromatogram.filter.result.ResultStatus;
import org.eclipse.chemclipse.chromatogram.filter.settings.IPeakFilterSettings;
import org.eclipse.chemclipse.chromatogram.msd.filter.core.peak.AbstractPeakFilter;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.IPeakMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.amdiscalri.impl.RetentionIndexCalculator;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.amdiscalri.preferences.PreferenceSupplier;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.amdiscalri.settings.IRetentionIndexFilterSettingsPeak;

public class PeakFilter extends AbstractPeakFilter {

	@Override
	public IPeakFilterProcessingInfo applyFilter(List<IPeakMSD> peaks, IPeakFilterSettings peakFilterSettings, IProgressMonitor monitor) {

		IPeakFilterProcessingInfo processingInfo = new PeakFilterProcessingInfo();
		processingInfo.addMessages(validate(peaks, peakFilterSettings));
		if(processingInfo.hasErrorMessages()) {
			return processingInfo;
		}
		/*
		 * Filter
		 */
		IRetentionIndexFilterSettingsPeak retentionIndexFilterSettingsPeak;
		if(peakFilterSettings instanceof IRetentionIndexFilterSettingsPeak) {
			retentionIndexFilterSettingsPeak = (IRetentionIndexFilterSettingsPeak)peakFilterSettings;
		} else {
			retentionIndexFilterSettingsPeak = PreferenceSupplier.getPeakFilterSettings();
		}
		RetentionIndexCalculator calculator = new RetentionIndexCalculator();
		calculator.apply(peaks, retentionIndexFilterSettingsPeak, monitor);
		//
		IPeakFilterResult peakFilterResult = new PeakFilterResult(ResultStatus.OK, "The retention index filter has been applied successfully.");
		processingInfo.setPeakFilterResult(peakFilterResult);
		return processingInfo;
	}

	// -------------------
	@Override
	public IPeakFilterProcessingInfo applyFilter(IPeakMSD peak, IPeakFilterSettings peakFilterSettings, IProgressMonitor monitor) {

		List<IPeakMSD> peaks = new ArrayList<IPeakMSD>();
		return applyFilter(peaks, peakFilterSettings, monitor);
	}

	@Override
	public IPeakFilterProcessingInfo applyFilter(IPeakMSD peak, IProgressMonitor monitor) {

		List<IPeakMSD> peaks = new ArrayList<IPeakMSD>();
		IPeakFilterSettings peakFilterSettings = PreferenceSupplier.getPeakFilterSettings();
		return applyFilter(peaks, peakFilterSettings, monitor);
	}

	@Override
	public IPeakFilterProcessingInfo applyFilter(List<IPeakMSD> peaks, IProgressMonitor monitor) {

		IPeakFilterSettings peakFilterSettings = PreferenceSupplier.getPeakFilterSettings();
		return applyFilter(peaks, peakFilterSettings, monitor);
	}

	@Override
	public IPeakFilterProcessingInfo applyFilter(IChromatogramSelectionMSD chromatogramSelection, IPeakFilterSettings peakFilterSettings, IProgressMonitor monitor) {

		IChromatogramMSD chromatogram = chromatogramSelection.getChromatogramMSD();
		List<IChromatogramPeakMSD> peakList = chromatogram.getPeaks(chromatogramSelection);
		/*
		 * Create a list. This could be implemented in a better way.
		 */
		List<IPeakMSD> peaks = new ArrayList<IPeakMSD>();
		for(IChromatogramPeakMSD peak : peakList) {
			peaks.add(peak);
		}
		//
		return applyFilter(peaks, peakFilterSettings, monitor);
	}

	@Override
	public IPeakFilterProcessingInfo applyFilter(IChromatogramSelectionMSD chromatogramSelection, IProgressMonitor monitor) {

		IPeakFilterSettings peakFilterSettings = PreferenceSupplier.getPeakFilterSettings();
		return applyFilter(chromatogramSelection, peakFilterSettings, monitor);
	}
}
