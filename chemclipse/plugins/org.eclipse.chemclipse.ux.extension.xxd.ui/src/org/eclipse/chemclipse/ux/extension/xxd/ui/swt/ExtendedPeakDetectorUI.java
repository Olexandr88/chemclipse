/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.csd.model.core.IChromatogramPeakCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IPeakModel;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.support.ui.addons.ModelSupportAddon;
import org.eclipse.chemclipse.swt.ui.support.Colors;
import org.eclipse.chemclipse.ux.extension.ui.support.PartSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.listener.BaselineSelectionPaintListener;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.listener.ScanSelectionPaintListener;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.ChromatogramSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.ManualPeakDetector;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.OverlaySupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.preferences.PreferencePagePeaks;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.eavp.service.swtchart.core.BaseChart;
import org.eclipse.eavp.service.swtchart.core.IChartSettings;
import org.eclipse.eavp.service.swtchart.core.ISeriesData;
import org.eclipse.eavp.service.swtchart.core.SeriesData;
import org.eclipse.eavp.service.swtchart.customcharts.ChromatogramChart;
import org.eclipse.eavp.service.swtchart.events.AbstractHandledEventProcessor;
import org.eclipse.eavp.service.swtchart.events.IHandledEventProcessor;
import org.eclipse.eavp.service.swtchart.linecharts.ILineSeriesData;
import org.eclipse.eavp.service.swtchart.linecharts.ILineSeriesSettings;
import org.eclipse.eavp.service.swtchart.linecharts.LineChart;
import org.eclipse.eavp.service.swtchart.linecharts.LineSeriesData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IPlotArea;
import org.swtchart.Range;

public class ExtendedPeakDetectorUI {

	private static final Logger logger = Logger.getLogger(ExtendedPeakDetectorUI.class);
	//
	private static final String ID_PEAK = "Peak";
	private static final String ID_BACKGROUND = "Background";
	/*
	 * Detection Types
	 */
	private static final String DETECTION_TYPE_BASELINE = "DETECTION_TYPE_BASELINE";
	private static final String DETECTION_TYPE_SCAN = "DETECTION_TYPE_SCAN";
	private static final String DETECTION_TYPE_SCAN_BB = DETECTION_TYPE_SCAN + "_BB";
	private static final String DETECTION_TYPE_SCAN_VV = DETECTION_TYPE_SCAN + "_VV";
	private static final String DETECTION_TYPE_NONE = "";
	//
	private Map<String, String> detectionTypeDescriptions;
	//
	private static final char KEY_BASELINE = BaseChart.KEY_CODE_d;
	private static final char KEY_BB = BaseChart.KEY_CODE_b;
	private static final char KEY_VV = BaseChart.KEY_CODE_v;
	/*
	 * Detection Box
	 */
	// private static final String DETECTION_BOX_LEFT = "DETECTION_BOX_LEFT";
	// private static final String DETECTION_BOX_RIGHT = "DETECTION_BOX_RIGHT";
	// private static final String DETECTION_BOX_NONE = "DETECTION_BOX_NONE";
	private static final int BOX_SNAP_MARKER_WINDOW = 4;
	private static final int BOX_MAX_DELTA = 1;
	//
	private Composite toolbarInfo;
	private Label labelChromatogram;
	private Label labelDetectionType;
	private Button buttonAddPeak;
	private ChromatogramChart chromatogramChart;
	//
	private IChromatogramSelection chromatogramSelection;
	private IPeak peak;
	//
	private BaselineSelectionPaintListener baselineSelectionPaintListener;
	private ScanSelectionPaintListener scanSelectionPaintListener;
	//
	private Cursor defaultCursor;
	private String detectionType = DETECTION_TYPE_NONE;
	// private String detectionBox = DETECTION_BOX_NONE;
	/*
	 * Baseline Detection Method
	 */
	private int xStart;
	private int yStart;
	private int xStop;
	private int yStop;
	//
	// private int xMoveStart;
	//
	private Shell shell = Display.getDefault().getActiveShell();
	private OverlaySupport overlaySupport;

	private class KeyPressedEventProcessor extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		private int keyCode;

		public KeyPressedEventProcessor(int keyCode) {
			this.keyCode = keyCode;
		}

