/*******************************************************************************
 * Copyright (c) 2015 Dr. Philip Wenig.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.model.identifier;

public class ChromatogramComparisonResult extends AbstractChromatogramComparisonResult implements IChromatogramComparisonResult {

	public ChromatogramComparisonResult(float matchFactor, float reverseMatchFactor, float probability) {

		super(matchFactor, reverseMatchFactor, probability);
	}

	public ChromatogramComparisonResult(float matchFactor, float reverseMatchFactor) {

		super(matchFactor, reverseMatchFactor);
	}
}
