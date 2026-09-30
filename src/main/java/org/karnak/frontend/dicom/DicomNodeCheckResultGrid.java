/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.util.function.Consumer;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.model.dicom.result.DicomEchoResult;
import org.karnak.backend.model.dicom.result.DicomNodeCheckResult;
import org.karnak.backend.model.dicom.result.NetworkCheckResult;
import org.weasis.core.util.StringUtil;

/**
 * Grid presenting one {@link DicomNodeCheckResult} per row: the node identity, success /
 * error badges for the DICOM echo and the network check, the measured connection and
 * execution durations, and an expandable details row with the DICOM and network messages.
 */
@NullUnmarked
public class DicomNodeCheckResultGrid extends Grid<DicomNodeCheckResult> {

	private Consumer<DicomNodeCheckResult> capabilityProbeAction;

	public DicomNodeCheckResultGrid() {
		super(DicomNodeCheckResult.class, false);

		init();
	}

	/**
	 * Enables an on-demand "Capabilities" action column. The given callback is invoked
	 * with the row's result when its button is pressed; without it the column is not
	 * shown.
	 */
	public void setCapabilityProbeAction(Consumer<DicomNodeCheckResult> action) {
		this.capabilityProbeAction = action;
		addColumn(new ComponentRenderer<>(this::createCapabilityButton)).setHeader("能力")
			.setAutoWidth(true)
			.setFlexGrow(0);
	}

	private Button createCapabilityButton(DicomNodeCheckResult result) {
		Button button = new Button("探测", (event) -> capabilityProbeAction.accept(result));
		button.addThemeVariants(ButtonVariant.SMALL, ButtonVariant.TERTIARY);
		return button;
	}

	private void init() {
		// Display details on row click
		setDetailsVisibleOnClick(true);
		setItemDetailsRenderer(createItemDetailsRenderer());

		// Empty grid case
		setEmptyStateText("未找到结果");

		// Selection mode
		setSelectionMode(SelectionMode.NONE);

		// Styling grid
		addThemeVariants(GridVariant.WRAP_CELL_CONTENT);

		addColumns();
	}

	private void addColumns() {
		addColumn(createDicomNodeRenderer()).setHeader("DICOM 节点");
		addColumn(createDicomStatusRenderer()).setHeader("DICOM Echo");
		addColumn(createConnectionRenderer()).setHeader("连接时间 (ms)");
		addColumn(createExecutionRenderer()).setHeader("执行时间 (ms)");
		addColumn(createNetworkStatusRenderer()).setHeader("网络检查");
	}

	private static ComponentRenderer<Div, DicomNodeCheckResult> createDicomNodeRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			Div div = new Div();

			if (dicomNodeCheckResult != null) {
				div.add(new Div(dicomNodeCheckResult.getCalledNodeDescription()));
				div.add(new Div(dicomNodeCheckResult.getCalledNodeNetworkDetails()));
			}

