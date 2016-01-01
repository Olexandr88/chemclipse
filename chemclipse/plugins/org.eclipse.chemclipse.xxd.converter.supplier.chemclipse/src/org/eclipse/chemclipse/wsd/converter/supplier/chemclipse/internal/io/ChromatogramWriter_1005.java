/*******************************************************************************
 * Copyright (c) 2015, 2016 michaelchang.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * michaelchang - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.wsd.converter.supplier.chemclipse.internal.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.chemclipse.converter.exceptions.FileIsNotWriteableException;
import org.eclipse.chemclipse.converter.io.AbstractChromatogramWriter;
import org.eclipse.chemclipse.model.baseline.IBaselineModel;
import org.eclipse.chemclipse.model.core.IMethod;
import org.eclipse.chemclipse.support.history.IEditHistory;
import org.eclipse.chemclipse.support.history.IEditInformation;
import org.eclipse.chemclipse.wsd.converter.io.IChromatogramWSDWriter;
import org.eclipse.chemclipse.wsd.model.core.IChromatogramWSD;
import org.eclipse.chemclipse.wsd.model.core.IScanSignalWSD;
import org.eclipse.chemclipse.wsd.model.core.IScanWSD;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IConstants;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IFormat;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.preferences.PreferenceSupplier;
import org.eclipse.core.runtime.IProgressMonitor;

public class ChromatogramWriter_1005 extends AbstractChromatogramWriter implements IChromatogramWSDWriter {

	@Override
	public void writeChromatogram(File file, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws FileNotFoundException, FileIsNotWriteableException, IOException {

		monitor.subTask(IConstants.EXPORT_CHROMATOGRAM);
		/*
		 * ZIP
		 */
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
		zipOutputStream.setLevel(PreferenceSupplier.getCompressionLevel());
		zipOutputStream.setMethod(IFormat.METHOD);
		/*
		 * Write the data
		 */
		writeVersion(zipOutputStream, monitor);
		writeOverviewFolder(zipOutputStream, chromatogram, monitor);
		writeChromatogramFolder(zipOutputStream, chromatogram, monitor);
		/*
		 * Flush and close the output stream.
		 */
		zipOutputStream.flush();
		zipOutputStream.close();
	}

	private void writeVersion(ZipOutputStream zipOutputStream, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Version
		 */
		zipEntry = new ZipEntry(IFormat.FILE_VERSION);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		String version = IFormat.VERSION_1005;
		dataOutputStream.writeInt(version.length()); // Length Version
		dataOutputStream.writeChars(version); // Version
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeOverviewFolder(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Create the overview folder
		 */
		zipEntry = new ZipEntry(IFormat.DIR_OVERVIEW_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		zipOutputStream.closeEntry();
		/*
		 * TIC
		 */
		zipEntry = new ZipEntry(IFormat.FILE_TIC_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		int scans = chromatogram.getNumberOfScans();
		dataOutputStream.writeInt(scans); // Number of Scans
		// Retention Times - Total Signals
		for(int scan = 1; scan <= scans; scan++) {
			monitor.subTask(IConstants.EXPORT_SCANS + scan);
			IScanWSD scanWsd = chromatogram.getSupplierScan(scan);
			int scanSignalTotal = scanWsd.getScanSignals().size();
			dataOutputStream.writeInt(scanSignalTotal);
			for(int signal = 0; signal < scanSignalTotal; signal++) {
				IScanSignalWSD scanSignal = scanWsd.getScanSignal(signal);
				int wavelength = scanSignal.getWavelength();
				float abundance = scanSignal.getAbundance();
				dataOutputStream.writeInt(wavelength);
				dataOutputStream.writeFloat(abundance);
			}
			int retentionTime = chromatogram.getSupplierScan(scan).getRetentionTime();
			dataOutputStream.writeInt(retentionTime); // Retention Time
			dataOutputStream.writeFloat(chromatogram.getSupplierScan(scan).getRetentionIndex()); // Retention Index
			dataOutputStream.writeFloat(chromatogram.getSupplierScan(scan).getTotalSignal()); // Total Signal
			dataOutputStream.writeInt(chromatogram.getSupplierScan(scan).getTimeSegmentId()); // Time Segment Id
			dataOutputStream.writeInt(chromatogram.getSupplierScan(scan).getCycleNumber()); // Cycle Number
		}
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeChromatogramFolder(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		// the things we want in chromatogram folder rn ==> SCANS, MISC, HISTORY, BASELINE.
		// others might be added later
		//
		ZipEntry zipEntry;
		/*
		 * Create the chromatogram folder
		 */
		zipEntry = new ZipEntry(IFormat.DIR_CHROMATOGRAM_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		zipOutputStream.closeEntry();
		/*
		 * WRITE THE FILES
		 */
		writeChromatogramMethod(zipOutputStream, chromatogram, monitor);
		writeChromatogramScans(zipOutputStream, chromatogram, monitor);
		writeChromatogramBaseline(zipOutputStream, chromatogram, monitor);
		writeChromatogramHistory(zipOutputStream, chromatogram, monitor);
		writeChromatogramMiscellaneous(zipOutputStream, chromatogram, monitor);
	}

	private void writeChromatogramMethod(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Edit-History
		 */
		zipEntry = new ZipEntry(IFormat.FILE_SYSTEM_SETTINGS_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		IMethod method = chromatogram.getMethod();
		//
		writeString(dataOutputStream, method.getInstrumentName());
		writeString(dataOutputStream, method.getIonSource());
		dataOutputStream.writeDouble(method.getSamplingRate());
		dataOutputStream.writeInt(method.getSolventDelay());
		dataOutputStream.writeDouble(method.getSourceHeater());
		writeString(dataOutputStream, method.getStopMode());
		dataOutputStream.writeInt(method.getStopTime());
		dataOutputStream.writeInt(method.getTimeFilterPeakWidth());
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeChromatogramScans(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		zipEntry = new ZipEntry(IFormat.FILE_SCANS_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		int scans = chromatogram.getNumberOfScans();
		dataOutputStream.writeInt(scans);
		for(int scan = 1; scan <= scans; scan++) {
			monitor.subTask(IConstants.EXPORT_SCANS + scan);
			IScanWSD scanWsd = chromatogram.getSupplierScan(scan);
			int scanSignalTotal = scanWsd.getScanSignals().size();
			dataOutputStream.writeInt(scanSignalTotal);
			for(int signal = 0; signal < scanSignalTotal; signal++) {
				IScanSignalWSD scanSignal = scanWsd.getScanSignal(signal);
				int wavelength = scanSignal.getWavelength();
				float abundance = scanSignal.getAbundance();
				dataOutputStream.writeInt(wavelength);
				dataOutputStream.writeFloat(abundance);
			}
			int retentionTime = chromatogram.getSupplierScan(scan).getRetentionTime();
			dataOutputStream.writeInt(retentionTime); // Retention Time
			dataOutputStream.writeFloat(chromatogram.getSupplierScan(scan).getRetentionIndex()); // Retention Index
			dataOutputStream.writeFloat(chromatogram.getSupplierScan(scan).getTotalSignal()); // Total Signal
			dataOutputStream.writeInt(chromatogram.getSupplierScan(scan).getTimeSegmentId()); // Time Segment Id
			dataOutputStream.writeInt(chromatogram.getSupplierScan(scan).getCycleNumber()); // Cycle Number
		}
		// clean up flush the stream and close zip-entry 1
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeChromatogramBaseline(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Baseline
		 */
		zipEntry = new ZipEntry(IFormat.FILE_BASELINE_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		int scans = chromatogram.getNumberOfScans();
		dataOutputStream.writeInt(scans); // Number of Scans
		//
		IBaselineModel baselineModel = chromatogram.getBaselineModel();
		// Scans
		for(int scan = 1; scan <= scans; scan++) {
			monitor.subTask(IConstants.EXPORT_BASELINE + scan);
			int retentionTime = chromatogram.getSupplierScan(scan).getRetentionTime();
			float backgroundAbundance = baselineModel.getBackgroundAbundance(retentionTime);
			dataOutputStream.writeInt(retentionTime); // Retention Time
			dataOutputStream.writeFloat(backgroundAbundance); // Background Abundance
		}
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeChromatogramHistory(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Edit-History
		 */
		zipEntry = new ZipEntry(IFormat.FILE_HISTORY_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		IEditHistory editHistory = chromatogram.getEditHistory();
		dataOutputStream.writeInt(editHistory.getHistoryList().size()); // Number of entries
		// Date, Description
		for(IEditInformation editInformation : editHistory.getHistoryList()) {
			dataOutputStream.writeLong(editInformation.getDate().getTime()); // Date
			writeString(dataOutputStream, editInformation.getDescription()); // Description
		}
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeChromatogramMiscellaneous(ZipOutputStream zipOutputStream, IChromatogramWSD chromatogram, IProgressMonitor monitor) throws IOException {

		ZipEntry zipEntry;
		DataOutputStream dataOutputStream;
		/*
		 * Miscellaneous
		 */
		zipEntry = new ZipEntry(IFormat.FILE_MISC_WSD);
		zipOutputStream.putNextEntry(zipEntry);
		dataOutputStream = new DataOutputStream(zipOutputStream);
		//
		dataOutputStream.writeLong(chromatogram.getDate().getTime()); // Date
		writeString(dataOutputStream, chromatogram.getMiscInfo()); // Miscellaneous Info
		writeString(dataOutputStream, chromatogram.getOperator()); // Operator
		//
		dataOutputStream.flush();
		zipOutputStream.closeEntry();
	}

	private void writeString(DataOutputStream dataOutputStream, String value) throws IOException {

		dataOutputStream.writeInt(value.length()); // Value Length
		dataOutputStream.writeChars(value); // Value
	}
}
