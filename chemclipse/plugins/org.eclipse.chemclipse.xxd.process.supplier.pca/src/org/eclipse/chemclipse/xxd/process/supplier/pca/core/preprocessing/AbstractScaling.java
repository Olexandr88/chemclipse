/*******************************************************************************
 * Copyright (c) 2017, 2023 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jan Holy - initial API and implementation
 * Philip Wenig - refactoring
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.process.supplier.pca.core.preprocessing;

public abstract class AbstractScaling extends AbstractCentering implements IScaling {

	private int centeringType;

	protected AbstractScaling(int centeringType) {

		this.centeringType = centeringType;
	}

	@Override
	public int getCenteringType() {

		return centeringType;
	}

	@Override
	public void setCenteringType(int centeringType) {

		this.centeringType = centeringType;
	}
}
