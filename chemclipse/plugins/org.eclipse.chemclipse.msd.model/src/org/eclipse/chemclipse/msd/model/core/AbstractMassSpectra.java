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
package org.eclipse.chemclipse.msd.model.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eselmeister
 */
public abstract class AbstractMassSpectra implements IMassSpectra {

	private List<IScanMSD> massSpectra;
	private String converterId = "";
	private String name = "";

	/**
	 * Initialize mass spectra and create a new internal mass spectra list.
	 */
	public AbstractMassSpectra() {

		massSpectra = new ArrayList<IScanMSD>();
	}

	// ---------------------------------------------------IMassSpectra
	@Override
	public void addMassSpectrum(IScanMSD massSpectrum) {

		if(massSpectrum != null) {
			massSpectra.add(massSpectrum);
		}
	}

	@Override
	public void removeMassSpectrum(IScanMSD massSpectrum) {

		if(massSpectrum != null) {
			massSpectra.remove(massSpectrum);
		}
	}

	@Override
	public IScanMSD getMassSpectrum(int i) {

		IScanMSD massSpectrum = null;
		if(i > 0 && i <= massSpectra.size()) {
			massSpectrum = massSpectra.get(--i);
		}
		return massSpectrum;
	}

	@Override
	public int size() {

		return massSpectra.size();
	}

	@Override
	public List<IScanMSD> getList() {

		return massSpectra;
	}

	@Override
	public String getConverterId() {

		return converterId;
	}

	@Override
	public void setConverterId(String converterId) {

		this.converterId = converterId;
	}

	@Override
	public String getName() {

		return name;
	}

	@Override
	public void setName(String name) {

		this.name = name;
	}
	// ---------------------------------------------------IMassSpectra
}
