/*
 * Copyright (c) 2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.dicom;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.model.dicom.result.DicomNodeCheckHistory;
import org.weasis.core.util.StringUtil;

/**
 * Read-only history of past DICOM Echo checks (newest first), with a per-row details
 * panel showing the DICOM and network outcomes. Mirrors {@link DicomNodeCheckResultGrid}
 * but binds to the persisted {@link DicomNodeCheckHistory} view model and adds a "Checked
 * At" timestamp column.
 */
@NullUnmarked
public class DicomNodeCheckHistoryGrid extends Grid<DicomNodeCheckHistory> {

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		.withZone(ZoneId.systemDefault());

	public DicomNodeCheckHistoryGrid() {
		super(DicomNodeCheckHistory.class, false);

		init();
	}

	private void init() {
		// Display details on row click
		setDetailsVisibleOnClick(true);
		setItemDetailsRenderer(createItemDetailsRenderer());

		// Empty grid case
		setEmptyStateText("无历史检查记录");

		// Selection mode
		setSelectionMode(SelectionMode.NONE);

		// Styling grid
		addThemeVariants(GridVariant.WRAP_CELL_CONTENT);

		addColumns();
	}

	private void addColumns() {
		addColumn(DicomNodeCheckHistoryGrid::formatCheckedAt).setHeader("检查时间")
			.setAutoWidth(true)
			.setFlexGrow(0)
			.setSortable(true);
		addColumn(createDicomNodeRenderer()).setHeader("DICOM 节点");
		addColumn(DicomNodeCheckHistory::getCallingAeTitle).setHeader("Calling AE Title").setAutoWidth(true);
		addColumn(createEchoStatusRenderer()).setHeader("DICOM Echo");
		addColumn(createConnectionRenderer()).setHeader("连接时间 (ms)");
		addColumn(createExecutionRenderer()).setHeader("执行时间 (ms)");
		addColumn(createNetworkStatusRenderer()).setHeader("网络检查");
	}

	private static String formatCheckedAt(DicomNodeCheckHistory item) {
		Instant checkedAt = (item != null) ? item.getCheckedAt() : null;
		return (checkedAt != null) ? TIMESTAMP_FORMATTER.format(checkedAt) : "";
	}

	private static ComponentRenderer<Div, DicomNodeCheckHistory> createDicomNodeRenderer() {
		return new ComponentRenderer<>((item) -> {
			Div div = new Div();

			if (item != null) {
				String description = (item.getCalledDescription() != null) ? item.getCalledDescription() : "";
				div.add(new Div(description));
				div.add(new Div(item.getCalledNetworkDetails()));
			}

			return div;
		});
	}

	private static ComponentRenderer<Badge, DicomNodeCheckHistory> createEchoStatusRenderer() {
		return new ComponentRenderer<>((item) -> {
			Badge badge = new Badge();

			if (item != null) {
				initBadge(badge, item.isEchoSuccessful());
			}

			return badge;
		});
	}

	private static ComponentRenderer<Badge, DicomNodeCheckHistory> createNetworkStatusRenderer() {
		return new ComponentRenderer<>((item) -> {
			Badge badge = new Badge();

			if (item != null) {
				initBadge(badge, item.isNetworkReachable());
			}

			return badge;
		});
	}

	private static void initBadge(Badge badge, boolean isSuccessful) {
		badge.addThemeVariants(isSuccessful ? BadgeVariant.SUCCESS : BadgeVariant.ERROR);
		badge.setText(isSuccessful ? "成功" : "失败");
	}

	private static ComponentRenderer<Div, DicomNodeCheckHistory> createConnectionRenderer() {
		return new ComponentRenderer<>((item) -> {
			Div div = new Div();

			if (item != null && item.getConnectionMs() != null) {
				div.setText(String.valueOf(item.getConnectionMs()));
			}

			return div;
		});
	}

	private static ComponentRenderer<Div, DicomNodeCheckHistory> createExecutionRenderer() {
		return new ComponentRenderer<>((item) -> {
			Div div = new Div();

			if (item != null && item.getExecutionMs() != null) {
				div.setText(String.valueOf(item.getExecutionMs()));
			}

			return div;
		});
	}

	private static ComponentRenderer<HorizontalLayout, DicomNodeCheckHistory> createItemDetailsRenderer() {
		return new ComponentRenderer<>((item) -> {
			HorizontalLayout layout = new HorizontalLayout();
			layout.setWidthFull();
			layout.setMargin(false);
			layout.setPadding(false);
			layout.setSpacing(true);
			layout.getStyle().set("font-size", "var(--aura-font-size-s)");

			if (item != null) {
				layout.add(createDicomStatusLayout(item));
				layout.add(createNetworkStatusLayout(item));
			}

			return layout;
		});
	}

	private static VerticalLayout createDicomStatusLayout(DicomNodeCheckHistory item) {
		VerticalLayout layout = detailsSection("DICOM 状态");

		UnorderedList unorderedList = new UnorderedList();

		if (item.getEchoErrorMessage() != null) {
			unorderedList.add(new ListItem("意外错误：" + item.getEchoErrorMessage()));
		}
		else if (item.getEchoRejectionReason() != null) {
			unorderedList.add(new ListItem("关联被拒绝：" + item.getEchoRejectionReason()));
		}
		else if (item.getEchoVerificationUnsupportedMessage() != null) {
			unorderedList.add(new ListItem(item.getEchoVerificationUnsupportedMessage()));
			addPeerIdentity(unorderedList, item);
		}
		else {
			addIfPresent(unorderedList, "状态码：", item.getEchoStatusHex());
			addIfPresent(unorderedList, "状态消息：", item.getEchoStatusMessage());
			addPeerIdentity(unorderedList, item);
		}

		layout.add(unorderedList);

		return layout;
	}

	private static void addPeerIdentity(UnorderedList list, DicomNodeCheckHistory item) {
		addIfPresent(list, "对端实现：", item.getRemoteImplVersionName());
		addIfPresent(list, "对端类 UID：", item.getRemoteImplClassUid());
	}

	private static void addIfPresent(UnorderedList list, String label, String value) {
		if (StringUtil.hasText(value)) {
			list.add(new ListItem(label + value));
		}
	}

	private static VerticalLayout createNetworkStatusLayout(DicomNodeCheckHistory item) {
		VerticalLayout layout = detailsSection("网络状态");

		UnorderedList unorderedList = new UnorderedList();
		addIfPresent(unorderedList, "", item.getNetworkHostnameMessage());
		addIfPresent(unorderedList, "", item.getNetworkPortMessage());
		addIfPresent(unorderedList, "", item.getNetworkQualityMessage());

		layout.add(unorderedList);

		return layout;
	}

	private static VerticalLayout detailsSection(String title) {
		VerticalLayout layout = new VerticalLayout();
		layout.setWidthFull();
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setSpacing(false);

		H6 header = new H6(title);
		header.getStyle().set("margin-top", "0px");
		layout.add(header);

		return layout;
	}

}