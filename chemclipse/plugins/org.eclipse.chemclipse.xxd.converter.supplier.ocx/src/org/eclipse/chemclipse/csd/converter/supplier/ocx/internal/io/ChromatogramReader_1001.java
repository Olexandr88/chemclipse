/*******************************************************************************
 * Copyright (c) 2014, 2024 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.csd.converter.supplier.ocx.internal.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.chemclipse.csd.converter.supplier.ocx.io.IChromatogramCSDZipReader;
import org.eclipse.chemclipse.csd.converter.supplier.ocx.model.chromatogram.IVendorChromatogram;
import org.eclipse.chemclipse.csd.converter.supplier.ocx.model.chromatogram.IVendorScan;
import org.eclipse.chemclipse.csd.converter.supplier.ocx.model.chromatogram.VendorChromatogram;
import org.eclipse.chemclipse.csd.converter.supplier.ocx.model.chromatogram.VendorScan;
import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.csd.model.core.IChromatogramPeakCSD;
import org.eclipse.chemclipse.csd.model.core.IPeakModelCSD;
import org.eclipse.chemclipse.csd.model.implementation.ChromatogramPeakCSD;
import org.eclipse.chemclipse.csd.model.implementation.PeakModelCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.baseline.IBaselineModel;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.model.core.IIntegrationEntry;
import org.eclipse.chemclipse.model.core.IPeakIntensityValues;
import org.eclipse.chemclipse.model.core.PeakType;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.implementation.IntegrationEntry;
import org.eclipse.chemclipse.model.implementation.PeakIntensityValues;
import org.eclipse.chemclipse.xxd.converter.supplier.ocx.internal.support.BaselineElement;
import org.eclipse.chemclipse.xxd.converter.supplier.ocx.internal.support.Format;
import org.eclipse.chemclipse.xxd.converter.supplier.ocx.internal.support.IBaselineElement;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Methods are copied to ensure that file formats are kept readable even if they contain errors.
 * This is suitable but I know, it's not the best way to achieve long term support for older formats.
 */
public class ChromatogramReader_1001 extends AbstractChromatogramReader implements IChromatogramCSDZipReader {

	private static final Logger logger = Logger.getLogger(ChromatogramReader_1001.class);

	@Override
	public IChromatogramCSD read(File file, IProgressMonitor monitor) throws IOException {

		IChromatogramCSD chromatogram = null;
		try (ZipFile zipFile = new ZipFile(file)) {
			if(isValidFileFormat(zipFile)) {
				chromatogram = readFromZipFile(zipFile, "", file, monitor);
			}
		}
		return chromatogram;
	}

	@Override
	public IChromatogramOverview readOverview(File file, IProgressMonitor monitor) throws IOException {

		IChromatogramOverview chromatogramOverview = null;
		try (ZipFile zipFile = new ZipFile(file)) {
			if(isValidFileFormat(zipFile)) {
				chromatogramOverview = readFromZipFile(zipFile, "", file, monitor);
			}
		}
		return chromatogramOverview;
	}

	@Override
	public IChromatogramCSD read(ZipInputStream zipInputStream, String directoryPrefix, IProgressMonitor monitor) throws IOException {

		return readZipData(zipInputStream, directoryPrefix, null, monitor);
	}

	@Override
	public IChromatogramCSD read(ZipFile zipFile, String directoryPrefix, IProgressMonitor monitor) throws IOException {

		return readFromZipFile(zipFile, directoryPrefix, null, monitor);
	}

	private IChromatogramCSD readFromZipFile(ZipFile zipFile, String directoryPrefix, File file, IProgressMonitor monitor) throws IOException {

		return readZipData(zipFile, directoryPrefix, file, monitor);
	}