			return div;
		});
	}

	private static ComponentRenderer<Badge, DicomNodeCheckResult> createDicomStatusRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			Badge badge = new Badge();

			if (dicomNodeCheckResult != null) {
				DicomEchoResult dicomEchoResult = dicomNodeCheckResult.getDicomEchoResult();
				if (dicomEchoResult != null) {
					initBadge(badge, dicomEchoResult.isSuccessful());
				}
			}

			return badge;
		});
	}

	private static ComponentRenderer<Badge, DicomNodeCheckResult> createNetworkStatusRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			Badge badge = new Badge();

			if (dicomNodeCheckResult != null) {
				NetworkCheckResult networkCheckResult = dicomNodeCheckResult.getNetworkCheckResult();
				if (networkCheckResult != null) {
					initBadge(badge, networkCheckResult.isSuccessful());
				}
			}

			return badge;
		});
	}

	private static void initBadge(Badge badge, boolean isSuccessful) {
		badge.addThemeVariants(isSuccessful ? BadgeVariant.SUCCESS : BadgeVariant.ERROR);
		badge.setText(isSuccessful ? "成功" : "失败");
	}

	private static ComponentRenderer<Div, DicomNodeCheckResult> createConnectionRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			Div div = new Div();

			if (dicomNodeCheckResult != null) {
				DicomEchoResult dicomEchoResult = dicomNodeCheckResult.getDicomEchoResult();
				if (dicomEchoResult != null) {
					Long connectionDurationInMs = dicomEchoResult.getConnectionDurationInMs();
					if (connectionDurationInMs != null) {
						div.setText(String.valueOf(connectionDurationInMs));
					}
				}
			}

			return div;
		});
	}

	private static ComponentRenderer<Div, DicomNodeCheckResult> createExecutionRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			Div div = new Div();

			if (dicomNodeCheckResult != null) {
				DicomEchoResult dicomEchoResult = dicomNodeCheckResult.getDicomEchoResult();
				if (dicomEchoResult != null) {
					Long executionDurationInMs = dicomEchoResult.getExecutionDurationInMs();
					if (executionDurationInMs != null) {
						div.setText(String.valueOf(executionDurationInMs));
					}
				}
			}

			return div;
		});
	}

	private static ComponentRenderer<HorizontalLayout, DicomNodeCheckResult> createItemDetailsRenderer() {
		return new ComponentRenderer<>((dicomNodeCheckResult) -> {
			HorizontalLayout layout = new HorizontalLayout();
			layout.setWidthFull();
			layout.setMargin(false);
			layout.setPadding(false);
			layout.setSpacing(true);
			layout.getStyle().set("font-size", "var(--aura-font-size-s)");

			if (dicomNodeCheckResult != null) {
				layout.add(createDicomStatusLayout(dicomNodeCheckResult.getDicomEchoResult()));
				layout.add(createNetworkStatusLayout(dicomNodeCheckResult.getNetworkCheckResult()));
			}

			return layout;
		});
	}

	private static VerticalLayout createDicomStatusLayout(DicomEchoResult dicomEchoResult) {
		VerticalLayout layout = detailsSection("DICOM 状态");

		if (dicomEchoResult != null) {
			UnorderedList unorderedList = new UnorderedList();

			if (dicomEchoResult.isUnexpectedError()) {
				unorderedList.add(new ListItem("意外错误：" + dicomEchoResult.getUnexpectedErrorMessage()));
			}
			else if (dicomEchoResult.isRejected()) {
				unorderedList.add(new ListItem("关联被拒绝：" + dicomEchoResult.getRejectionReason()));
			}
			else if (dicomEchoResult.isVerificationUnsupported()) {
				unorderedList.add(new ListItem(dicomEchoResult.getVerificationUnsupportedMessage()));
				addIfPresent(unorderedList, "对端实现：",
						dicomEchoResult.getRemoteImplementationVersionName());
				addIfPresent(unorderedList, "对端类 UID：", dicomEchoResult.getRemoteImplementationClassUid());
			}
			else {
				unorderedList.add(new ListItem("状态码：" + dicomEchoResult.getDicomStatusInHex()));
				addIfPresent(unorderedList, "状态消息：", dicomEchoResult.getDicomStatusMessage());
				addIfPresent(unorderedList, "对端实现：",
						dicomEchoResult.getRemoteImplementationVersionName());
				addIfPresent(unorderedList, "对端类 UID：", dicomEchoResult.getRemoteImplementationClassUid());
			}

			layout.add(unorderedList);
		}

		return layout;
	}

	private static void addIfPresent(UnorderedList list, String label, String value) {
		if (StringUtil.hasText(value)) {
			list.add(new ListItem(label + value));
		}
	}

	private static VerticalLayout createNetworkStatusLayout(NetworkCheckResult networkCheckResult) {
		VerticalLayout layout = detailsSection("网络状态");

		if (networkCheckResult != null) {
			UnorderedList unorderedList = new UnorderedList();
			unorderedList.add(new ListItem(networkCheckResult.getCheckHostnameMessage()));
			unorderedList.add(new ListItem(networkCheckResult.getCheckPortMessage()));
			addIfPresent(unorderedList, "", networkCheckResult.getCheckQualityMessage());

			layout.add(unorderedList);
		}

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