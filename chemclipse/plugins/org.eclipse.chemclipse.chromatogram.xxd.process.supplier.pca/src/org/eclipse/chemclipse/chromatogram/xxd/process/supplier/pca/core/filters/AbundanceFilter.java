/*******************************************************************************
 * Copyright (c) 2017 Jan Holy.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jan Holy - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IPcaResults;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISample;

public class AbundanceFilter implements IFilter {

	final static public int ALL_VALUE = 0;
	final static public int ANY_VALUE = 1;
	final static public int LIMIT_GREATER_THEN = 0;
	final static public int LIMIT_LESS_THEN = 1;
	private Function<Double, Boolean> comparator;
	private int filterType;
	private Function<Double, Boolean> gt = d -> d > this.limitValue;
	private int limitType;
	private double limitValue;
	private Function<Double, Boolean> lt = d -> d < this.limitValue;
	final private String name = "Abundance filter";
	private boolean onlySelected;

	public AbundanceFilter() {
		onlySelected = true;
		comparator = gt;
		filterType = ALL_VALUE;
		limitType = LIMIT_GREATER_THEN;
		limitValue = 0;
		onlySelected = true;
	}

	@Override
	public List<Boolean> filter(IPcaResults pcaResults) {

		List<ISample> selectedSamples = pcaResults.getSampleList().stream().filter(s -> s.isSelected() || !onlySelected).collect(Collectors.toList());
		List<Boolean> selection = new ArrayList<>();
		for(int i = 0; i < selectedSamples.get(0).getSampleData().size(); i++) {
			final int index = i;
			boolean b;
			DoubleStream stream = selectedSamples.stream().mapToDouble(s -> s.getSampleData().get(index).getNormalizedData());
			if(filterType == ALL_VALUE) {
				b = stream.allMatch(d -> comparator.apply(d));
			} else {
				b = stream.anyMatch(d -> comparator.apply(d));
			}
			selection.add(b);
		}
		return selection;
	}

	@Override
	public String getDescription() {

		return "";
	}

	public int getFilterType() {

		return filterType;
	}

	public int getLimitType() {

		return limitType;
	}

	public double getLimitValue() {

		return limitValue;
	}

	@Override
	public String getName() {

		return name;
	}

	@Override
	public boolean isOnlySelected() {

		return onlySelected;
	}

	public void setFilterType(int filterType) {

		if(filterType == ALL_VALUE || filterType == ANY_VALUE) {
			this.filterType = filterType;
		}
	}

	public void setlimitType(int limitType) {

		switch(limitType) {
			case LIMIT_GREATER_THEN:
				comparator = gt;
				break;
			case LIMIT_LESS_THEN:
				comparator = lt;
				break;
			default:
				return;
		}
		this.limitType = limitType;
	}

	public void setLimitValue(double limitValue) {

		this.limitValue = limitValue;
	}

	@Override
	public void setOnlySelected(boolean onlySelected) {

		this.onlySelected = onlySelected;
	}
}
