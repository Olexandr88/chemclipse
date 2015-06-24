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
package org.eclipse.chemclipse.rcp.app.ui.switcher;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class PerspectiveSwitcher {

	/*
	 * Context and services
	 */
	@Inject
	private MApplication application;
	@Inject
	private EModelService modelService;
	@Inject
	private EPartService partService;

	/**
	 * Try to load the perspective.
	 * 
	 * @param perspectiveId
	 */
	public void changePerspective(String perspectiveId) {

		MUIElement element = modelService.find(perspectiveId, application);
		if(element instanceof MPerspective) {
			MPerspective perspective = (MPerspective)element;
			partService.switchPerspective(perspective);
		}
	}

	/**
	 * Load and show the view.
	 * 
	 * @param viewId
	 */
	public void focusView(String viewId) {

		MUIElement element = modelService.find(viewId, application);
		if(element instanceof MPart) {
			MPart part = (MPart)element;
			if(!partService.getParts().contains(part)) {
				partService.createPart(part.getElementId());
			}
			partService.showPart(part, PartState.ACTIVATE);
		}
	}
}
