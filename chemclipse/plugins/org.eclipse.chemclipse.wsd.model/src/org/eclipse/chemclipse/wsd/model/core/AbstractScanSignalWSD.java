/*******************************************************************************
 * Copyright (c) 2013, 2015 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.wsd.model.core;

public abstract class AbstractScanSignalWSD implements IScanSignalWSD {

	private int wavelength;
	private float abundance;

	@Override
	public int getWavelength() {

		return wavelength;
	}

	@Override
	public void setWavelength(int wavelength) {

		this.wavelength = wavelength;
	}

	@Override
	public float getAbundance() {

		return abundance;
	}

	@Override
	public void setAbundance(float abundance) {

		this.abundance = abundance;
	}
}
