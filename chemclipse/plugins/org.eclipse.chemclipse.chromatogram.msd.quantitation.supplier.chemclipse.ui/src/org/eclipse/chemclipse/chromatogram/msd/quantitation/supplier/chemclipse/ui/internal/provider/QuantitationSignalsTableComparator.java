/*******************************************************************************
 * Copyright (c) 2013, 2018 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.quantitation.supplier.chemclipse.ui.internal.provider;

import org.eclipse.chemclipse.model.quantitation.IQuantitationSignal;
import org.eclipse.chemclipse.support.ui.swt.AbstractRecordTableComparator;
import org.eclipse.chemclipse.support.ui.swt.IRecordTableComparator;
import org.eclipse.jface.viewers.Viewer;

public class QuantitationSignalsTableComparator extends AbstractRecordTableComparator implements IRecordTableComparator {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		int sortOrder = 0;
		if(e1 instanceof IQuantitationSignal && e2 instanceof IQuantitationSignal) {
			IQuantitationSignal entry1 = (IQuantitationSignal)e1;
			IQuantitationSignal entry2 = (IQuantitationSignal)e2;
			//
			switch(getPropertyIndex()) {
				case 0: // Ion
					sortOrder = Double.compare(entry2.getSignal(), entry1.getSignal());
					break;
				case 1: // Relative Response
					sortOrder = Float.compare(entry2.getRelativeResponse(), entry1.getRelativeResponse());
					break;
				case 2: // Uncertainty
					sortOrder = Double.compare(entry2.getUncertainty(), entry1.getUncertainty());
					break;
				case 3: // Use
					sortOrder = Boolean.valueOf(entry2.isUse()).compareTo(entry1.isUse());
					break;
				default:
					sortOrder = 0;
			}
		}
		if(getDirection() == ASCENDING) {
			sortOrder = -sortOrder;
		}
		return sortOrder;
	}
}
