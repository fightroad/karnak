/*
 * Copyright (c) 2022-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.monitoring.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import java.util.function.Supplier;
import org.karnak.backend.model.monitoring.NodeActivityModel;
import org.karnak.frontend.monitoring.MonitoringLogic;
import org.weasis.core.util.annotations.Generated;

/**
 * Forward-node activity dashboard: KPI cards with the totals over the selected period
 * plus a per-forward-node table (studies, series, instances, retries, sent, errors,
 * excluded, and de-identification / tag-morphing volume). Dependency-free — cards are
 * styled {@code Div}s.
 */
@Generated()
public class NodeActivityDashboard extends VerticalLayout {

	private final transient MonitoringLogic monitoringLogic;

	private final transient Supplier<TransferStatusFilter> filterSupplier;

	private final HorizontalLayout cards = new HorizontalLayout();

	private final Grid<NodeActivityModel> grid = new Grid<>(NodeActivityModel.class, false);

	public NodeActivityDashboard(MonitoringLogic monitoringLogic, Supplier<TransferStatusFilter> filterSupplier) {
		this.monitoringLogic = monitoringLogic;
		this.filterSupplier = filterSupplier;

		cards.setWidthFull();
		cards.setPadding(false);
		cards.getStyle().set("flex-wrap", "nowrap");

		grid.addColumn(NodeActivityModel::forwardAet).setHeader("转发 AE Title").setSortable(true).setFlexGrow(20);
		grid.addColumn(NodeActivityModel::studies).setHeader("检查").setSortable(true);
		grid.addColumn(NodeActivityModel::series).setHeader("序列").setSortable(true);
		grid.addColumn(NodeActivityModel::instances).setHeader("实例").setSortable(true);
		grid.addColumn(NodeActivityModel::retries).setHeader("重试").setSortable(true);
		grid.addColumn(NodeActivityModel::sent).setHeader("已发送").setSortable(true);
		grid.addColumn(NodeActivityModel::errors).setHeader("错误").setSortable(true);
		grid.addColumn(NodeActivityModel::excluded).setHeader("已排除").setSortable(true);
		grid.addColumn(NodeActivityModel::deidentified).setHeader("已去标识").setSortable(true);
		grid.addColumn(NodeActivityModel::tagMorphed).setHeader("标签变形").setSortable(true);
		grid.setWidthFull();

		add(cards, grid);
		setSizeFull();
	}

	/** Recompute the dashboard for the current filter range. */
	public void refresh() {
		List<NodeActivityModel> nodes = monitoringLogic.listNodeActivity(filterSupplier.get());
		grid.setItems(nodes);

		cards.removeAll();
		cards.add(card("检查", sum(nodes, NodeActivityModel::studies), false),
				card("序列", sum(nodes, NodeActivityModel::series), false),
				card("实例", sum(nodes, NodeActivityModel::instances), false),
				card("重试", sum(nodes, NodeActivityModel::retries), false),
				card("已发送", sum(nodes, NodeActivityModel::sent), false),
				card("错误", sum(nodes, NodeActivityModel::errors), true),
				card("已排除", sum(nodes, NodeActivityModel::excluded), false),
				card("已去标识", sum(nodes, NodeActivityModel::deidentified), false),
				card("标签变形", sum(nodes, NodeActivityModel::tagMorphed), false));
	}

	private long sum(List<NodeActivityModel> nodes, java.util.function.ToLongFunction<NodeActivityModel> extractor) {
		return nodes.stream().mapToLong(extractor).sum();
	}

	private Component card(String label, long value, boolean errorEmphasis) {
		Span number = new Span(Long.toString(value));
		number.getStyle().set("font-size", "var(--aura-font-size-xl)").set("font-weight", "700");
		if (errorEmphasis && value > 0) {
			number.addClassName("karnak-error-text");
		}
		Span caption = new Span(label);
		caption.getStyle()
			.set("color", "var(--vaadin-text-color-secondary)")
			.set("font-size", "var(--aura-font-size-s)");

		Div card = new Div(number, caption);
		card.getStyle()
			.set("display", "flex")
			.set("flex-direction", "column")
			.set("flex", "1 1 0")
			.set("min-width", "0")
			.set("padding", "var(--vaadin-gap-s) var(--vaadin-gap-m)")
			.set("border", "1px solid color-mix(in srgb, var(--vaadin-text-color) 10%, transparent)")
			.set("border-radius", "var(--vaadin-radius-l)");
		return card;
	}

}
