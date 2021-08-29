/*******************************************************************************
 * Copyright (c) 2008, 2021 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.integrator.result;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class PeakIntegrationResult_7_Test extends TestCase {

	private IPeakIntegrationResult result;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		result = new PeakIntegrationResult();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testGetIntegratedIons_1() {

		result.addIntegratedIon(55);
		Set<Integer> ions = result.getIntegratedIons();
		assertEquals(1, ions.size());
		assertTrue(ions.contains(55));
		assertFalse(ions.contains(73));
		assertFalse(ions.contains(34));
		assertFalse(ions.contains(48));
	}

	public void testGetIntegratedIons_2() {

		result.addIntegratedIon(55);
		result.addIntegratedIon(73);
		result.addIntegratedIon(34);
		result.addIntegratedIon(48);
		result.removeIntegratedIon(73);
		result.removeIntegratedIon(34);
		result.removeIntegratedIon(48);
		Set<Integer> ions = result.getIntegratedIons();
		assertEquals(1, ions.size());
		assertTrue(ions.contains(55));
		assertFalse(ions.contains(73));
		assertFalse(ions.contains(34));
		assertFalse(ions.contains(48));
	}

	public void testGetIntegratedIons_3() {

		Set<Integer> ions = new HashSet<Integer>();
		ions.add(55);
		ions.add(73);
		ions.add(34);
		ions.add(48);
		result.addIntegratedIons(ions);
		Set<Integer> ions2 = result.getIntegratedIons();
		assertEquals(4, ions2.size());
		assertTrue(ions2.contains(55));
		assertTrue(ions2.contains(73));
		assertTrue(ions2.contains(34));
		assertTrue(ions2.contains(48));
	}

	public void testGetIntegratedIons_4() {

		result.addIntegratedIon(55);
		result.addIntegratedIon(55);
		result.addIntegratedIon(55);
		result.addIntegratedIon(55);
		Set<Integer> ions = result.getIntegratedIons();
		assertEquals(1, ions.size());
		assertTrue(ions.contains(55));
	}
}
