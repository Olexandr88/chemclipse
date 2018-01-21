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
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.chart3d;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IPcaResults;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;

public class ScorePlot3d {

	private Axes axes;
	private ContextMenu contextMenu;
	private FXCanvas fxCanvas;
	private ChartLegend legend;
	private final double rotateModifier = 10;
	private Chart3DScatter scatter;
	private Chart3DSettings settings;
	private boolean showLegend;

	public ScorePlot3d(Composite parent, Object dataLayout) {
		showLegend = false;
		/*
		 * JavaFX init
		 */
		fxCanvas = new FXCanvas(parent, SWT.None);
		fxCanvas.setLayoutData(dataLayout);
		settings = new Chart3DSettings(800);
		axes = new Axes(settings);
		scatter = new Chart3DScatter(settings);
		legend = new ChartLegend(settings);
		contextMenu = new ContextMenu();
		CheckMenuItem displayLegend = new CheckMenuItem("Display Legend");
		displayLegend.setSelected(showLegend);
		displayLegend.setOnAction(e -> {
			showLegend = ((CheckMenuItem)e.getSource()).isSelected();
			createScene();
			createScene();
		});
		contextMenu.getItems().add(displayLegend);
		/*
		 * update scene after resize
		 */
		parent.addListener(SWT.Resize, (event) -> createScene());
		createScene();
	}

	private void createScene() {

		/*
		 * create data
		 */
		Group root = new Group();
		AmbientLight ambientlight = new AmbientLight();
		Group mainGroup = new Group();
		/*
		 * set camera
		 */
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setTranslateZ(-4000);
		camera.setNearClip(0.01);
		camera.setFarClip(10000.0);
		root.getChildren().addAll(mainGroup, ambientlight, camera);
		Group objects = new Group();
		objects.getChildren().addAll(scatter.getScarter(), axes.getAxes());
		Rotate rotate = new Rotate(180, 0, 0, 0, Rotate.X_AXIS);
		objects.getTransforms().add(rotate);
		mainGroup.getChildren().add(objects);
		/*
		 * built header
		 */
		BorderPane borderPane = new BorderPane();
		Label label = new Label("PCA Score plot");
		label.setAlignment(Pos.CENTER);
		label.setFont(new Font("Arial", 20));
		borderPane.setCenter(label);
		/*
		 * built legend
		 */
		VBox boxLegend = legend.getLegend();
		boxLegend.layout();
		/*
		 * build central subscene, which contains chart
		 */
		Point sizeScene = fxCanvas.getParent().getSize();
		SubScene mainScene;
		if(showLegend) {
			mainScene = new SubScene(root, sizeScene.x - boxLegend.getWidth(), sizeScene.y - borderPane.getHeight(), true, SceneAntialiasing.BALANCED);
		} else {
			mainScene = new SubScene(root, sizeScene.x, sizeScene.y - borderPane.getHeight(), true, SceneAntialiasing.BALANCED);
		}
		mainScene.setFill(Color.WHITE);
		mainScene.setCamera(camera);
		makeZoomable(mainScene, mainGroup);
		mousePressedOrMoved(mainScene, mainGroup);
		mainScene.setOnMousePressed(e -> {
			if(e.getButton() == null) {
				return;
			}
			if(e.getButton().equals(MouseButton.SECONDARY)) {
				contextMenu.show(mainScene, e.getScreenX(), e.getScreenY());
				// updateSeries();
			}
			if(e.getButton().equals(MouseButton.PRIMARY)) {
				contextMenu.hide();
			}
		});
		/*
		 * create scene
		 */
		BorderPane pane;
		if(showLegend) {
			pane = new BorderPane(mainScene, borderPane, boxLegend, null, null);
		} else {
			pane = new BorderPane(mainScene, borderPane, null, null, null);
		}
		pane.layout();
		Scene scene = new Scene(pane, sizeScene.x, sizeScene.y);
		fxCanvas.setScene(scene);
		pane.setCenter(mainScene);
		/*
		 * adjust size composite
		 */
		fxCanvas.getParent().layout(true);
	}

	private void makeZoomable(SubScene scene, Group group) {

		final double MAX_SCALE = 200000.0;
		final double MIN_SCALE = 0.1;
		scene.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {

				double delta = 1.2;
				double scale = group.getScaleX();
				if(event.getDeltaY() < 0) {
					scale /= delta;
				} else {
					scale *= delta;
				}
				scale = (scale < MAX_SCALE ? scale : MAX_SCALE);
				scale = (MIN_SCALE < scale ? scale : MIN_SCALE);
				group.setScaleX(scale);
				group.setScaleY(scale);
			}
		});
	}

	private void mousePressedOrMoved(SubScene sceneRoot, Group group) {

		Rotate xRotate = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
		Rotate yRotate = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
		group.getTransforms().addAll(xRotate, yRotate);
		sceneRoot.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {

			private double mouseXold = 0;
			private double mouseYold = 0;

			@Override
			public void handle(MouseEvent event) {

				if(event.getEventType() == MouseEvent.MOUSE_PRESSED || event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
					double mouseXnew = event.getSceneX();
					double mouseYnew = event.getSceneY();
					if(event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
						double pitchRotate = xRotate.getAngle() + (mouseYnew - mouseYold) / rotateModifier;
						xRotate.setAngle(pitchRotate);
						double yawRotate;
						double a = Math.abs((pitchRotate % 360));
						if(a < 90 || a > 270) {
							yawRotate = yRotate.getAngle() - (mouseXnew - mouseXold) / rotateModifier;
						} else {
							yawRotate = yRotate.getAngle() + (mouseXnew - mouseXold) / rotateModifier;
						}
						yRotate.setAngle(yawRotate);
					}
					mouseXold = mouseXnew;
					mouseYold = mouseYnew;
				}
			}
		});
	}

	public void removeData() {

		scatter = new Chart3DScatter(settings);
		createScene();
		createScene();
	}

	public void update(IPcaResults pcaResults) {

		Chart3DSettings.setSettings(settings, pcaResults, 800);
		Chart3DSettings.setAxes(settings);
		axes = new Axes(settings);
		scatter = new Chart3DScatter(settings, pcaResults);
		legend = new ChartLegend(settings);
		createScene();
		createScene();
	}

	public void updateSelection() {

		scatter.updateSelection();
		fxCanvas.redraw();
	}
}