	/*
	 * Object = ZipFile or ZipInputStream
	 * @param object
	 * @param file
	 * @return
	 */
	private IChromatogramCSD readZipData(Object object, String directoryPrefix, File file, IProgressMonitor monitor) throws IOException {

		boolean closeStream;
		if(object instanceof ZipFile) {
			/*
			 * ZipFile
			 */
			closeStream = true;
		} else if(object instanceof ZipInputStream) {
			/*
			 * ZipInputStream
			 */
			closeStream = false;
		} else {
			return null;
		}
		//
		IVendorChromatogram chromatogram = null;
		/*
		 * Read the chromatographic information.
		 */
		chromatogram = new VendorChromatogram();
		readScans(getDataInputStream(object, directoryPrefix + Format.FILE_SCANS_FID), closeStream, chromatogram, monitor);
		readBaseline(getDataInputStream(object, directoryPrefix + Format.FILE_BASELINE_FID), closeStream, chromatogram, monitor);
		readPeaks(getDataInputStream(object, directoryPrefix + Format.FILE_PEAKS_FID), closeStream, chromatogram, monitor);
		//
		setAdditionalInformation(file, chromatogram, monitor);
		//
		return chromatogram;
	}

	private void readScans(DataInputStream dataInputStream, boolean closeStream, IChromatogramCSD chromatogram, IProgressMonitor monitor) throws IOException {

		/*
		 * Scans
		 */
		int scans = dataInputStream.readInt();
		for(int scan = 1; scan <= scans; scan++) {
			int retentionTime = dataInputStream.readInt();
			float retentionIndex = dataInputStream.readFloat();
			float totalSignal = dataInputStream.readFloat();
			//
			IVendorScan scanFID = new VendorScan(retentionTime, totalSignal);
			scanFID.setRetentionIndex(retentionIndex);
			chromatogram.addScan(scanFID);
		}
		//
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private void readBaseline(DataInputStream dataInputStream, boolean closeStream, IChromatogramCSD chromatogram, IProgressMonitor monitor) throws IOException {

		/*
		 * Get the Baseline
		 */
		int scans = dataInputStream.readInt(); // Number of Scans
		List<IBaselineElement> baselineElements = new ArrayList<>();
		for(int scan = 1; scan <= scans; scan++) {
			int retentionTime = dataInputStream.readInt();
			float backgroundAbundance = dataInputStream.readFloat();
			IBaselineElement baselineElement = new BaselineElement(retentionTime, backgroundAbundance);
			baselineElements.add(baselineElement);
		}
		/*
		 * Set the Baseline
		 */
		IBaselineModel baselineModel = chromatogram.getBaselineModel();
		for(int index = 0; index < (scans - 1); index++) {
			/*
			 * Retention times and background abundances.
			 */
			IBaselineElement baselineElement = baselineElements.get(index);
			IBaselineElement baselineElementNext = baselineElements.get(index + 1);
			int startRetentionTime = baselineElement.getRetentionTime();
			float startBackgroundAbundance = baselineElement.getBackgroundAbundance();
			int stopRetentionTime = baselineElementNext.getRetentionTime();
			float stopBackgroundAbundance = baselineElementNext.getBackgroundAbundance();
			/*
			 * Set the baseline.
			 */
			baselineModel.addBaseline(startRetentionTime, stopRetentionTime, startBackgroundAbundance, stopBackgroundAbundance, false);
		}
		//
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private void readPeaks(DataInputStream dataInputStream, boolean closeStream, IChromatogramCSD chromatogram, IProgressMonitor monitor) throws IOException {

		int numberOfPeaks = dataInputStream.readInt();
		for(int i = 1; i <= numberOfPeaks; i++) {
			try {
				IChromatogramPeakCSD peak = readPeak(dataInputStream, chromatogram, monitor);
				chromatogram.addPeak(peak);
			} catch(IllegalArgumentException e) {
				logger.warn(e);
			} catch(PeakException e) {
				logger.warn(e);
			}
		}
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private IChromatogramPeakCSD readPeak(DataInputStream dataInputStream, IChromatogramCSD chromatogram, IProgressMonitor monitor) throws IOException, IllegalArgumentException, PeakException {

		String detectorDescription = readString(dataInputStream); // Detector Description
		String integratorDescription = readString(dataInputStream); // Integrator Description
		String modelDescription = readString(dataInputStream); // Model Description
		PeakType peakType = PeakType.valueOf(readString(dataInputStream)); // Peak Type
		int suggestedNumberOfComponents = dataInputStream.readInt(); // Suggest Number Of Components
		//
		float startBackgroundAbundance = dataInputStream.readFloat(); // Start Background Abundance
		float stopBackgroundAbundance = dataInputStream.readFloat(); // Stop Background Abundance
		//
		int retentionTimeScan = dataInputStream.readInt();
		float retentionIndexScan = dataInputStream.readFloat();
		float totalSignalScan = dataInputStream.readFloat();
		IVendorScan peakMaximum = new VendorScan(retentionTimeScan, totalSignalScan);
		peakMaximum.setRetentionIndex(retentionIndexScan);
		//
		int numberOfRetentionTimes = dataInputStream.readInt(); // Number Retention Times
		IPeakIntensityValues intensityValues = new PeakIntensityValues(Float.MAX_VALUE);
		for(int i = 1; i <= numberOfRetentionTimes; i++) {
			int retentionTime = dataInputStream.readInt();
			float relativeIntensity = dataInputStream.readFloat(); // Intensity
			intensityValues.addIntensityValue(retentionTime, relativeIntensity);
		}
		intensityValues.normalize();
		//
		IPeakModelCSD peakModel = new PeakModelCSD(peakMaximum, intensityValues, startBackgroundAbundance, stopBackgroundAbundance);
		peakModel.setStrictModel(true); // Legacy
		IChromatogramPeakCSD peak = new ChromatogramPeakCSD(peakModel, chromatogram);
		peak.setDetectorDescription(detectorDescription);
		peak.setIntegratorDescription(integratorDescription);
		peak.setModelDescription(modelDescription);
		peak.setPeakType(peakType);
		peak.setSuggestedNumberOfComponents(suggestedNumberOfComponents);
		//
		List<IIntegrationEntry> integrationEntries = readIntegrationEntries(dataInputStream);
		peak.setIntegratedArea(integrationEntries, integratorDescription);
		//
		return peak;
	}

	private void setAdditionalInformation(File file, IChromatogramCSD chromatogram, IProgressMonitor monitor) {

		chromatogram.setConverterId(Format.CONVERTER_ID_CHROMATOGRAM);
		chromatogram.setFile(file);
		// Delay
		int startRetentionTime = chromatogram.getStartRetentionTime();
		int scanDelay = startRetentionTime;
		chromatogram.setScanDelay(scanDelay);
		// Interval
		int endRetentionTime = chromatogram.getStopRetentionTime();
		int scanInterval = endRetentionTime / chromatogram.getNumberOfScans();
		chromatogram.setScanInterval(scanInterval);
	}

	private List<IIntegrationEntry> readIntegrationEntries(DataInputStream dataInputStream) throws IOException {

		List<IIntegrationEntry> integrationEntries = new ArrayList<>();
		int numberOfIntegrationEntries = dataInputStream.readInt(); // Number Integration Entries
		for(int i = 1; i <= numberOfIntegrationEntries; i++) {
			double integratedArea = dataInputStream.readDouble(); // Integrated Area
			IIntegrationEntry integrationEntry = new IntegrationEntry(integratedArea);
			integrationEntries.add(integrationEntry);
		}
		return integrationEntries;
	}

	private boolean isValidFileFormat(ZipFile zipFile) throws IOException {

		boolean isValid = false;
		DataInputStream dataInputStream = getDataInputStream(zipFile, Format.FILE_VERSION);
		String version = readString(dataInputStream);
		if(version.equals(Format.CHROMATOGRAM_VERSION_1001)) {
			isValid = true;
		}
		//
		dataInputStream.close();
		//
		return isValid;
	}
}
