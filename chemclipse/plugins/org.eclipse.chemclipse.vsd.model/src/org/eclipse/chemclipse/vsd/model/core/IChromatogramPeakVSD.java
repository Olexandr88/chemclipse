/*******************************************************************************
 * Copyright (c) 2023 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.vsd.model.core;

import org.eclipse.chemclipse.model.core.IChromatogramPeak;

public interface IChromatogramPeakVSD extends IPeakVSD, IChromatogramPeak {

	IChromatogramVSD getChromatogram();
}