		@Override
		public int getEvent() {

			return BaseChart.EVENT_KEY_DOWN;
		}

		@Override
		public int getButton() {

			return keyCode;
		}

		@Override
		public int getStateMask() {

			return SWT.NONE;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			System.out.println("KEY PRESSED");
			handleKeyPressedEvent(event);
		}
	}

	private class MouseDownEventProcessor extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_DOWN;
		}

		@Override
		public int getButton() {

			return BaseChart.BUTTON_LEFT;
		}

		@Override
		public int getStateMask() {

			return SWT.CTRL;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			System.out.println("MOUSE DOWN");
			handleMouseDownEvent(event);
		}
	}

	private class MouseMoveEventProcessor extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_MOVE;
		}

		@Override
		public int getStateMask() {

			return SWT.CTRL;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			System.out.println("MOUSE MOVE");
			handleMouseMoveEvent(event);
		}
	}

	private class MouseUpEventProcessor extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_UP;
		}

		@Override
		public int getButton() {

			return BaseChart.BUTTON_LEFT;
		}

		@Override
		public int getStateMask() {

			return SWT.BUTTON1;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			System.out.println("MOUSE UP");
			handleMouseUpEvent(event);
		}
	}

	private class MouseDoubleClickEventProcessor extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_DOUBLE_CLICK;
		}

		@Override
		public int getButton() {

			return BaseChart.BUTTON_LEFT;
		}

		@Override
		public int getStateMask() {

			return SWT.NONE;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			handleMouseDoubleClickEvent(event);
		}
	}

	@Inject
	public ExtendedPeakDetectorUI(Composite parent) {
		overlaySupport = new OverlaySupport();
		detectionTypeDescriptions = new HashMap<String, String>();
		detectionTypeDescriptions.put(DETECTION_TYPE_BASELINE, "Detection Modus (Baseline) [Key:" + KEY_BASELINE + "]");
		detectionTypeDescriptions.put(DETECTION_TYPE_SCAN_BB, "Detection Modus (BB) [Key:" + KEY_BB + "]");
		detectionTypeDescriptions.put(DETECTION_TYPE_SCAN_VV, "Detection Modus (VV) [Key:" + KEY_VV + "]");
		detectionTypeDescriptions.put(DETECTION_TYPE_NONE, "");
		initialize(parent);
	}

	@Focus
	public void setFocus() {

		updateChromatogramAndPeak();
	}

	public void update(IChromatogramSelection chromatogramSelection) {

		this.chromatogramSelection = chromatogramSelection;
		IChromatogram chromatogram = null;
		if(chromatogramSelection != null) {
			chromatogram = chromatogramSelection.getChromatogram();
		}
		//
		labelDetectionType.setText("");
		labelChromatogram.setText(ChromatogramSupport.getChromatogramLabel(chromatogram));
		this.peak = null;
		//
		updateChromatogramAndPeak();
	}

	private void updateChromatogramAndPeak() {

		chromatogramChart.deleteSeries();
		buttonAddPeak.setEnabled(false);
		//
		if(chromatogramSelection != null) {
			/*
			 * Draw the chromatogram
			 */
			List<ILineSeriesData> lineSeriesDataList = new ArrayList<ILineSeriesData>();
			//
			String overlayType = OverlaySupport.OVERLAY_TYPE_TIC;
			String derivativeType = OverlaySupport.DERIVATIVE_NONE;
			List<Integer> ions = new ArrayList<Integer>();
			lineSeriesDataList.add(overlaySupport.getLineSeriesData(chromatogramSelection, overlayType, derivativeType, ions));
			chromatogramChart.addSeriesData(lineSeriesDataList, LineChart.LOW_COMPRESSION);
			//
			if(peak != null) {
				buttonAddPeak.setEnabled(true);
				lineSeriesDataList.add(getPeak(peak, Colors.RED));
				lineSeriesDataList.add(getPeakBackground(peak, Colors.BLACK));
				chromatogramChart.addSeriesData(lineSeriesDataList, LineChart.LOW_COMPRESSION);
			}
		}
	}

	private void initialize(Composite parent) {

		parent.setLayout(new GridLayout(1, true));
		//
		createToolbarMain(parent);
		toolbarInfo = createToolbarInfo(parent);
		createChromatogramChart(parent);
		//
		PartSupport.setCompositeVisibility(toolbarInfo, true);
	}

	private void createToolbarMain(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridDataStatus = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridDataStatus);
		composite.setLayout(new GridLayout(9, false));
		//
		labelDetectionType = createDetectionTypeLabel(composite);
		createButtonToggleToolbarInfo(composite);
		createDetectionTypeButton(composite, DETECTION_TYPE_BASELINE, IApplicationImage.IMAGE_DETECTION_TYPE_BASELINE);
		createDetectionTypeButton(composite, DETECTION_TYPE_SCAN_BB, IApplicationImage.IMAGE_DETECTION_TYPE_SCAN_BB);
		createDetectionTypeButton(composite, DETECTION_TYPE_SCAN_VV, IApplicationImage.IMAGE_DETECTION_TYPE_SCAN_VV);
		buttonAddPeak = createAddPeakButton(composite);
		createToggleChartLegendButton(composite);
		createDetectionTypeButton(composite, DETECTION_TYPE_NONE, IApplicationImage.IMAGE_RESET);
		createSettingsButton(composite);
	}

	private Label createDetectionTypeLabel(Composite parent) {

		Label label = new Label(parent, SWT.NONE);
		label.setText("");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		return label;
	}

	private Composite createToolbarInfo(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(1, false));
		composite.setVisible(false);
		//
		labelChromatogram = new Label(composite, SWT.NONE);
		labelChromatogram.setText("");
		labelChromatogram.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		return composite;
	}

	private Button createDetectionTypeButton(Composite parent, String detectionType, String image) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText(detectionTypeDescriptions.get(detectionType));
		button.setImage(ApplicationImageFactory.getInstance().getImage(image, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				setDetectionType(detectionType);
				if(detectionType.equals(DETECTION_TYPE_NONE)) {
					reset();
				}
			}
		});
		return button;
	}

	private Button createAddPeakButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Add the manually detected peak.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ADD, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if(peak != null) {
					IChromatogram chromatogram = chromatogramSelection.getChromatogram();
					if(chromatogram instanceof IChromatogramMSD) {
						if(peak instanceof IChromatogramPeakMSD) {
							IChromatogramPeakMSD peakMSD = (IChromatogramPeakMSD)peak;
							((IChromatogramMSD)chromatogram).addPeak(peakMSD);
							peak = null;
							updateChromatogramAndPeak();
							chromatogramSelection.update(true);
						}
					} else if(chromatogram instanceof IChromatogramCSD) {
						if(peak instanceof IChromatogramPeakCSD) {
							IChromatogramPeakCSD peakCSD = (IChromatogramPeakCSD)peak;
							((IChromatogramCSD)chromatogram).addPeak(peakCSD);
							peak = null;
							updateChromatogramAndPeak();
							chromatogramSelection.update(true);
						}
					}
				}
			}
		});
		return button;
	}

	private Button createButtonToggleToolbarInfo(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle info toolbar.");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean visible = PartSupport.toggleCompositeVisibility(toolbarInfo);
				if(visible) {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
				} else {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
				}
			}
		});
		//
		return button;
	}

	private void createToggleChartLegendButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle the chart legend");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_TAG, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chromatogramChart.toggleSeriesLegendVisibility();
			}
		});
	}

	private void createSettingsButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Open the Settings");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CONFIGURE, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPreferencePage preferencePage = new PreferencePagePeaks();
				preferencePage.setTitle("Peak Settings");
				PreferenceManager preferenceManager = new PreferenceManager();
				preferenceManager.addToRoot(new PreferenceNode("1", preferencePage));
				//
				PreferenceDialog preferenceDialog = new PreferenceDialog(shell, preferenceManager);
				preferenceDialog.create();
				preferenceDialog.setMessage("Settings");
				if(preferenceDialog.open() == PreferenceDialog.OK) {
					try {
						applySettings();
					} catch(Exception e1) {
						MessageDialog.openError(shell, "Settings", "Something has gone wrong to apply the chart settings.");
					}
				}
			}
		});
	}

	private void createChromatogramChart(Composite parent) {

		chromatogramChart = new ChromatogramChart(parent, SWT.BORDER);
		chromatogramChart.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Get the default cursor.
		 */
		defaultCursor = chromatogramChart.getCursor();
		/*
		 * Chart Settings
		 */
		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		chartSettings.setCreateMenu(true);
		chartSettings.setEnableRangeSelector(true);
		chartSettings.setShowRangeSelectorInitially(false);
		chartSettings.setSupportDataShift(false);
		//
		chartSettings.addHandledEventProcessor(new KeyPressedEventProcessor(BaseChart.KEY_CODE_d));
		// chartSettings.addHandledEventProcessor(new KeyPressedEventProcessor(BaseChart.KEY_CODE_b));
		// chartSettings.addHandledEventProcessor(new KeyPressedEventProcessor(BaseChart.KEY_CODE_v));
		// chartSettings.addHandledEventProcessor(new KeyPressedEventProcessor(SWT.ARROW_LEFT));
		// chartSettings.addHandledEventProcessor(new KeyPressedEventProcessor(SWT.ARROW_RIGHT));
		chartSettings.addHandledEventProcessor(new MouseDownEventProcessor());
		chartSettings.addHandledEventProcessor(new MouseMoveEventProcessor());
		chartSettings.addHandledEventProcessor(new MouseUpEventProcessor());
		// chartSettings.addHandledEventProcessor(new MouseDoubleClickEventProcessor());
		//
		chromatogramChart.applySettings(chartSettings);
		/*
		 * Add the paint listeners to draw the selected peak range.
		 */
		IPlotArea plotArea = (IPlotArea)getPlotArea();
		baselineSelectionPaintListener = new BaselineSelectionPaintListener();
		scanSelectionPaintListener = new ScanSelectionPaintListener();
		plotArea.addCustomPaintListener(baselineSelectionPaintListener);
		plotArea.addCustomPaintListener(scanSelectionPaintListener);
	}

	private void handleKeyPressedEvent(Event event) {

		// if(detectionType.equals(DETECTION_TYPE_NONE)) {
		// if(event.keyCode == KEY_BASELINE) {
		// detectionType = DETECTION_TYPE_BASELINE;
		// } else if(event.keyCode == KEY_BB) {
		// detectionType = DETECTION_TYPE_SCAN_BB;
		// } else if(event.keyCode == KEY_VV) {
		// detectionType = DETECTION_TYPE_SCAN_VV;
		// } else {
		// detectionType = DETECTION_TYPE_NONE;
		// }
		// } else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		// if(event.keyCode == SWT.ARROW_LEFT) {
		// if(detectionBox.equals(DETECTION_BOX_LEFT)) {
		// xStart -= 1;
		// redrawScanPeakSelection(true);
		// } else if(detectionBox.equals(DETECTION_BOX_RIGHT)) {
		// xStop -= 1;
		// redrawScanPeakSelection(true);
		// }
		// } else if(event.keyCode == SWT.ARROW_RIGHT) {
		// if(detectionBox.equals(DETECTION_BOX_LEFT)) {
		// xStart += 1;
		// redrawScanPeakSelection(true);
		// } else if(detectionBox.equals(DETECTION_BOX_RIGHT)) {
		// xStop += 1;
		// redrawScanPeakSelection(true);
		// }
		// }
		// }
		if(detectionType.equals(DETECTION_TYPE_NONE)) {
			System.out.println("KEY: " + event.keyCode);
			if(event.keyCode == KEY_BASELINE) {
				setDetectionType(DETECTION_TYPE_BASELINE);
			} else if(event.keyCode == KEY_BB) {
				setDetectionType(DETECTION_TYPE_SCAN_BB);
			} else if(event.keyCode == KEY_VV) {
				setDetectionType(DETECTION_TYPE_SCAN_VV);
			}
		} else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		}
	}

	private void setDetectionType(String detectionType) {

		/*
		 * Defaults
		 */
		this.detectionType = detectionType;
		if(detectionType.equals(DETECTION_TYPE_NONE)) {
			setDefaultCursor();
			resetSelectedRange();
		} else {
			setCursor(SWT.CURSOR_CROSS);
		}
		/*
		 * Label
		 */
		labelDetectionType.setText(detectionTypeDescriptions.get(detectionType));
		System.out.println("SET DT: " + detectionTypeDescriptions.get(detectionType));
		//
		// if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
		// this.detectionBox = DETECTION_BOX_NONE;
		// }
		//
	}

	private void resetSelectedRange() {

		baselineSelectionPaintListener.reset();
		//
		xStart = 0;
		yStart = 0;
		xStop = 0;
		yStop = 0;
	}

	private void handleMouseDownEvent(Event event) {

		if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
			System.out.println("START: " + event.x + "\t" + event.y);
			startBaselineSelection(event.x, event.y);
			setCursor(SWT.CURSOR_CROSS);
		} else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		}
		// if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		// if(isLeftMoveSnapMarker(event.x)) {
		// setCursor(SWT.CURSOR_SIZEWE);
		// xMoveStart = event.x;
		// detectionBox = DETECTION_BOX_LEFT;
		// } else if(isRightMoveSnapMarker(event.x)) {
		// setCursor(SWT.CURSOR_SIZEWE);
		// xMoveStart = event.x;
		// detectionBox = DETECTION_BOX_RIGHT;
		// } else {
		// setCursor(SWT.CURSOR_CROSS);
		// detectionBox = DETECTION_BOX_NONE;
		// }
		// }
	}

	private void handleMouseMoveEvent(Event event) {

		if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
			if(isControlKeyPressed(event)) {
				if(xStart > 0 && yStart > 0) {
					System.out.println("TRACK: " + event.x + "\t" + event.y);
					trackBaselineSelection(event.x, event.y);
				}
			}
		} else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		}
		// if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
		// /*
		// * Baseline Selection.
		// */
		// if(xStart == 0 && yStart == 0) {
		// startBaselinePeakSelection(event.x, event.y);
		// setCursor(SWT.CURSOR_CROSS);
		// } else {
		// trackBaselinePeakSelection(event.x, event.y);
		// }
		// } else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		// if(isLeftMoveSnapMarker(event.x)) {
		// setCursor(SWT.CURSOR_SIZEWE);
		// } else if(isRightMoveSnapMarker(event.x)) {
		// setCursor(SWT.CURSOR_SIZEWE);
		// } else {
		// setCursor(SWT.CURSOR_CROSS);
		// }
		// //
		// if((event.stateMask & SWT.BUTTON1) == SWT.BUTTON1) {
		// if(!detectionBox.equals(DETECTION_BOX_NONE)) {
		// int delta = getDeltaMove(event.x);
		// if(detectionBox.equals(DETECTION_BOX_LEFT)) {
		// xStart += delta;
		// redrawScanPeakSelection(false);
		// } else if(detectionBox.equals(DETECTION_BOX_RIGHT)) {
		// xStop += delta;
		// redrawScanPeakSelection(false);
		// }
		// }
		// }
		// }
	}

	private void handleMouseUpEvent(Event event) {

		if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
			if(isControlKeyPressed(event)) {
				System.out.println("STOP: " + event.x + "\t" + event.y);
				stopBaselineSelection(event.x, event.y);
				setDefaultCursor();
			}
		} else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		}
		// if(detectionType.equals(DETECTION_TYPE_BASELINE)) {
		// stopBaselinePeakSelection(event.x, event.y);
		// setDefaultCursor();
		// } else if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		// if(event.button == BaseChart.BUTTON_LEFT) {
		// setCursor(SWT.CURSOR_CROSS);
		// if(!detectionBox.equals(DETECTION_BOX_NONE)) {
		// int delta = getDeltaMove(event.x);
		// if(detectionBox.equals(DETECTION_BOX_LEFT)) {
		// xStart += delta;
		// redrawScanPeakSelection(true);
		// } else if(detectionBox.equals(DETECTION_BOX_RIGHT)) {
		// xStop += delta;
		// redrawScanPeakSelection(true);
		// }
		// }
		// if(event.count == 1) {
		// detectionBox = getDetectionBox(event.x);
		// redrawScanPeakSelection(false);
		// }
		// }
		// }
	}

	private boolean isControlKeyPressed(Event event) {

		return (event.stateMask & SWT.CTRL) == SWT.CTRL;
	}

	private void handleMouseDoubleClickEvent(Event event) {

		// if(detectionType.startsWith(DETECTION_TYPE_SCAN)) {
		// setCursor(SWT.CURSOR_CROSS);
		// if(xStart == 0) {
		// int y;
		// switch(detectionType) {
		// case DETECTION_TYPE_SCAN_BB:
		// y = getPlotArea().getBounds().height;
		// break;
		// case DETECTION_TYPE_SCAN_VV:
		// y = event.y;
		// break;
		// default:
		// y = getPlotArea().getBounds().height;
		// }
		// startScanPeakSelection(event.x, y);
		// } else if(xStart > 0 && xStop == 0) {
		// int y;
		// switch(detectionType) {
		// case DETECTION_TYPE_SCAN_BB:
		// y = getPlotArea().getBounds().height;
		// break;
		// case DETECTION_TYPE_SCAN_VV:
		// y = event.y;
		// break;
		// default:
		// y = getPlotArea().getBounds().height;
		// }
		// stopScanPeakSelection(event.x, y);
		// } else {
		// setDefaultCursor();
		// resetScanPeakSelection();
		// }
		// }
	}

	private Composite getPlotArea() {

		return chromatogramChart.getBaseChart().getPlotArea();
	}

	private void applySettings() {

		updateChromatogramAndPeak();
	}

	private void redraw() {

		chromatogramChart.getBaseChart().redraw();
	}

	private void setCursor(int cursorId) {

		chromatogramChart.setCursor(Display.getCurrent().getSystemCursor(cursorId));
	}

	private void setDefaultCursor() {

		chromatogramChart.setCursor(defaultCursor);
	}

	private String getDetectionBox(int x) {

		// if(xStart > 0) {
		// if(x <= xStart) {
		// return DETECTION_BOX_LEFT;
		// } else {
		// if(xStop > 0) {
		// if(x >= xStop) {
		// return DETECTION_BOX_RIGHT;
		// }
		// }
		// }
		// }
		// return DETECTION_BOX_NONE;
		return "";
	}

	private boolean isLeftMoveSnapMarker(int x) {

		if(x > xStart - BOX_SNAP_MARKER_WINDOW && x < xStart + BOX_SNAP_MARKER_WINDOW) {
			return true;
		}
		return false;
	}

	private boolean isRightMoveSnapMarker(int x) {

		if(x > xStop - BOX_SNAP_MARKER_WINDOW && x < xStop + BOX_SNAP_MARKER_WINDOW) {
			return true;
		}
		return false;
	}

	private int getDeltaMove(int x) {

		// int delta = x - xMoveStart;
		// if(Math.abs(delta) > BOX_MAX_DELTA) {
		// if(delta < 0) {
		// delta = -BOX_MAX_DELTA;
		// } else {
		// delta = BOX_MAX_DELTA;
		// }
		// }
		// return delta;
		return 0;
	}

	private void startBaselineSelection(int x, int y) {

		xStart = x;
		yStart = y;
		/*
		 * Set the start point.
		 */
		baselineSelectionPaintListener.setX1(xStart);
		baselineSelectionPaintListener.setY1(yStart);
	}

	private void trackBaselineSelection(int x, int y) {

		xStop = x;
		yStop = y;
		//
		baselineSelectionPaintListener.setX1(xStart);
		baselineSelectionPaintListener.setY1(yStart);
		baselineSelectionPaintListener.setX2(xStop);
		baselineSelectionPaintListener.setY2(yStop);
		redraw();
	}

	private void stopBaselineSelection(int x, int y) {

		xStop = x;
		yStop = y;
		//
		extractPeak();
	}

	private void extractPeak() {

		this.peak = extractPeakFromUserSelection(xStart, yStart, xStop, yStop);
		IEventBroker eventBroker = ModelSupportAddon.getEventBroker();
		eventBroker.send(IChemClipseEvents.TOPIC_PEAK_XXD_UPDATE_SELECTION, peak);
		/*
		 * Get the selected range.
		 */
		BaseChart baseChart = chromatogramChart.getBaseChart();
		IAxisSet axisSet = baseChart.getAxisSet();
		IAxis xAxis = axisSet.getXAxis(BaseChart.ID_PRIMARY_X_AXIS);
		Range xRange = xAxis.getRange();
		IAxis yAxis = axisSet.getYAxis(BaseChart.ID_PRIMARY_Y_AXIS);
		Range yRange = yAxis.getRange();
		/*
		 * Update the chromatogram and peak
		 */
		setDetectionType(DETECTION_TYPE_NONE);
		updateChromatogramAndPeak();
		/*
		 * Restore the selected range.
		 */
		xAxis.setRange(xRange);
		yAxis.setRange(yRange);
		redraw();
	}

	private void startScanPeakSelection(int x, int y) {

		xStart = x;
		yStart = y;
		setCursor(SWT.CURSOR_CROSS);
		scanSelectionPaintListener.setX1(xStart);
		scanSelectionPaintListener.setX2(xStop);
		redraw();
	}

	private void stopScanPeakSelection(int x, int y) {

		if(x > xStart) {
			xStop = x;
			yStop = y;
			scanSelectionPaintListener.setX1(xStart);
			scanSelectionPaintListener.setX2(xStop);
			redraw();
			// extractPeakFromUserSelection();
		}
	}

	private void redrawScanPeakSelection(boolean extractPeak) {

		// scanSelectionPaintListener.setX1(xStart);
		// scanSelectionPaintListener.setX2(xStop);
		// if(detectionBox.equals(DETECTION_BOX_LEFT)) {
		// scanSelectionPaintListener.setHighlightBox(ScanSelectionPaintListener.HIGHLIGHT_BOX_LEFT);
		// } else if(detectionBox.equals(DETECTION_BOX_RIGHT)) {
		// scanSelectionPaintListener.setHighlightBox(ScanSelectionPaintListener.HIGHLIGHT_BOX_RIGHT);
		// } else {
		// scanSelectionPaintListener.setHighlightBox(ScanSelectionPaintListener.HIGHLIGHT_BOX_NONE);
		// }
		// redraw();
		// if(extractPeak) {
		// extractSelectedPeak();
		// }
	}

	/**
	 * Extracts the selected peak.
	 * 
	 * @param xStop
	 * @param yStop
	 */
	private IPeak extractPeakFromUserSelection(int xStart, int yStart, int xStop, int yStop) {

		IPeak peak = null;
		/*
		 * Calculate the rectangle factors.
		 */
		Rectangle rectangle = getPlotArea().getBounds();
		int height = rectangle.height;
		double factorHeight = 100.0d / height;
		int width = rectangle.width;
		double factorWidth = 100.0d / width;
		/*
		 * Calculate the percentage heights and widths.
		 */
		double percentageStartHeight = (100.0d - (factorHeight * yStart)) / 100.0d;
		double percentageStopHeight = (100.0d - (factorHeight * yStop)) / 100.0d;
		double percentageStartWidth = (factorWidth * xStart) / 100.0d;
		double percentageStopWidth = (factorWidth * xStop) / 100.0d;
		/*
		 * Calculate the start and end points.
		 */
		BaseChart baseChart = chromatogramChart.getBaseChart();
		IAxis retentionTime = baseChart.getAxisSet().getXAxis(BaseChart.ID_PRIMARY_X_AXIS);
		Range millisecondsRange = retentionTime.getRange();
		IAxis intensity = baseChart.getAxisSet().getYAxis(BaseChart.ID_PRIMARY_Y_AXIS);
		Range abundanceRange = intensity.getRange();
		/*
		 * With abundance and retention time.
		 */
		double abundanceHeight = abundanceRange.upper - abundanceRange.lower;
		double millisecondsWidth = millisecondsRange.upper - millisecondsRange.lower;
		/*
		 * Peak start and stop abundances and retention times.
		 */
		int startRetentionTime = (int)(millisecondsRange.lower + millisecondsWidth * percentageStartWidth);
		int stopRetentionTime = (int)(millisecondsRange.lower + millisecondsWidth * percentageStopWidth);
		float startAbundance = (float)(abundanceRange.lower + abundanceHeight * percentageStartHeight);
		float stopAbundance = (float)(abundanceRange.lower + abundanceHeight * percentageStopHeight);
		/*
		 * Try to detect the peak.
		 */
		if(chromatogramSelection instanceof IChromatogramSelectionMSD) {
			/*
			 * Peak Detection MSD
			 */
			try {
				IChromatogramSelectionMSD chromatogramSelectionMSD = (IChromatogramSelectionMSD)chromatogramSelection;
				ManualPeakDetector manualPeakDetector = new ManualPeakDetector();
				IChromatogramMSD chromatogram = chromatogramSelectionMSD.getChromatogramMSD();
				IChromatogramPeakMSD chromatogramPeak = manualPeakDetector.calculatePeak(chromatogram, startRetentionTime, stopRetentionTime, startAbundance, stopAbundance);
				peak = chromatogramPeak;
			} catch(PeakException e) {
				logger.warn(e);
			}
		} else if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
			/*
			 * Peak Detection FID
			 */
			try {
				IChromatogramSelectionCSD chromatogramSelectionCSD = (IChromatogramSelectionCSD)chromatogramSelection;
				ManualPeakDetector manualPeakDetector = new ManualPeakDetector();
				IChromatogramCSD chromatogram = chromatogramSelectionCSD.getChromatogramCSD();
				IChromatogramPeakCSD chromatogramPeak = manualPeakDetector.calculatePeak(chromatogram, startRetentionTime, stopRetentionTime, startAbundance, stopAbundance);
				peak = chromatogramPeak;
			} catch(PeakException e) {
				logger.warn(e);
			}
		}
		//
		return peak;
	}

	private void reset() {

		this.peak = null;
		setDetectionType(DETECTION_TYPE_NONE);
		updateChromatogramAndPeak();
	}

	private void resetScanPeakSelection() {

		resetSelectedRange();
		detectionType = DETECTION_TYPE_NONE;
		labelDetectionType.setText(detectionTypeDescriptions.get(detectionType));
		setDefaultCursor();
		scanSelectionPaintListener.reset();
		redraw();
	}

	private ILineSeriesData getPeak(IPeak peak, Color color) {

		ISeriesData seriesData = getPeakSeriesData(peak);
		return getLineSeriesData(seriesData, color);
	}

	private ILineSeriesData getPeakBackground(IPeak peak, Color color) {

		ISeriesData seriesData = getPeakBaselineData(peak);
		return getLineSeriesData(seriesData, color);
	}

	private ILineSeriesData getLineSeriesData(ISeriesData seriesData, Color color) {

		ILineSeriesData lineSeriesData = new LineSeriesData(seriesData);
		ILineSeriesSettings lineSeriesSettings = lineSeriesData.getLineSeriesSettings();
		lineSeriesSettings.setLineColor(color);
		lineSeriesSettings.setEnableArea(true);
		ILineSeriesSettings lineSeriesSettingsHighlight = (ILineSeriesSettings)lineSeriesSettings.getSeriesSettingsHighlight();
		lineSeriesSettingsHighlight.setLineWidth(2);
		return lineSeriesData;
	}

	private ISeriesData getPeakSeriesData(IPeak peak) {

		String id = ID_PEAK;
		IPeakModel peakModel = peak.getPeakModel();
		List<Integer> retentionTimes = peakModel.getRetentionTimes();
		int size = retentionTimes.size();
		double[] xSeries = new double[size];
		double[] ySeries = new double[size];
		int index = 0;
		for(int retentionTime : retentionTimes) {
			//
			xSeries[index] = retentionTime;
			ySeries[index] = peakModel.getBackgroundAbundance(retentionTime) + peakModel.getPeakAbundance(retentionTime);
			//
			index++;
		}
		//
		return new SeriesData(xSeries, ySeries, id);
	}

	private ISeriesData getPeakBaselineData(IPeak peak) {

		String id = ID_BACKGROUND;
		IPeakModel peakModel = peak.getPeakModel();
		List<Integer> retentionTimes = peakModel.getRetentionTimes();
		int size = retentionTimes.size();
		double[] xSeries = new double[size];
		double[] ySeries = new double[size];
		//
		int index = 0;
		for(int retentionTime : peakModel.getRetentionTimes()) {
			xSeries[index] = retentionTime;
			ySeries[index] = peakModel.getBackgroundAbundance(retentionTime);
			//
			index++;
		}
		//
		return new SeriesData(xSeries, ySeries, id);
	}
}
