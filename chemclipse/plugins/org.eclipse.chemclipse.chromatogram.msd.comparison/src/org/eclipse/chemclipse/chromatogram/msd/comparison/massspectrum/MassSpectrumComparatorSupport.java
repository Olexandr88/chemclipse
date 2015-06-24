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
package org.eclipse.chemclipse.chromatogram.msd.comparison.massspectrum;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.chemclipse.chromatogram.msd.comparison.exceptions.NoMassSpectrumComparatorAvailableException;

/**
 * This class gives you the information about the registered mass spectra
 * comparators.<br/>
 * A mass spectrum comparator can be used to determine the match quality of two
 * different mass spectra.
 * 
 * @author eselmeister
 */
public class MassSpectrumComparatorSupport implements IMassSpectrumComparatorSupport {

	private List<IMassSpectrumComparisonSupplier> suppliers;

	public MassSpectrumComparatorSupport() {

		suppliers = new ArrayList<IMassSpectrumComparisonSupplier>();
	}

	/**
	 * Adds a ({@link IMassSpectrumComparator}) to the {@link MassSpectrumComparatorSupport}.
	 * 
	 * @param supplier
	 */
	protected void add(final IMassSpectrumComparisonSupplier supplier) {

		suppliers.add(supplier);
	}

	@Override
	public String getConverterId(int index) throws NoMassSpectrumComparatorAvailableException {

		/*
		 * Test if the suppliers ArrayList is empty.
		 */
		areConvertersStored();
		/*
		 * Test if the index is out of range.
		 */
		if(index < 0 || index > suppliers.size() - 1) {
			throw new NoMassSpectrumComparatorAvailableException("There is no mass spectrum comparator available with the following id: " + index + ".");
		}
		IMassSpectrumComparisonSupplier supplier = suppliers.get(index);
		return supplier.getId();
	}

	@Override
	public IMassSpectrumComparisonSupplier getMassSpectrumComparisonSupplier(String converterId) throws NoMassSpectrumComparatorAvailableException {

		IMassSpectrumComparisonSupplier comparisonSupplier = null;
		/*
		 * Test if the suppliers ArrayList is empty.
		 */
		areConvertersStored();
		/*
		 * Test if the index is out of range.
		 */
		if(converterId == null || converterId.equals("")) {
			throw new NoMassSpectrumComparatorAvailableException("There is no mass spectrum comparator available with the following id: " + converterId + ".");
		}
		endsearch:
		for(IMassSpectrumComparisonSupplier supplier : suppliers) {
			if(supplier.getId().equals(converterId)) {
				comparisonSupplier = supplier;
				break endsearch;
			}
		}
		if(comparisonSupplier == null) {
			throw new NoMassSpectrumComparatorAvailableException("There is no mass spectrum comparator available with the following id: " + converterId + ".");
		} else {
			return comparisonSupplier;
		}
	}

	@Override
	public List<String> getAvailableComparatorIds() throws NoMassSpectrumComparatorAvailableException {

		/*
		 * Test if the suppliers ArrayList is empty.
		 */
		areConvertersStored();
		List<String> availableConverters = new ArrayList<String>();
		for(IMassSpectrumComparisonSupplier supplier : suppliers) {
			availableConverters.add(supplier.getId());
		}
		return availableConverters;
	}

	public String[] getComparatorNames() throws NoMassSpectrumComparatorAvailableException {

		/*
		 * Test if the suppliers ArrayList is empty.
		 */
		areConvertersStored();
		/*
		 * If the ArrayList is not empty, return the registered chromatogram
		 * converter filter names.<br/>
		 */
		ArrayList<String> comparatorNames = new ArrayList<String>();
		for(IMassSpectrumComparisonSupplier supplier : suppliers) {
			comparatorNames.add(supplier.getComparatorName());
		}
		return comparatorNames.toArray(new String[comparatorNames.size()]);
	}

	// -------------------------------------private methods
	/**
	 * Check if there are comparators stored in the
	 * ArrayList<IMassSpectrumComparisonSupplier>.
	 * 
	 * @throws NoMassSpectrumComparatorAvailableException
	 */
	private void areConvertersStored() throws NoMassSpectrumComparatorAvailableException {

		if(suppliers.size() < 1) {
			throw new NoMassSpectrumComparatorAvailableException();
		}
	}
	// -------------------------------------private methods
}
