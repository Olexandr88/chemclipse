/*******************************************************************************
 * Copyright (c) 2021, 2023 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Matthias Mailänder - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.chemclipse.converter.io.AbstractChromatogramReader;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.msd.converter.io.IChromatogramMSDReader;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.converter.model.IVendorChromatogram;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.converter.model.VendorChromatogram;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.converter.BinaryReader110;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.converter.XmlReader;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.converter.XmlReader110;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.BinaryDataArrayType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.CVParamType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.ChromatogramType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.DataProcessingType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.InstrumentConfigurationType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.MzMLType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.ParamGroupType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.PrecursorType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.ProcessingMethodType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.RunType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.SampleListType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.SampleType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.ScanType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.SoftwareType;
import org.eclipse.chemclipse.msd.converter.supplier.mzml.internal.v110.model.SpectrumType;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IIonTransition;
import org.eclipse.chemclipse.msd.model.core.IIonTransitionGroup;
import org.eclipse.chemclipse.msd.model.core.IIonTransitionSettings;
import org.eclipse.chemclipse.msd.model.core.IVendorMassSpectrum;
import org.eclipse.chemclipse.msd.model.implementation.IonTransition;
import org.eclipse.chemclipse.msd.model.implementation.VendorMassSpectrum;
import org.eclipse.chemclipse.support.history.EditInformation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;

public class ChromatogramReaderVersion110 extends AbstractChromatogramReader implements IChromatogramMSDReader {

	private static final Logger logger = Logger.getLogger(ChromatogramReaderVersion110.class);

	@Override
	public IChromatogramOverview readOverview(File file, IProgressMonitor monitor) throws IOException {

		IVendorChromatogram chromatogram = null;
		double[] retentionTimes = null;
		double[] intensities = null;
		//
		try {
			chromatogram = new VendorChromatogram();
			//
			RunType run = XmlReader110.getMzML(file).getRun();
			for(ChromatogramType chromatogramType : run.getChromatogramList().getChromatogram()) {
				if(chromatogramType.getId().equals("TIC")) {
					if(chromatogramType.getCvParam().stream().anyMatch(n -> n.getAccession().equals("MS:1000235") && n.getName().equals("total ion current chromatogram"))) {
						for(BinaryDataArrayType binaryDataArrayType : chromatogramType.getBinaryDataArrayList().getBinaryDataArray()) {
							Pair<String, double[]> binaryData = BinaryReader110.parseBinaryData(binaryDataArrayType);
							if(binaryData.getKey().equals("time")) {
								retentionTimes = binaryData.getValue();
							} else if(binaryData.getKey().equals("intensity")) {
								intensities = binaryData.getValue();
							}
						}
					}
				}
			}
			XmlReader.addIons(intensities, retentionTimes, chromatogram);
		} catch(JAXBException e) {
			logger.warn(e);
		} catch(SAXException e) {
			logger.warn(e);
		} catch(ParserConfigurationException e) {
			logger.warn(e);
		} catch(DataFormatException e) {
			logger.warn(e);
		}
		//
		return chromatogram;
	}

	@Override
	public IChromatogramMSD read(File file, IProgressMonitor monitor) throws IOException {

		IVendorChromatogram chromatogram = null;
		double[] intensities = null;
		double[] mzs = null;
		//
		try {
			chromatogram = new VendorChromatogram();
			chromatogram.setFile(file);
			//
			MzMLType mzML = XmlReader110.getMzML(file);
			for(ParamGroupType contact : mzML.getFileDescription().getContact()) {
				for(CVParamType cvParam : contact.getCvParam()) {
					if(chromatogram.getOperator().isEmpty()) {
						chromatogram.setOperator(cvParam.getValue());
					} else {
						chromatogram.setOperator(String.join(", ", chromatogram.getOperator(), cvParam.getValue()));
					}
				}
			}
			SampleListType sampleList = mzML.getSampleList();
			if(sampleList != null) {
				for(SampleType sample : sampleList.getSample()) {
					chromatogram.setSampleName(sample.getName());
				}
			}
			for(InstrumentConfigurationType instrument : mzML.getInstrumentConfigurationList().getInstrumentConfiguration()) {
				for(CVParamType cvParam : instrument.getCvParam()) {
					if(cvParam.getAccession().equals("MS:1000554")) {
						chromatogram.setInstrument(cvParam.getName());
					}
				}
			}
			for(DataProcessingType dataProcessing : mzML.getDataProcessingList().getDataProcessing()) {
				for(ProcessingMethodType processingMethod : dataProcessing.getProcessingMethod()) {
					SoftwareType software = (SoftwareType)processingMethod.getSoftwareRef();
					for(CVParamType cvParam : processingMethod.getCvParam()) {
						String operation = cvParam.getName();
						String editor = software.getId() + " " + software.getVersion();
						chromatogram.getEditHistory().add(new EditInformation(operation, editor));
					}
				}
			}
			RunType run = mzML.getRun();
			for(SpectrumType spectrum : run.getSpectrumList().getSpectrum()) {
				IVendorMassSpectrum massSpectrum = new VendorMassSpectrum();
				for(CVParamType cvParam : spectrum.getCvParam()) {
					if(cvParam.getAccession().equals("MS:1000127") && cvParam.getName().equals("centroid spectrum")) {
						massSpectrum.setMassSpectrumType((short)0);
					} else if(cvParam.getAccession().equals("MS:1000128") && cvParam.getName().equals("profile spectrum")) {
						massSpectrum.setMassSpectrumType((short)1);
					}
					if(cvParam.getAccession().equals("MS:1000511") && cvParam.getName().equals("ms level")) {
						short msLevel = Short.parseShort(cvParam.getValue());
						massSpectrum.setMassSpectrometer(msLevel);
					}
				}
				for(ScanType scanType : spectrum.getScanList().getScan()) {
					for(CVParamType cvParam : scanType.getCvParam()) {
						if(cvParam.getAccession().equals("MS:1000016") && cvParam.getName().equals("scan start time")) {
							float multiplicator = XmlReader110.getTimeMultiplicator(cvParam);
							int retentionTime = Math.round(Float.parseFloat(cvParam.getValue()) * multiplicator);
							massSpectrum.setRetentionTime(retentionTime);
						}
					}
				}
				if(massSpectrum.isTandemMS()) {
					IIonTransitionSettings ionTransitionSettings = chromatogram.getIonTransitionSettings();
					IIonTransitionGroup ionTransitionGroup = ionTransitionSettings.get(0);
					for(PrecursorType precursorType : spectrum.getPrecursorList().getPrecursor()) {
						double selectedIon = 0;
						double selectedIonPeakIntensity = 0;
						for(ParamGroupType paramGroupType : precursorType.getSelectedIonList().getSelectedIon()) {
							for(CVParamType cvParam : paramGroupType.getCvParam()) {
								if(cvParam.getAccession().equals("MS:1000744") && cvParam.getName().equals("selected ion m/z")) {
									selectedIon = Double.parseDouble(cvParam.getValue());
								}
								if(cvParam.getAccession().equals("MS:1000042") && cvParam.getName().equals("peak intensity")) {
									selectedIonPeakIntensity = Double.parseDouble(cvParam.getValue());
								}
							}
						}
						double collisionEnergy = 0;
						for(CVParamType cvParam : precursorType.getActivation().getCvParam()) {
							if(cvParam.getAccession().equals("MS:1000045") && cvParam.getName().equals("collision energy")) {
								collisionEnergy = Double.parseDouble(cvParam.getValue());
							}
						}
						IIonTransition ionTransition = new IonTransition(selectedIon, selectedIonPeakIntensity, 0, 0, collisionEnergy, 0, 0, 0);
						ionTransitionGroup.add(ionTransition);
					}
				}
				for(BinaryDataArrayType binaryDataArrayType : spectrum.getBinaryDataArrayList().getBinaryDataArray()) {
					Pair<String, double[]> binaryData = BinaryReader110.parseBinaryData(binaryDataArrayType);
					if(binaryData.getKey().equals("m/z")) {
						mzs = binaryData.getValue();
					} else if(binaryData.getKey().equals("intensity")) {
						intensities = binaryData.getValue();
					}
				}
				XmlReader.addIons(intensities, mzs, massSpectrum);
				chromatogram.addScan(massSpectrum);
			}
		} catch(DataFormatException e) {
			logger.warn(e);
		} catch(ParserConfigurationException e) {
			logger.warn(e);
		} catch(SAXException e) {
			logger.warn(e);
		} catch(JAXBException e) {
			logger.warn(e);
		}
		//
		return chromatogram;
	}
}
