/*
 * Copyright (c) 2022-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.monitoring;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.karnak.frontend.MainLayout;
import org.karnak.frontend.component.WarningConfirmDialog;
import org.karnak.frontend.monitoring.component.ExportDialog;
import org.karnak.frontend.monitoring.component.MonitoringDetailPanel;
import org.karnak.frontend.monitoring.component.MonitoringFilterBar;
import org.karnak.frontend.monitoring.component.MonitoringTreeGrid;
import org.karnak.frontend.monitoring.component.NodeActivityDashboard;
import org.karnak.frontend.monitoring.component.TransferStatusFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.weasis.core.util.annotations.Generated;

/**
 * Monitoring view: a shared date-range filter over two tabs — an Activity tab showing the
 * Destination / Study / Series hierarchy (with one error-reason line per failing series)
 * and a Dashboard tab showing per-forward-node activity.
 */
@Route(value = MonitoringView.ROUTE, layout = MainLayout.class)
@PageTitle("Karnak - 监控")
@RolesAllowed("admin")
@Generated()
public class MonitoringView extends VerticalLayout {

	public static final String VIEW_NAME = "监控";

	public static final String ROUTE = "monitoring";

	private final MonitoringLogic monitoringLogic;

	private MonitoringFilterBar filterBar;

	private MonitoringTreeGrid treeGrid;

	private MonitoringDetailPanel detailPanel;

	private NodeActivityDashboard dashboard;

	private ExportDialog exportDialog;

	private Component activityPanel;

	private Tabs tabs;

	private Tab activityTab;

	private Tab dashboardTab;

	private final Div content = new Div();

	private Button expandErrorsButton;

	private boolean errorsExpanded;

	@Autowired
	public MonitoringView(final MonitoringLogic monitoringLogic) {
		this.monitoringLogic = monitoringLogic;
		this.monitoringLogic.setMonitoringView(this);

		buildComponents();
		addComponentsView();
	}

	/** The filter currently applied (used by the CSV export). */
	public TransferStatusFilter getCurrentFilter() {
		return filterBar.getFilter();
	}

	private void buildComponents() {
		filterBar = new MonitoringFilterBar(this::onFilterChanged);
		treeGrid = new MonitoringTreeGrid(monitoringLogic, filterBar::getFilter);
		detailPanel = new MonitoringDetailPanel();
		treeGrid.setSelectionListener(detailPanel::show);
		dashboard = new NodeActivityDashboard(monitoringLogic, filterBar::getFilter);
		exportDialog = new ExportDialog(
				() -> monitoringLogic.buildCsv(getCurrentFilter(), exportDialog.getExportSettings()));

		expandErrorsButton = new Button("展开错误", new Icon(VaadinIcon.WARNING));
		activityPanel = buildActivityPanel();

		activityTab = new Tab(VaadinIcon.LIST_OL.create(), new Text("传输明细"));
		dashboardTab = new Tab(VaadinIcon.DASHBOARD.create(), new Text("仪表盘"));
		tabs = new Tabs(activityTab, dashboardTab);
		tabs.addSelectedChangeListener(event -> showSelectedTab());

		content.setSizeFull();
		content.getStyle().set("min-height", "0");
		content.add(activityPanel);
	}

	private Component buildActivityPanel() {
		expandErrorsButton.addClickListener(event -> {
			if (errorsExpanded) {
				treeGrid.collapseErrors();
				resetErrorsToggle();
			}
			else {
				treeGrid.expandErrors();
				expandErrorsButton.setText("折叠错误");
				expandErrorsButton.setIcon(new Icon(VaadinIcon.CHEVRON_UP));
				errorsExpanded = true;
			}
		});

		Button refreshButton = new Button("刷新", new Icon(VaadinIcon.REFRESH));
		refreshButton.addClickListener(event -> {
			filterBar.refreshRange();
			treeGrid.refresh();
			resetErrorsToggle();
		});

		Button exportButton = new Button("导出", new Icon(VaadinIcon.DOWNLOAD_ALT));
		exportButton.addClickListener(event -> exportDialog.open());

		Button deleteButton = new Button("全部删除", new Icon(VaadinIcon.TRASH));
		deleteButton.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY);
		deleteButton.addClickListener(event -> confirmDeleteAll());

		HorizontalLayout buttonLayout = new HorizontalLayout(expandErrorsButton, exportButton, refreshButton,
				deleteButton);
		buttonLayout.setWidthFull();

		VerticalLayout treeSide = new VerticalLayout(treeGrid, buttonLayout);
		treeSide.setSizeFull();
		treeSide.setFlexGrow(1, treeGrid);
		treeSide.setPadding(false);
		treeSide.getStyle().set("min-height", "0");
		treeGrid.getStyle().set("min-height", "0");

		SplitLayout split = new SplitLayout(treeSide, detailPanel);
		split.setSizeFull();
		split.setSplitterPosition(62);
		return split;
	}

	/**
	 * Restore the "Expand errors" button to its collapsed state (after a tree reload).
	 */
	private void resetErrorsToggle() {
		errorsExpanded = false;
		expandErrorsButton.setText("展开错误");
		expandErrorsButton.setIcon(new Icon(VaadinIcon.WARNING));
	}

	private void confirmDeleteAll() {
		Div dialogContent = new Div();
		dialogContent.add(new Text("即将删除所有监控条目。此操作无法撤销。确定要继续吗？"));
		WarningConfirmDialog dialog = new WarningConfirmDialog("删除所有监控条目", dialogContent, "删除", "取消");
		dialog.addConfirmationListener(event -> {
			monitoringLogic.deleteAllTransferStatus();
			treeGrid.refresh();
			resetErrorsToggle();
		});
		dialog.open();
	}

	private void onFilterChanged() {
		if (tabs.getSelectedTab() == dashboardTab) {
			dashboard.refresh();
		}
		else {
			treeGrid.refresh();
			resetErrorsToggle();
		}
	}

	private void showSelectedTab() {
		content.removeAll();
		if (tabs.getSelectedTab() == dashboardTab) {
			content.add(dashboard);
			dashboard.refresh();
		}
		else {
			content.add(activityPanel);
			treeGrid.refresh();
			resetErrorsToggle();
		}
	}

	private void addComponentsView() {
		add(filterBar, tabs, content);
		setFlexGrow(1, content);
		setSizeFull();
		setWidthFull();
	}

}
