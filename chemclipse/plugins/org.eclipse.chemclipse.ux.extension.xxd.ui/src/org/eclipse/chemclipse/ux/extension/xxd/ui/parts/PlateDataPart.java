/*******************************************************************************
 * Copyright (c) 2018, 2024 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.parts;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.chemclipse.pcr.model.core.IPlate;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.ux.extension.xxd.ui.swt.ExtendedPlateDataUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class PlateDataPart extends AbstractPart<ExtendedPlateDataUI> {

	private static final String TOPIC = IChemClipseEvents.TOPIC_PLATE_PCR_UPDATE_SELECTION;

	@Inject
	public PlateDataPart(Composite parent) {

		super(parent, TOPIC);
	}

	@Override
	protected ExtendedPlateDataUI createControl(Composite parent) {

		return new ExtendedPlateDataUI(parent, SWT.NONE);
	}

	@Override
	protected boolean updateData(List<Object> objects, String topic) {

		if(objects.size() == 1) {
			if(isCloseEvent(topic)) {
				getControl().update(null);
				return false;
			} else {
				Object object = objects.get(0);
				if(object instanceof IPlate plate) {
					getControl().update(plate);
					return true;
				} else {
					getControl().update(null);
					return true;
				}
			}
		}
		//
		return false;
	}

	@Override
	protected boolean isUpdateTopic(String topic) {

		return isUpdateEvent(topic) || isCloseEvent(topic);
	}

	private boolean isUpdateEvent(String topic) {

		return TOPIC.equals(topic);
	}

	private boolean isCloseEvent(String topic) {

		return IChemClipseEvents.TOPIC_EDITOR_PCR_CLOSE.equals(topic);
	}
}
