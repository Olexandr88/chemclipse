/*******************************************************************************
 * Copyright (c) 2015 Lablicate UG (haftungsbeschränkt).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Janos Binder - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.model.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.INamedScanMSD;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.numeric.statistics.model.AnovaStatistics;
import org.eclipse.chemclipse.numeric.statistics.model.IUnivariateStatistics;
import org.eclipse.chemclipse.numeric.statistics.model.UnivariateStatistics;

public class StatisticsCalculator {

	public Map<Double, IUnivariateStatistics> calculateStatistics(List<IScanMSD> massSpectra, StatisticsInputTypes id) {

		int capacity = massSpectra.size();
		/*
		 * Creating a HashSet for the statistics
		 */
		Map<Double, List<Double>> mzAbundances = new HashMap<Double, List<Double>>();
		switch(id) {
			case STATISTICS_ABUNDANCE:
				for(IScanMSD massSpectrum : massSpectra) {
					for(IIon ion : massSpectrum.getIons()) {
						double mz = ion.getIon();
						double abundance = ion.getAbundance();
						if(mzAbundances.containsKey(mz)) {
							mzAbundances.get(mz).add(abundance);
						} else {
							List<Double> abundances = new ArrayList<Double>(capacity);
							abundances.add(abundance);
							mzAbundances.put(mz, abundances);
						}
					}
				}
				break;
			default:
				// Should we throw here an exception?
				break;
		}
		/*
		 * Calculating multiple statistical values
		 */
		Map<Double, IUnivariateStatistics> valueStatisticsPairs = new HashMap<Double, IUnivariateStatistics>();
		for(Double mz : mzAbundances.keySet()) {
			/*
			 * Unbox the gift, calculate the statistical values in a PeakMassSpectraStatistics object
			 */
			List<Double> abundancesList = mzAbundances.get(mz);
			int sampleSize = abundancesList.size();
			double[] abundances = new double[sampleSize];
			for(int i = 0; i < sampleSize; i++) {
				abundances[i] = abundancesList.get(i).doubleValue();
			}
			UnivariateStatistics statistics = new UnivariateStatistics(abundances);
			/*
			 * Add to the resulting map
			 */
			valueStatisticsPairs.put(mz, statistics);
		}
		return valueStatisticsPairs;
	}

	/*
	 * A JoinedScanMSD knows about the Origin/Substance name, e.g. we group the replicate experiments on the substance name
	 */
	public Map<Double, Collection<double[]>> calculateInputForOneWayAnova(List<INamedScanMSD> groupedMassSpectra) {

		Map<Double, Map<String, List<Double>>> mzSubstancesAbundances = new HashMap<Double, Map<String, List<Double>>>();
		for(INamedScanMSD groupedMassSpectrum : groupedMassSpectra) {
			String substance = groupedMassSpectrum.getSubstanceName();
			for(IIon ion : groupedMassSpectrum.getIons()) {
				double mz = ion.getIon();
				double abundance = ion.getAbundance();
				if(mzSubstancesAbundances.containsKey(mz)) {
					if(mzSubstancesAbundances.get(mz).containsKey(substance)) {
						mzSubstancesAbundances.get(mz).get(substance).add(abundance);
					} else {
						List<Double> abundances = new ArrayList<Double>();
						abundances.add(abundance);
						mzSubstancesAbundances.get(mz).put(substance, abundances);
					}
				} else {
					Map<String, List<Double>> substancesAbundances = new HashMap<String, List<Double>>();
					List<Double> abundances = new ArrayList<Double>();
					abundances.add(abundance);
					substancesAbundances.put(substance, abundances);
					mzSubstancesAbundances.put(mz, substancesAbundances);
				}
			}
		}
		/*
		 * Create the proper data structure for OneWayAnova, maybe we need a hashmap based on {mz,Collection<double[]> - grouped by substance}
		 */
		Map<Double, Collection<double[]>> mzAnovaInputPairs = new HashMap<Double, Collection<double[]>>();
		for(Entry<Double, Map<String, List<Double>>> entry : mzSubstancesAbundances.entrySet()) {
			Double mz = entry.getKey();
			Collection<double[]> anovaInput = new ArrayList<double[]>();
			for(String substance : entry.getValue().keySet()) {
				List<Double> valuesList = entry.getValue().get(substance);
				int size = valuesList.size();
				/*
				 * Handle when the number of samples is 1 => no statistics can be done on it
				 */
				if(size > 1) {
					double[] values = new double[size];
					for(int i = 0; i < size; i++) {
						values[i] = valuesList.get(i);
					}
					anovaInput.add(values);
				}
			}
			/*
			 * Add only if at least two categories exists
			 */
			if(anovaInput.size() > 1) {
				mzAnovaInputPairs.put(mz, anovaInput);
			}
		}
		/*
		 * One can use OneWayAnova.anovaFValue() or OneWayAnova.anovaPValue()
		 */
		return mzAnovaInputPairs;
	}

	public Map<Double, Double> calculateAnovaFValues(Map<Double, Collection<double[]>> mzAnovaInputPairs) {

		OneWayAnova anova = new OneWayAnova();
		Map<Double, Double> mzAnovaFPairs = new HashMap<Double, Double>();
		for(Entry<Double, Collection<double[]>> mzAnovaInputPair : mzAnovaInputPairs.entrySet()) {
			Double mz = mzAnovaInputPair.getKey();
			double fvalue = anova.anovaFValue(mzAnovaInputPair.getValue());
			mzAnovaFPairs.put(mz, fvalue);
		}
		return mzAnovaFPairs;
	}

	public Map<Double, Double> calculateAnovaPValues(Map<Double, Collection<double[]>> mzAnovaInputPairs) {

		OneWayAnova anova = new OneWayAnova();
		Map<Double, Double> mzAnovaPPairs = new HashMap<Double, Double>();
		for(Entry<Double, Collection<double[]>> mzAnovaInputPair : mzAnovaInputPairs.entrySet()) {
			Double mz = mzAnovaInputPair.getKey();
			double pvalue = anova.anovaPValue(mzAnovaInputPair.getValue());
			mzAnovaPPairs.put(mz, pvalue);
		}
		return mzAnovaPPairs;
	}

	public Map<Double, AnovaStatistics> calculateAnovaStatistics(Map<Double, Collection<double[]>> mzAnovaInputPairs) {

		OneWayAnova anova = new OneWayAnova();
		Map<Double, AnovaStatistics> mzanovaStatistics = new HashMap<Double, AnovaStatistics>();
		for(Entry<Double, Collection<double[]>> mzAnovaInputPair : mzAnovaInputPairs.entrySet()) {
			Double mz = mzAnovaInputPair.getKey();
			Collection<double[]> anovaInput = mzAnovaInputPair.getValue();
			double pvalue = anova.anovaPValue(anovaInput);
			double fvalue = anova.anovaFValue(anovaInput);
			AnovaStatistics anovaStatistics = new AnovaStatistics(pvalue, fvalue);
			mzanovaStatistics.put(mz, anovaStatistics);
		}
		return mzanovaStatistics;
	}
}
