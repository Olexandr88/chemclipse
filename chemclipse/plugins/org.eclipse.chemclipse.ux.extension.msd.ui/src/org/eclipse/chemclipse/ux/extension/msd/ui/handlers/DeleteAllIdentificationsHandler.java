/*******************************************************************************
 * Copyright (c) 2012, 2015 Philip (eselmeister) Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip (eselmeister) Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.msd.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class DeleteAllIdentificationsHandler implements EventHandler {

	private static IChromatogramSelectionMSD chromatogramSelection;

	@Execute
	public void execute(UISynchronize uiSynchronize, final @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {

		if(chromatogramSelection != null) {
			/*
			 * Remove all targets.
			 */
			IChromatogramMSD chromatogram = chromatogramSelection.getChromatogramMSD();
			if(chromatogram != null) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
				messageBox.setText("Delete All Identifications");
				messageBox.setMessage("Do you really want to delete all identifications?");
				int decision = messageBox.open();
				if(SWT.YES == decision) {
					/*
					 * Remove all chromatogram targets.
					 */
					chromatogram.removeAllTargets();
					/*
					 * Remove all peak targets.
					 */
					List<IChromatogramPeakMSD> peaks = chromatogram.getPeaks();
					for(IChromatogramPeakMSD peak : peaks) {
						peak.removeAllTargets();
					}
					/*
					 * Try to update if the IChromatogramSelection is an
					 * instance of ChromatogramSelection.
					 */
					if(chromatogramSelection instanceof ChromatogramSelectionMSD) {
						((ChromatogramSelectionMSD)chromatogramSelection).update(true);
					}
				}
			}
		}
	}

	@Override
	public void handleEvent(Event event) {

		if(event.getTopic().equals(IChemClipseEvents.TOPIC_CHROMATOGRAM_MSD_UPDATE_CHROMATOGRAM_SELECTION)) {
			chromatogramSelection = (IChromatogramSelectionMSD)event.getProperty(IChemClipseEvents.PROPERTY_CHROMATOGRAM_SELECTION);
		}
	}
}
