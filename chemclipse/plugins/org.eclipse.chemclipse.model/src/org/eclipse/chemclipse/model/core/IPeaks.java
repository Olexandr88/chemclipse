/*******************************************************************************
 * Copyright (c) 2011, 2019 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Christoph Läubrich - add default implementations, adjust API
 *******************************************************************************/
package org.eclipse.chemclipse.model.core;

import java.util.Collections;
import java.util.List;

/**
 * This class stores a list of peaks.
 * 
 * @author eselmeister
 * 
 */
public interface IPeaks<T extends IPeak> {

	/**
	 * Adds the peak to the end of the list.
	 * 
	 * @param peak
	 */
	default boolean addPeak(IPeak peak) throws UnsupportedOperationException {

		return false;
	}

	/**
	 * Removes the peak from the list.
	 * 
	 * @param peak
	 * @return <code>true</code> if peak was removed, <code>false</code> otherwise
	 */
	default boolean removePeak(IPeak peak) throws UnsupportedOperationException {

		return false;
	}

	/**
	 * Returns the peak with the given number.<br/>
	 * Be aware, the index is 1 based and not 0 based like in a normal list.<br/>
	 * If no peak is available, null will be returned.
	 * 
	 * @deprecated because of strange definition of index, use {@link #getPeaks().get(...)} instead
	 * @param i
	 * @return IPeak
	 */
	@Deprecated
	default T getPeak(int i) {

		List<T> peaks = getPeaks();
		if(i > 0 && i <= peaks.size()) {
			return peaks.get(--i);
		}
		return null;
	}

	/**
	 * Returns the list of peaks.
	 * 
	 * @return List<IPeak>
	 */
	List<T> getPeaks();

	/**
	 * Returns the number of stored peaks.
	 * 
	 * @deprecated use {@link #getPeaks().size()} instead
	 * @return int
	 */
	@Deprecated
	default int size() {

		return getPeaks().size();
	}

	static <X extends IPeak> IPeaks<X> singelton(X peak) {

		return new IPeaks<X>() {

			@Override
			public List<X> getPeaks() {

				return Collections.singletonList(peak);
			}
		};
	}
}
