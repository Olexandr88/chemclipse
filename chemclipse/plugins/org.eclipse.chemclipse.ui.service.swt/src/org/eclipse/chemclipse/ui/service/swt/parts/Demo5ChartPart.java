/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ui.service.swt.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.chemclipse.ui.service.swt.impl.Demo5Chart;
import org.eclipse.chemclipse.ui.service.swt.impl.IChart;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class Demo5ChartPart {

	@Inject
	private Composite parent;
	private IChart chart;

	@PostConstruct
	public void postConstruct() {

		chart = new Demo5Chart(parent, SWT.BORDER);
	}

	@PreDestroy
	public void preDestroy() {

	}

	@Focus
	public void setFocus() {

		chart.setFocus();
	}
}
