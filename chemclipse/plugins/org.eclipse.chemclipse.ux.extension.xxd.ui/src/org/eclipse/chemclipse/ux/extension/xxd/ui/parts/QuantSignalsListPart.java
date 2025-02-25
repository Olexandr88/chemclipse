/*******************************************************************************
 * Copyright (c) 2018, 2024 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.parts;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.chemclipse.model.quantitation.IQuantitationCompound;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.ux.extension.xxd.ui.swt.ExtendedQuantSignalsListUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class QuantSignalsListPart extends AbstractPart<ExtendedQuantSignalsListUI> {

	private static final String TOPIC = IChemClipseEvents.TOPIC_QUANT_DB_COMPOUND_UPDATE;

	@Inject
	public QuantSignalsListPart(Composite parent) {

		super(parent, TOPIC);
	}

	@Override
	protected ExtendedQuantSignalsListUI createControl(Composite parent) {

		return new ExtendedQuantSignalsListUI(parent, SWT.NONE);
	}

	@Override
	protected boolean updateData(List<Object> objects, String topic) {

		if(objects.size() == 1) {
			Object object = objects.get(0);
			if(object instanceof IQuantitationCompound quantitationCompound) {
				getControl().update(quantitationCompound);
				return true;
			} else {
				getControl().update(null);
				return false;
			}
		}
		//
		return false;
	}

	@Override
	protected boolean isUpdateTopic(String topic) {

		return TOPIC.equals(topic);
	}
}
