/*******************************************************************************
 * Copyright (c) 2008, 2021 Lablicate GmbH.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.chemclipse.converter.exceptions.FileIsEmptyException;
import org.eclipse.chemclipse.converter.exceptions.FileIsNotReadableException;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.AbstractChromatogram;
import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.msd.converter.io.AbstractMassSpectraReader;
import org.eclipse.chemclipse.msd.converter.io.IMassSpectraReader;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.converter.misc.CompoundInformation;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.converter.misc.ConverterCID;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.model.IVendorLibraryMassSpectrum;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.model.VendorLibraryMassSpectrum;
import org.eclipse.chemclipse.msd.converter.supplier.amdis.preferences.PreferenceSupplier;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.IMassSpectra;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.implementation.Ion;
import org.eclipse.chemclipse.msd.model.implementation.MassSpectra;
import org.eclipse.core.runtime.IProgressMonitor;

public class MSLReader extends AbstractMassSpectraReader implements IMassSpectraReader {

	private static final Logger logger = Logger.getLogger(MSLReader.class);
	//
	private static final String CONVERTER_ID = "org.eclipse.chemclipse.msd.converter.supplier.amdis.massspectrum.msl";
	/**
	 * Pre-compile all patterns to be a little bit faster.
	 */
	private static final Pattern NAME = Pattern.compile("(NAME:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern COMMENTS = Pattern.compile("(COMMENT:|COMMENTS:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CAS = Pattern.compile("(CAS(NO|#)?:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DB_NAME = Pattern.compile("(DB(NO|#)?:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern REFERENCE_IDENTIFIER = Pattern.compile("(REFID:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern SMILES = Pattern.compile("(SMILES:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern RETENTION_TIME = Pattern.compile("(RT:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern RELATIVE_RETENTION_TIME = Pattern.compile("(RRT:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern RETENTION_INDEX = Pattern.compile("(RI:)(.*)", Pattern.CASE_INSENSITIVE);
	private static final Pattern DATA = Pattern.compile("(.*)(Num Peaks:)(\\s*)(\\d*)(.*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern IONS = Pattern.compile("([+]?\\d+\\.?\\d*)(\\s+)([+-]?\\d+\\.?\\d*([eE][+-]?\\d+)?)"); // "(\\d+)(\\s+)(\\d+)" or "(\\d+)(\\s+)([+-]?\\d+\\.?\\d*([eE][+-]?\\d+)?)"
	//
	private static final String CHARSET_US = "US-ASCII";
	private static final String RETENTION_INDICES_DELIMITER = ", ";
	private static final String LINE_DELIMITER = "\r\n";

	@Override
	public IMassSpectra read(File file, IProgressMonitor monitor) throws FileNotFoundException, FileIsNotReadableException, FileIsEmptyException, IOException {

		List<String> massSpectraData = getMassSpectraData(file);
		//
		IMassSpectra massSpectra = extractMassSpectra(massSpectraData);
		massSpectra.setConverterId(CONVERTER_ID);
		massSpectra.setName(file.getName());
		/*
		 * Compound Information (*.CID)
		 */
		if(PreferenceSupplier.isParseCompoundInformation()) {
			File fileCID = ConverterCID.getFileCID(file);
			if(fileCID != null) {
				List<CompoundInformation> compoundList = ConverterCID.convert(fileCID);
				ConverterCID.transfer(compoundList, massSpectra);
			}
		}
		//
		return massSpectra;
	}

	/**
	 * Extracts the mass spectrum from the given text.
	 * 
	 * @param massSpectrumData
	 * @return {@link IVendorLibraryMassSpectrum}
	 */
	protected IVendorLibraryMassSpectrum extractMassSpectrum(String massSpectrumData) {

		return extractMassSpectrum(massSpectrumData, "", "");
	}

	/**
	 * Extracts the mass spectrum from the given text.
	 * 
	 * @param massSpectrumData
	 * @param referenceIdentifierMarker
	 * @param referenceIdentifierPrefix
	 * @return {@link IVendorLibraryMassSpectrum}
	 */
	protected IVendorLibraryMassSpectrum extractMassSpectrum(String massSpectrumData, String referenceIdentifierMarker, String referenceIdentifierPrefix) {

		IVendorLibraryMassSpectrum massSpectrum = new VendorLibraryMassSpectrum();
		/*
		 * Extract name and reference identifier.
		 * Additionally, add the reference identifier if it is stored as a pattern.
		 */
		String name = extractContentAsString(massSpectrumData, NAME, 2);
		extractNameAndReferenceIdentifier(massSpectrum, name, referenceIdentifierMarker, referenceIdentifierPrefix);
		String referenceIdentifier = extractContentAsString(massSpectrumData, REFERENCE_IDENTIFIER, 2) + massSpectrum.getLibraryInformation().getReferenceIdentifier();
		massSpectrum.getLibraryInformation().setReferenceIdentifier(referenceIdentifier);
		//
		String comments = extractContentAsString(massSpectrumData, COMMENTS, 2);
		massSpectrum.getLibraryInformation().setComments(comments);
		String casNumber = extractContentAsString(massSpectrumData, CAS, 3);
		massSpectrum.getLibraryInformation().setCasNumber(casNumber);
		String database = extractContentAsString(massSpectrumData, DB_NAME, 3);
		massSpectrum.getLibraryInformation().setDatabase(database);
		String smiles = extractContentAsString(massSpectrumData, SMILES, 2);
		massSpectrum.getLibraryInformation().setSmiles(smiles);
		int retentionTime = extractContentAsInt(massSpectrumData, RETENTION_TIME, 2);
		massSpectrum.setRetentionTime(retentionTime);
		int relativeRetentionTime = extractContentAsInt(massSpectrumData, RELATIVE_RETENTION_TIME, 2);
		massSpectrum.setRelativeRetentionTime(relativeRetentionTime);
		String retentionIndices = extractContentAsString(massSpectrumData, RETENTION_INDEX, 2);
		extractRetentionIndices(massSpectrum, retentionIndices, RETENTION_INDICES_DELIMITER);
		/*
		 * Extracts all ions and stored them.
		 */
		extractIons(massSpectrum, massSpectrumData);
		//
		return massSpectrum;
	}

	/**
	 * Returns a list of mass spectra data.
	 * 
	 * @throws IOException
	 */
	private List<String> getMassSpectraData(File file) throws IOException {

		Charset charset = Charset.forName(CHARSET_US);
		List<String> massSpectraData = new ArrayList<String>();
		//
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = bufferedReader.readLine()) != null) {
				/*
				 * The mass spectra are divided by empty lines. If the builder has
				 * at least 1 char, then add a new potential mass spectrum to the
				 * mass spectra data list. Don't forget to build a new
				 * StringBuilder. In all other cases append the found lines to the
				 * StringBuilder.
				 */
				if(line.length() == 0) {
					addMassSpectrumData(builder, massSpectraData);
					builder = new StringBuilder();
				} else {
					builder.append(line);
					builder.append(LINE_DELIMITER);
				}
			}
			/*
			 * Don't forget to add the last mass spectrum.
			 */
			addMassSpectrumData(builder, massSpectraData);
			bufferedReader.close();
		}
		//
		return massSpectraData;
	}

	/**
	 * Adds the content from the StringBuilder to the mass spectra data list, if
	 * the length is > 0.
	 * 
	 * @param builder
	 * @param massSpectraData
	 */
	private void addMassSpectrumData(StringBuilder builder, List<String> massSpectraData) {

		String massSpectrumData;
		if(builder.length() > 0) {
			massSpectrumData = builder.toString();
			massSpectraData.add(massSpectrumData);
		}
	}

	/**
	 * Returns a mass spectra object or null, if something has gone wrong.
	 * 
	 * @param massSpectraData
	 * @return IMassSpectra
	 */
	private IMassSpectra extractMassSpectra(List<String> massSpectraData) {

		IMassSpectra massSpectra = new MassSpectra();
		String referenceIdentifierMarker = org.eclipse.chemclipse.msd.converter.preferences.PreferenceSupplier.getReferenceIdentifierMarker();
		String referenceIdentifierPrefix = org.eclipse.chemclipse.msd.converter.preferences.PreferenceSupplier.getReferenceIdentifierPrefix();
		/*
		 * Iterates through the saved mass spectrum text data and converts it to
		 * a mass spectrum.
		 */
		for(String massSpectrumData : massSpectraData) {
			addMassSpectrum(massSpectra, massSpectrumData, referenceIdentifierMarker, referenceIdentifierPrefix);
		}
		return massSpectra;
	}

	/**
	 * Detect a mass spectrum and add it to the given mass spectra.
	 * 
	 * @param massSpectra
	 * @param massSpectrumData
	 */
	private void addMassSpectrum(IMassSpectra massSpectra, String massSpectrumData, String referenceIdentifierMarker, String referenceIdentifierPrefix) {

		/*
		 * Store the mass spectrum in mass spectra if there is at least 1 mass
		 * fragment.
		 */
		IVendorLibraryMassSpectrum massSpectrum = extractMassSpectrum(massSpectrumData, referenceIdentifierMarker, referenceIdentifierPrefix);
		if(massSpectrum.getNumberOfIons() > 0) {
			massSpectra.addMassSpectrum(massSpectrum);
		}
	}

	/**
	 * Extracts all ion from the given mass spectrum data and stores
	 * them in the given mass spectrum.
	 * 
	 * @param massSpectrum
	 * @param massSpectrumData
	 */
	private void extractIons(IVendorLibraryMassSpectrum massSpectrum, String massSpectrumData) {

		String ionData = "";
		Matcher data = DATA.matcher(massSpectrumData);
		data.find();
		if(data.matches()) {
			ionData = data.group(5);
		}
		//
		IIon amdisIon = null;
		double ion;
		float abundance;
		Matcher ions = IONS.matcher(ionData);
		while(ions.find()) {
			try {
				/*
				 * Get the ion and abundance values.
				 */
				ion = Double.parseDouble(ions.group(1));
				abundance = Float.parseFloat(ions.group(3));
				/*
				 * Create the ion and store it in mass spectrum.
				 */
				if(abundance > 0) {
					amdisIon = new Ion(ion, abundance);
					massSpectrum.addIon(amdisIon);
				}
			} catch(AbundanceLimitExceededException e) {
				logger.warn(e);
			} catch(IonLimitExceededException e) {
				logger.warn(e);
			}
		}
	}

	/**
	 * Extracts the content from the given mass spectrum string defined by the
	 * given pattern.
	 * 
	 * @param massSpectrumData
	 * @return String
	 */
	private String extractContentAsString(String massSpectrumData, Pattern pattern, int group) {

		String content = "";
		Matcher matcher = pattern.matcher(massSpectrumData);
		if(matcher.find()) {
			content = matcher.group(group).trim();
		}
		return content.replace("\0", " ");
	}

	/**
	 * Extracts the content from the given mass spectrum string defined by the
	 * given pattern.
	 * 
	 * @param massSpectrumData
	 * @return int
	 */
	private int extractContentAsInt(String massSpectrumData, Pattern pattern, int group) {

		int content = 0;
		try {
			Matcher matcher = pattern.matcher(massSpectrumData);
			if(matcher.find()) {
				content = (int)(Double.parseDouble(matcher.group(group).trim()) * AbstractChromatogram.MINUTE_CORRELATION_FACTOR);
			}
		} catch(Exception e) {
			logger.warn(e);
		}
		return content;
	}
}
