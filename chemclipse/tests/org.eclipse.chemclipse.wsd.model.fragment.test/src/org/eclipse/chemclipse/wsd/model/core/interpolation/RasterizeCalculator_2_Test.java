/*******************************************************************************
 * Copyright (c) 2022, 2023 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.wsd.model.core.interpolation;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.chemclipse.model.interpolation.RasterizeCalculator;

import junit.framework.TestCase;

public class RasterizeCalculator_2_Test extends TestCase {

	private TreeMap<Float, Float> dataOriginal = new TreeMap<>();

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		dataOriginal.put(200.0f, 500.0f);
		dataOriginal.put(202.0f, 1000.0f);
		dataOriginal.put(204.0f, 1500.0f);
		dataOriginal.put(206.0f, 1000.0f);
		dataOriginal.put(208.0f, 800.0f);
		dataOriginal.put(210.0f, 600.0f);
	}

	@Override
	protected void tearDown() throws Exception {

		dataOriginal = null;
		super.tearDown();
	}

	public void test1() {

		Map<Integer, Float> dataRasterized = RasterizeCalculator.apply(dataOriginal, 0);
		assertNull(dataRasterized);
	}

	public void test2() {

		Map<Integer, Float> dataRasterized = RasterizeCalculator.apply(dataOriginal, 1);
		//
		assertEquals(11, dataRasterized.size());
		assertEquals(500.0f, dataRasterized.get(200));
		assertEquals(750.0f, dataRasterized.get(201));
		assertEquals(1000.0f, dataRasterized.get(202));
		assertEquals(1250.0f, dataRasterized.get(203));
		assertEquals(1500.0f, dataRasterized.get(204));
		assertEquals(1250.0f, dataRasterized.get(205));
		assertEquals(1000.0f, dataRasterized.get(206));
		assertEquals(900.0f, dataRasterized.get(207));
		assertEquals(800.0f, dataRasterized.get(208));
		assertEquals(700.0f, dataRasterized.get(209));
		assertEquals(600.0f, dataRasterized.get(210));
	}

	public void test3() {

		Map<Integer, Float> dataRasterized = RasterizeCalculator.apply(dataOriginal, 2);
		//
		assertEquals(6, dataRasterized.size());
		assertEquals(500.0f, dataRasterized.get(200));
		assertNull(dataRasterized.get(201));
		assertEquals(1000.0f, dataRasterized.get(202));
		assertNull(dataRasterized.get(203));
		assertEquals(1500.0f, dataRasterized.get(204));
		assertNull(dataRasterized.get(205));
		assertEquals(1000.0f, dataRasterized.get(206));
		assertNull(dataRasterized.get(207));
		assertEquals(800.0f, dataRasterized.get(208));
		assertNull(dataRasterized.get(209));
		assertEquals(600.0f, dataRasterized.get(210));
	}

	public void test4() {

		Map<Integer, Float> dataRasterized = RasterizeCalculator.apply(dataOriginal, 10);
		//
		assertEquals(2, dataRasterized.size());
		assertEquals(500.0f, dataRasterized.get(200));
		assertNull(dataRasterized.get(201));
		assertNull(dataRasterized.get(202));
		assertNull(dataRasterized.get(203));
		assertNull(dataRasterized.get(204));
		assertNull(dataRasterized.get(205));
		assertNull(dataRasterized.get(206));
		assertNull(dataRasterized.get(207));
		assertNull(dataRasterized.get(208));
		assertNull(dataRasterized.get(209));
		assertEquals(600.0f, dataRasterized.get(210));
	}

	public void test5() {

		Map<Integer, Float> dataRasterized = RasterizeCalculator.apply(dataOriginal, 0);
		assertNull(dataRasterized);
	}
}