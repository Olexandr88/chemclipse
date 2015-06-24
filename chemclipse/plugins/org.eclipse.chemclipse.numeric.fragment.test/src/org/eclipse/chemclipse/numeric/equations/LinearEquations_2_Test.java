/*******************************************************************************
 * Copyright (c) 2008, 2015 Philip (eselmeister) Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip (eselmeister) Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.numeric.equations;

import org.eclipse.chemclipse.numeric.core.IPoint;
import org.eclipse.chemclipse.numeric.core.Point;
import junit.framework.TestCase;

public class LinearEquations_2_Test extends TestCase {

	private LinearEquation eq1;
	private LinearEquation eq2;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		IPoint p11 = new Point(1, 4);
		IPoint p12 = new Point(8, 14);
		eq1 = Equations.createLinearEquation(p11, p12);
		eq2 = Equations.createLinearEquation(p11, p12);
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testEquals_1() {

		assertTrue("equals", eq1.equals(eq2));
	}

	public void testEquals_2() {

		assertTrue("equals", eq2.equals(eq1));
	}

	public void testEquals_3() {

		assertFalse("equals", eq1.equals(null));
	}

	public void testEquals_4() {

		assertFalse("equals", eq1.equals(""));
	}

	public void testHashCode_1() {

		assertEquals("hashCode", eq1.hashCode(), eq2.hashCode());
	}
}
