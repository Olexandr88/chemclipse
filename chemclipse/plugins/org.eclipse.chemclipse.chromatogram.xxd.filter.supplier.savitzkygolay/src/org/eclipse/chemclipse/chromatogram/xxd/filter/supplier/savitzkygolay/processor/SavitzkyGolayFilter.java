/*******************************************************************************
 * Copyright (c) 2020, 2022 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Lorenz Gerber - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.savitzkygolay.processor;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.eclipse.chemclipse.chromatogram.xxd.filter.supplier.savitzkygolay.preferences.PreferenceSupplier;

public class SavitzkyGolayFilter {

	private final int derivative;
	private final int order;
	private final int width;
	private RealMatrix normalEquations;
	private double[][] weights;
	private double[] filterCoefficients;
	private double[] derivativeCoefficients;
	private double[][] startStopWeights;
	private double[][] uStart;
	private double[][] uStop;

	public SavitzkyGolayFilter(int order, int width, int derivative) {

		// Validate and rectify input
		width = Math.max(PreferenceSupplier.MIN_WIDTH, (1 + 2 * (width - 1) / 2));
		order = (int)StatUtils.min(new double[]{Math.max(0, order), 5, (width - 1)});
		derivative = Math.min(Math.max(0, derivative), order);
		this.derivative = derivative;
		this.order = order;
		this.width = width;
		calculateConvolutionWeights();
		filterCoefficients = weights[derivative];
	}

	public double[] getFactorialAdjustedFilterCoefficients() {

		// coefficients need to be multiplied by factorial(derivative)
		double[] filterCoefficientsAdjusted = new double[filterCoefficients.length];
		for(int i = 0; i < filterCoefficients.length; i++) {
			filterCoefficientsAdjusted[i] = filterCoefficients[i] * CombinatoricsUtils.factorial(derivative);
		}
		return filterCoefficientsAdjusted;
	}

	public double[] apply(double[] ticValues) {

		double[] newTicValues = new double[ticValues.length];
		System.arraycopy(ticValues, 0, newTicValues, 0, ticValues.length);
		processStart(ticValues, newTicValues);
		processMiddle(ticValues, newTicValues);
		processEnd(ticValues, newTicValues);
		return newTicValues;
	}

	private void calculateConvolutionWeights() {

		// Set up the normal equations for the desired least-squares fit
		normalEquations = getNormalEquations(width, order);
		RealMatrix dm = MatrixUtils.createRealIdentityMatrix(width);
		// solve the normal equations
		QRDecomposition qrDecomposition = new QRDecomposition(normalEquations);
		RealMatrix q = qrDecomposition.getQ();
		RealMatrix r2 = qrDecomposition.getR();
		// Get one row of the inverse matrix
		SingularValueDecomposition singularValueDecomposition = new SingularValueDecomposition(normalEquations);
		int r = singularValueDecomposition.getRank();
		RealMatrix q2 = q.getSubMatrix(0, q.getRowDimension() - 1, 0, r - 1);
		RealMatrix r3 = r2.getSubMatrix(0, r - 1, 0, r2.getColumnDimension() - 1);
		RealMatrix weightsCalculation = new LUDecomposition(r3).getSolver().getInverse().multiply(q2.transpose().multiply(dm));
		// Extract into double matrix
		weights = new double[weightsCalculation.getRowDimension()][weightsCalculation.getColumnDimension()];
		for(int i = 0; i < weightsCalculation.getRowDimension(); i++) {
			for(int j = 0; j < weightsCalculation.getColumnDimension(); j++) {
				weights[i][j] = weightsCalculation.getEntry(i, j);
			}
		}
		calculateStartStopCoefficients();
	}

	private void calculateStartStopCoefficients() {

		derivativeCoefficients = calculateCoefficients(derivative, order);
		startStopWeights = new double[order - derivative + 1][weights[0].length]; // rows, columns
		for(int i = derivative, k = 0; i <= order; i++, k++) {
			for(int j = 0; j < weights[0].length; j++) {
				startStopWeights[k][j] = weights[i][j];
			}
		}
		//
		for(int i = 0; i < derivativeCoefficients.length; i++) {
			double coefficient = derivativeCoefficients[i];
			for(int j = 0; j < startStopWeights[i].length; j++) {
				startStopWeights[i][j] *= coefficient;
			}
		}
		int p = (width - 1) / 2;
		uStart = new double[p][order - derivative + 1]; // rows, columns
		for(int i = 0; i < p; i++) {
			for(int j = 0; j < order - derivative + 1; j++) {
				uStart[i][j] = normalEquations.getEntry(i, j);
			}
		}
		uStop = new double[p][order - derivative + 1]; // rows, columns
		for(int i = p + 1, k = 0; i < normalEquations.getRowDimension(); i++, k++) {
			for(int j = 0; j < order - derivative + 1; j++) {
				uStop[k][j] = normalEquations.getEntry(i, j);
			}
		}
	}

	private RealMatrix getNormalEquations(int width, int order) {

		int rows = width;
		int columns = 1 + order;
		int p = (width - 1) / 2;
		double[][] t1 = createT1(rows, columns, -p);
		double[][] t2 = createT2(rows, columns, 0);
		return MatrixUtils.createRealMatrix(generateNormalMatrix(t1, t2));
	}

	private double[][] createT1(int rows, int columns, int min) {

		double[][] array = new double[rows][columns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				array[i][j] = min;
			}
			min++;
		}
		return array;
	}

	private double[][] createT2(int rows, int columns, int min) {

		double[][] array = new double[rows][columns];
		for(int i = 0; i < rows; i++) {
			int value = min;
			for(int j = 0; j < columns; j++) {
				array[i][j] = value++;
			}
		}
		return array;
	}

	private double[][] generateNormalMatrix(double[][] t1, double[][] t2) {

		int rows = t1.length;
		int columns = t1[0].length;
		//
		double[][] array = new double[rows][columns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				array[i][j] = Math.pow(t1[i][j], t2[i][j]);
			}
		}
		return array;
	}

	private double[] calculateCoefficients(int derivative, int order) {

		double[] result;
		if(derivative > 0) {
			/*
			 * Calculate the coefficient.
			 */
			int val1 = order + 1 - derivative;
			//
			double[][] t3 = createOnes(derivative, 1); // t3 one column
			double[] t4 = createArray(val1, 1);
			double[][] t34 = new double[t3.length][t4.length];
			for(int i = 0; i < t3.length; i++) {
				double valt3 = t3[i][0]; // t3 one column
				for(int j = 0; j < t4.length; j++) {
					t34[i][j] = valt3 * t4[j];
				}
			}
			//
			double[] t5 = createArray(derivative, 0);
			double[][] t6 = createOnes(1, val1); // t6 one row
			double[][] t56 = new double[t5.length][t6[0].length];
			for(int i = 0; i < t5.length; i++) {
				double valt5 = t5[i]; // t5 transpose
				for(int j = 0; j < t6[0].length; j++) { // t6 one row
					t56[i][j] = valt5 * t6[0][j]; // t6 one row
				}
			}
			//
			int size = t34[0].length; // size of the columns
			result = new double[size];
			int rows = t34.length;
			int columns = t34[0].length;
			for(int j = 0; j < columns; j++) {
				double product = 1;
				for(int i = 0; i < rows; i++) {
					product *= t34[i][j] + t56[i][j];
				}
				result[j] = product;
			}
			//
		} else {
			/*
			 * Set a coefficient default value.
			 */
			int size = order + 1;
			result = new double[size];
			for(int i = 0; i < size; i++) {
				result[i] = 1;
			}
		}
		return result;
	}

	private double[][] createOnes(int rows, int columns) {

		double[][] array = new double[rows][columns];
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < columns; j++) {
				array[i][j] = 1;
			}
		}
		return array;
	}

	private double[] createArray(int size, int start) {

		double[] array = new double[size];
		int value = start;
		for(int i = 0; i < size; i++) {
			array[i] = value++;
		}
		return array;
	}

	private void processStart(double[] ticValues, double[] newTicValues) {

		int p = (width - 1) / 2;
		for(int i = 0; i < p; i++) {
			double[] values = new double[width];
			for(int j = 0; j < width; j++) {
				double newVal = 0;
				for(int k = 0; k < order - derivative + 1; k++) {
					newVal += uStart[i][k] * startStopWeights[k][j];
				}
				values[j] = newVal;
			}
			//
			double newTic = 0;
			for(int j = 0; j < width; j++) {
				newTic += ticValues[j] * values[j];
			}
			newTicValues[i] = newTic;
		}
	}

	private void processMiddle(double[] ticValues, double[] newTicValues) {

		int p = (width - 1) / 2;
		for(int i = p; i < ticValues.length - p; i++) {
			double newTic = 0;
			for(int j = -p, k = 0; j <= p; j++, k++) {
				double ticValue = ticValues[i + j];
				double sgValue = filterCoefficients[k];
				newTic += (ticValue * sgValue) * derivativeCoefficients[0];
			}
			newTicValues[i] = newTic;
		}
	}

	private void processEnd(double[] ticValues, double[] newTicValues) {

		int p = (width - 1) / 2;
		for(int i = ticValues.length - p, m = 0; i < ticValues.length; i++, m++) {
			double[] values = new double[width];
			for(int j = 0; j < width; j++) {
				double newVal = 0;
				for(int k = 0; k < order - derivative + 1; k++) {
					newVal += uStop[m][k] * startStopWeights[k][j];
				}
				values[j] = newVal;
			}
			//
			double newTic = 0;
			for(int j = 0, k = ticValues.length - width; j < width; j++, k++) {
				newTic += ticValues[k] * values[j];
			}
			newTicValues[i] = newTic;
		}
	}
}
