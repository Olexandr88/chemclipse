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
package org.eclipse.chemclipse.swt.ui.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.chemclipse.support.settings.IOperatingSystemUtils;
import org.eclipse.chemclipse.support.settings.OperatingSystemUtils;

public class ExtendedTableViewer extends TableViewer {

	private Clipboard clipboard;
	private IOperatingSystemUtils operatingSystemUtils;
	private static final String DELIMITER = "\t";

	public ExtendedTableViewer(Composite parent) {

		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
	}

	public ExtendedTableViewer(Composite parent, int style) {

		super(parent, style);
		clipboard = new Clipboard(Display.getDefault());
		operatingSystemUtils = new OperatingSystemUtils();
	}

	/**
	 * Copies the actual selection to the clipboard.
	 */
	public void copyToClipboard(final String[] titles) {

		StringBuilder builder = new StringBuilder();
		int size = titles.length;
		/*
		 * Header
		 */
		for(String title : titles) {
			builder.append(title);
			builder.append(DELIMITER);
		}
		builder.append(operatingSystemUtils.getLineDelimiter());
		/*
		 * Copy the selected items.
		 */
		TableItem selection;
		Table table = this.getTable();
		for(int index : table.getSelectionIndices()) {
			/*
			 * Get the nth selected item.
			 */
			selection = table.getItem(index);
			/*
			 * Dump all elements of the item, e.g. RT, Abundance, ... .
			 */
			for(int columnIndex = 0; columnIndex < size; columnIndex++) {
				builder.append(selection.getText(columnIndex));
				builder.append(DELIMITER);
			}
			builder.append(operatingSystemUtils.getLineDelimiter());
		}
		/*
		 * If the builder is empty, give a note that items needs to be selected.
		 */
		if(builder.length() == 0) {
			builder.append("Please select one or more entries in the list.");
			builder.append(operatingSystemUtils.getLineDelimiter());
		}
		/*
		 * Transfer the selected text (items) to the clipboard.
		 */
		TextTransfer textTransfer = TextTransfer.getInstance();
		Object[] data = new Object[]{builder.toString()};
		Transfer[] dataTypes = new Transfer[]{textTransfer};
		clipboard.setContents(data, dataTypes);
	}

	/**
	 * Creates the columns for the peak table.
	 * 
	 * @param tableViewer
	 */
	public void createColumns(final String[] titles, final int[] bounds) {

		final Table table = this.getTable();
		/*
		 * Clear the table and all existing columns.
		 */
		table.setRedraw(false);
		table.clearAll();
		while(table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		table.setRedraw(true);
		refresh();
		/*
		 * Set the columns.
		 */
		for(int i = 0; i < titles.length; i++) {
			/*
			 * Column sort.
			 */
			final int index = i;
			final TableViewerColumn tableViewerColumn = new TableViewerColumn(this, SWT.NONE);
			final TableColumn tableColumn = tableViewerColumn.getColumn();
			tableColumn.setText(titles[i]);
			tableColumn.setWidth(bounds[i]);
			tableColumn.setResizable(true);
			tableColumn.setMoveable(true);
			tableColumn.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					ViewerComparator viewerComparator = getComparator();
					if(viewerComparator instanceof IRecordTableComparator) {
						/*
						 * Only sort if a record table sorter has been set.
						 */
						IRecordTableComparator recordTableComparator = (IRecordTableComparator)viewerComparator;
						recordTableComparator.setColumn(index);
						int direction = table.getSortDirection();
						if(table.getSortColumn() == tableColumn) {
							/*
							 * Toggle the sort direction
							 */
							direction = (direction == SWT.UP) ? SWT.DOWN : SWT.UP;
						} else {
							direction = SWT.UP;
						}
						table.setSortDirection(direction);
						table.setSortColumn(tableColumn);
						refresh();
					}
				}
			});
		}
		/*
		 * Set header and lines visible.
		 */
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
}
