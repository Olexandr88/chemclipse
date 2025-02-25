/*******************************************************************************
 * Copyright (c) 2013, 2024 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 * Matthias Mailänder - add dirty handling
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.msd.ui.editors;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.chemclipse.converter.exceptions.NoConverterAvailableException;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.notifier.UpdateNotifier;
import org.eclipse.chemclipse.msd.converter.database.DatabaseConverter;
import org.eclipse.chemclipse.msd.converter.exceptions.NoMassSpectrumConverterAvailableException;
import org.eclipse.chemclipse.msd.model.core.IMassSpectra;
import org.eclipse.chemclipse.msd.swt.ui.support.DatabaseFileSupport;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImageProvider;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.support.events.IPerspectiveAndViewIds;
import org.eclipse.chemclipse.support.ui.workbench.DisplayUtils;
import org.eclipse.chemclipse.support.ui.workbench.EditorSupport;
import org.eclipse.chemclipse.support.updates.IUpdateListener;
import org.eclipse.chemclipse.swt.ui.notifier.UpdateNotifierUI;
import org.eclipse.chemclipse.ux.extension.msd.ui.internal.support.DatabaseImportRunnable;
import org.eclipse.chemclipse.ux.extension.msd.ui.swt.MassSpectrumLibraryUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

public class DatabaseEditor extends EditorPart {

	public static final String ID = "org.eclipse.chemclipse.ux.extension.msd.ui.part.massSpectrumLibraryEditor";
	public static final String CONTRIBUTION_URI = "bundleclass://org.eclipse.chemclipse.ux.extension.msd.ui/org.eclipse.chemclipse.ux.extension.msd.ui.editors.DatabaseEditor";
	public static final String ICON_URI = ApplicationImageFactory.getInstance().getURI(IApplicationImage.IMAGE_MASS_SPECTRUM_DATABASE, IApplicationImageProvider.SIZE_16x16);
	public static final String TOOLTIP = "Mass Spectrum Library - Detector Type: MSD";
	//
	private static final Logger logger = Logger.getLogger(DatabaseEditor.class);
	//
	private boolean isDirty = false;
	/*
	 * Injected member in constructor
	 */
	@Inject
	private MPart part;
	@Inject
	private MApplication application;
	@Inject
	private EModelService modelService;
	@Inject
	private IEventBroker eventBroker;
	/*
	 * Mass spectrum selection and the GUI element.
	 */
	private AtomicReference<MassSpectrumLibraryUI> massSpectrumLibraryControl = new AtomicReference<>();
	private File massSpectrumFile = null;
	private IMassSpectra massSpectra = null;
	private ArrayList<EventHandler> registeredEventHandler;
	private List<Object> objects = new ArrayList<>();

	public void registerEvent(String topic, String property) {

		registerEvent(topic, new String[]{property});
	}

	public void registerEvent(String topic, String[] properties) {

		if(eventBroker != null) {
			registeredEventHandler.add(registerEventHandler(eventBroker, topic, properties));
		}
	}

	public void registerEvents() {

		registerEvent(IChemClipseEvents.TOPIC_LIBRARY_MSD_UPDATE, IChemClipseEvents.EVENT_BROKER_DATA);
	}

	@Persist
	public void save() {

		Shell shell = DisplayUtils.getShell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				try {
					monitor.beginTask("Save Mass Spectra", IProgressMonitor.UNKNOWN);
					try {
						saveMassSpectra(monitor, shell);
					} catch(NoMassSpectrumConverterAvailableException e) {
						throw new InvocationTargetException(e);
					}
				} finally {
					monitor.done();
				}
			}
		};
		/*
		 * Run the export
		 */
		try {
			dialog.run(true, false, runnable);
		} catch(InvocationTargetException e) {
			doSaveAs();
		} catch(InterruptedException e) {
			logger.warn(e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	@Focus
	public void setFocus() {

		if(massSpectra != null) {
			UpdateNotifierUI.update(Display.getDefault(), IChemClipseEvents.TOPIC_LIBRARY_MSD_UPDATE_SELECTION, massSpectra);
		}
	}

	public void updateObjects(List<Object> objects, String topic) {

		if(objects.size() == 1) {
			Object object = objects.get(0);
			if(object instanceof IMassSpectra newMassSpectra) {
				if(object == massSpectra) {
					isDirty = newMassSpectra.isDirty();
				}
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {

		if(massSpectra != null) {
			try {
				boolean saveSuccessful = DatabaseFileSupport.saveMassSpectra(massSpectra);
				isDirty = !saveSuccessful;
			} catch(NoConverterAvailableException e) {
				logger.warn(e);
			}
		}
	}

	@Override
	public void doSaveAs() {

		try {
			DatabaseFileSupport.saveMassSpectra(Display.getCurrent().getActiveShell(), massSpectra, "Mass Spectra");
		} catch(NoConverterAvailableException e) {
			logger.warn(e);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {

		setSite(site);
		setInput(input);
		//
		String fileName = input.getName();
		fileName = fileName.substring(0, fileName.length() - 4);
		setPartName(fileName);
		//
		if(input instanceof IFileEditorInput fileEditorInput) {
			File file = fileEditorInput.getFile().getLocation().toFile();
			importMassSpectra(file, true);
		} else if(input instanceof IURIEditorInput uriEditorInput) {
			File file = new File(uriEditorInput.getURI());
			importMassSpectra(file, true);
		} else {
			throw new PartInitException("Unimplemented editor input " + input.getClass());
		}
	}

	@Override
	public boolean isDirty() {

		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {

		return true;
	}

	@Override
	public void createPartControl(Composite parent) {

		initializeEditor(parent);
		updateMassSpectrumListUI();
	}

	private void initializeEditor(Composite parent) {

		massSpectrumLibraryControl.set(new MassSpectrumLibraryUI(parent, SWT.NONE));
	}

	@PostConstruct
	private void createControl(Composite parent) {

		loadMassSpectra();
		createPages(parent);
		registeredEventHandler = new ArrayList<>();
		registerEvents();
	}

	private void createPages(Composite parent) {

		if(massSpectra != null && massSpectra.getMassSpectrum(1) != null) {
			part.setLabel(("".equals(massSpectra.getName())) ? massSpectrumFile.getName() : massSpectra.getName());
			createEditorPage(parent);
		} else {
			part.setLabel("Database Editor");
			createErrorMessagePage(parent);
		}
	}

	private void createEditorPage(Composite parent) {

		initializeEditor(parent);
		updateMassSpectrumListUI();
	}

	private void createErrorMessagePage(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		Label label = new Label(composite, SWT.NONE);
		label.setText("The mass spectrum couldn't be loaded.");
	}

	private void importMassSpectra(File file, boolean batch) {

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(DisplayUtils.getShell());
		DatabaseImportRunnable runnable = new DatabaseImportRunnable(file);
		try {
			/*
			 * No fork, otherwise it might crash when loading the data takes too long.
			 */
			boolean fork = !batch;
			dialog.run(fork, true, runnable);
		} catch(InvocationTargetException e) {
			logger.warn(e);
			logger.warn(e.getCause());
		} catch(InterruptedException e) {
			logger.warn(e);
			Thread.currentThread().interrupt();
		}
		/*
		 * Add the mass spectra handling.
		 */
		massSpectra = runnable.getMassSpectra();
		massSpectrumFile = file;
		massSpectra.addUpdateListener(new IUpdateListener() {

			@Override
			public void update() {

				updateMassSpectrumListUI();
			}
		});
	}

	private void loadMassSpectra() {

		try {
			Object object = part.getObject();
			if(object instanceof Map<?, ?> map) {
				/*
				 * String
				 */
				File file = new File((String)map.get(EditorSupport.MAP_FILE));
				boolean batch = (boolean)map.get(EditorSupport.MAP_BATCH);
				importMassSpectra(file, batch);
			} else if(object instanceof String text) {
				/*
				 * Legacy ... Deprecated
				 */
				File file = new File(text);
				importMassSpectra(file, true);
			}
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	@PreDestroy
	private void preDestroy() {

		MassSpectrumLibraryUI massSpectrumLibraryUI = massSpectrumLibraryControl.get();
		if(massSpectrumLibraryUI != null) {
			massSpectrumLibraryUI.dispose();
		}
		UpdateNotifierUI.update(Display.getDefault(), IChemClipseEvents.TOPIC_LIBRARY_MSD_UPDATE_SELECTION, null);
		/*
		 * Remove the editor from the listed parts.
		 */
		List<String> clearTopics = Arrays.asList(IChemClipseEvents.TOPIC_SCAN_XXD_UPDATE_SELECTION);
		UpdateNotifier.update(IChemClipseEvents.TOPIC_EDITOR_LIBRARY_CLOSE, clearTopics);
		//
		if(modelService != null) {
			MPartStack partStack = (MPartStack)modelService.find(IPerspectiveAndViewIds.EDITOR_PART_STACK_ID, application);
			part.setToBeRendered(false);
			part.setVisible(false);
			partStack.getChildren().remove(part);
		}
	}

	private EventHandler registerEventHandler(IEventBroker eventBroker, String topic, String[] properties) {

		EventHandler eventHandler = new EventHandler() {

			@Override
			public void handleEvent(Event event) {

				try {
					objects.clear();
					for(String property : properties) {
						Object object = event.getProperty(property);
						objects.add(object);
					}
					update(topic);
				} catch(Exception e) {
					logger.warn(e + "\t" + event);
				}
			}
		};
		eventBroker.subscribe(topic, eventHandler);
		return eventHandler;
	}

	private void saveMassSpectra(IProgressMonitor monitor, Shell shell) throws NoMassSpectrumConverterAvailableException {

		if(massSpectrumFile != null && massSpectra != null && shell != null) {
			/*
			 * Convert the mass spectra.
			 */
			String converterId = massSpectra.getConverterId();
			if(converterId != null && !converterId.equals("")) {
				monitor.subTask("Save Mass Spectra");
				IProcessingInfo<File> processingInfo = DatabaseConverter.convert(massSpectrumFile, massSpectra, false, converterId, monitor);
				try {
					isDirty = !processingInfo.hasErrorMessages();
				} catch(TypeCastException e) {
					logger.warn(e);
				}
			} else {
				throw new NoMassSpectrumConverterAvailableException();
			}
		}
	}

	private void update(String topic) {

		MassSpectrumLibraryUI massSpectrumLibraryUI = massSpectrumLibraryControl.get();
		if(massSpectrumLibraryUI != null) {
			if(massSpectrumLibraryUI.isVisible()) {
				updateObjects(objects, topic);
			}
		}
	}

	private void updateMassSpectrumListUI() {

		MassSpectrumLibraryUI massSpectrumLibraryUI = massSpectrumLibraryControl.get();
		if(massSpectrumLibraryUI != null) {
			massSpectrumLibraryUI.update(massSpectrumFile, massSpectra);
		}
	}
}