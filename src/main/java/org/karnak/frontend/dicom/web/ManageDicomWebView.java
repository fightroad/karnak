/*
 * Copyright (c) 2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.dicom.web;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.WebDestinationConfigEntity;
import org.karnak.backend.enums.MessageFormat;
import org.karnak.backend.enums.MessageLevel;
import org.karnak.backend.model.dicom.Message;
import org.karnak.backend.model.dicom.WebDestinationNode;
import org.karnak.backend.service.WebDestinationConfigService;
import org.karnak.backend.util.DicomNodeUtil;
import org.karnak.frontend.dicom.AbstractView;
import org.weasis.core.util.annotations.Generated;

/**
 * Management section for the persisted DICOMweb endpoints: a grid with add/edit/delete
 * and CSV import/export. The dynamic gateway STOW-RS destinations are listed read-only
 * and cannot be edited, deleted or used as an import target.
 */
@Generated()
@NullUnmarked
public class ManageDicomWebView extends AbstractView {

	private static final char CSV_SEPARATOR = ',';

	private final transient WebDestinationConfigService service;

	private final transient DicomNodeUtil dicomNodeUtil;

	private final WebDestinationManagementGrid grid;

	private final transient UI ui;

	public ManageDicomWebView(WebDestinationConfigService service, DicomNodeUtil dicomNodeUtil) {
		this.service = service;
		this.dicomNodeUtil = dicomNodeUtil;
		this.ui = UI.getCurrent();
		this.grid = createGrid();

		createMainLayout();
		refresh();
	}

	private void createMainLayout() {
		// Fill the page so the grid can flex-grow and scroll internally rather than
		// rendering at its intrinsic (header-only) height.
		setSizeFull();
		getStyle().set("min-height", "0");

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setPadding(true);
		mainLayout.setSpacing(true);
		mainLayout.getStyle().set("min-height", "0");

		H6 title = new H6("DICOMweb 端点管理");
		title.getStyle().set("margin-top", "0px");

		Button addBtn = new Button("添加端点", VaadinIcon.PLUS.create(), event -> openEditor(null));
		addBtn.addThemeVariants(ButtonVariant.PRIMARY);

		Button importExportBtn = new Button("导入/导出", VaadinIcon.EXCHANGE.create(),
				event -> openImportExportDialog());

		HorizontalLayout toolbar = new HorizontalLayout(addBtn, importExportBtn);
		toolbar.setSpacing(true);

		mainLayout.add(title, toolbar, grid);
		mainLayout.setFlexGrow(1, grid);
		add(mainLayout);
	}

	private WebDestinationManagementGrid createGrid() {
		WebDestinationManagementGrid managementGrid = new WebDestinationManagementGrid();
		managementGrid.setSizeFull();
		// Floor the grid at a few rows: it flex-shrinks with the window down to this
		// height, and only then does the page scroll.
		managementGrid.setMinHeight("200px");
		managementGrid.setEditHandler(this::openEditor);
		managementGrid.setDeleteHandler(this::deleteEndpoint);
		return managementGrid;
	}

	private void refresh() {
		List<WebDestinationConfigEntity> rows = new ArrayList<>(service.findAll());
		for (WebDestinationNode node : dicomNodeUtil.getGatewayStowDestinations()) {
			rows.add(gatewayRow(node));
		}
		grid.setItems(rows);
	}

	/**
	 * A read-only grid row (no id) for an endpoint coming from the gateway STOW
	 * destinations.
	 */
	private static WebDestinationConfigEntity gatewayRow(WebDestinationNode node) {
		var entity = new WebDestinationConfigEntity(node.description(), node.url(), "",
				DicomNodeUtil.GATEWAY_DESTINATIONS_GROUP_NAME);
		entity.setId(null);
		return entity;
	}

	private void openEditor(WebDestinationConfigEntity endpoint) {
		WebDestinationEditorDialog dialog = new WebDestinationEditorDialog(endpoint, service.getKnownGroups());
		dialog.addSaveEndpointListener(this::saveEndpoint);
		dialog.open();
	}

	private void saveEndpoint(WebDestinationEditorDialog.SaveEndpointEvent event) {
		try {
			if (event.getEndpointId() != null) {
				service.update(event.getEndpointId(), event.getDescription(), event.getUrl(), event.getServices(),
						event.getGroup());
			}
			else {
				service.save(event.getDescription(), event.getUrl(), event.getServices(), event.getGroup());
			}
			refresh();
			displayMessage(new Message(MessageLevel.INFO, MessageFormat.TEXT, "DICOMweb 端点已保存"));
		}
		catch (Exception ex) {
			displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
					"无法保存 DICOMweb 端点：" + ex.getMessage()));
		}
	}

	private void deleteEndpoint(WebDestinationConfigEntity endpoint) {
		if (endpoint.getId() == null) {
			return;
		}
		try {
			service.delete(endpoint.getId());
			refresh();
			displayMessage(new Message(MessageLevel.INFO, MessageFormat.TEXT, "DICOMweb 端点已删除"));
		}
		catch (Exception ex) {
			displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
					"无法删除 DICOMweb 端点：" + ex.getMessage()));
		}
	}

	private void openImportExportDialog() {
		List<String> knownGroups = service.getKnownGroups();

		ComboBox<String> importGroup = groupCombo("导入到分组", "留空 = 使用文件中的分组列",
				knownGroups);
		importGroup.setAllowCustomValue(true);
		importGroup.addCustomValueSetListener(event -> importGroup.setValue(event.getDetail()));
		Checkbox replace = new Checkbox("替换范围内的现有项");

		ComboBox<String> exportGroup = groupCombo("导出分组", "留空 = 所有分组", knownGroups);

		Upload importUpload = new Upload((UploadHandler) event -> {
			byte[] bytes;
			try (InputStream in = event.getInputStream()) {
				bytes = in.readAllBytes();
			}
			String targetGroup = emptyToNull(importGroup.getValue());
			boolean replaceExisting = replace.getValue();
			// Replacing without a target group wipes every endpoint, so confirm first.
			if (replaceExisting && targetGroup == null) {
				runOnUi(() -> confirmReplaceAll(() -> doImport(bytes, null, true)));
			}
			else {
				doImport(bytes, targetGroup, replaceExisting);
			}
		});
		importUpload.setDropLabel(new Span("将 CSV 文件拖放到此处"));
		importUpload.setUploadButton(new Button("导入 CSV", VaadinIcon.UPLOAD.create()));
		importUpload.setAcceptedFileTypes(".csv", "text/csv");
		importUpload.setMaxFiles(1);

		Anchor exportAnchor = new Anchor();
		exportAnchor.setHref(DownloadHandler.fromInputStream(event -> new DownloadResponse(
				new ByteArrayInputStream(service.exportCsv(emptyToNull(exportGroup.getValue()))),
				"dicomweb-endpoints.csv", "text/csv", -1)));
		exportAnchor.getElement().setAttribute("download", true);
		exportAnchor.add(new Button("导出 CSV", VaadinIcon.DOWNLOAD.create()));

		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("DICOMweb 端点导入/导出");
		dialog.add(importExportContent(importGroup, replace, importUpload, exportGroup, exportAnchor));
		dialog.getFooter().add(new Button("关闭", event -> dialog.close()));
		dialog.open();
	}

	private static VerticalLayout importExportContent(ComboBox<String> importGroup, Checkbox replace,
			Upload importUpload, ComboBox<String> exportGroup, Anchor exportAnchor) {
		HorizontalLayout importOptions = new HorizontalLayout(importGroup, replace);
		importOptions.setAlignItems(Alignment.BASELINE);
		HorizontalLayout exportRow = new HorizontalLayout(exportGroup, exportAnchor);
		exportRow.setAlignItems(Alignment.BASELINE);

		VerticalLayout content = new VerticalLayout(sectionTitle("导入"), importOptions, importUpload,
				sectionTitle("导出"), exportRow);
		content.setPadding(false);
		content.setSpacing(true);
		content.setWidth("30em");
		return content;
	}

	private static H6 sectionTitle(String text) {
		H6 title = new H6(text);
		title.getStyle().set("margin-top", "0");
		return title;
	}

	private static ComboBox<String> groupCombo(String label, String helper, List<String> groups) {
		ComboBox<String> field = new ComboBox<>(label);
		field.setItems(groups);
		field.setPlaceholder("所有分组");
		field.setClearButtonVisible(true);
		field.setHelperText(helper);
		return field;
	}

	private void doImport(byte[] bytes, String targetGroup, boolean replace) {
		try {
			WebDestinationConfigService.ImportReport report = service.importCsv(new ByteArrayInputStream(bytes),
					CSV_SEPARATOR, targetGroup, replace);
			runOnUi(() -> {
				refresh();
				showImportReport(targetGroup, report);
			});
		}
		catch (Exception ex) {
			runOnUi(() -> displayMessage(new Message(MessageLevel.ERROR, MessageFormat.TEXT,
					"无法导入 CSV 文件：" + ex.getMessage())));
		}
	}

	private void showImportReport(String targetGroup, WebDestinationConfigService.ImportReport report) {
		String scope = (targetGroup != null) ? "分组 '" + targetGroup + "'" : "所有分组";
		String removed = (report.removed() > 0) ? "（已删除 " + report.removed() + " 个）" : "";
		String summary = "已导入 " + report.imported() + " 个 DICOMweb 端点到 " + scope + removed;

		if (report.errors().isEmpty()) {
			displayMessage(new Message(MessageLevel.INFO, MessageFormat.TEXT, summary));
			return;
		}

		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("导入报告");
		dialog.add(new Div(summary + " - " + report.errors().size() + " 行已跳过："));
		UnorderedList list = new UnorderedList();
		report.errors().forEach(message -> list.add(new ListItem(message)));
		dialog.add(list);
		dialog.getFooter().add(new Button("关闭", event -> dialog.close()));
		dialog.open();
	}

	private void confirmReplaceAll(Runnable onConfirm) {
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("替换所有 DICOMweb 端点？");
		dialog.add(new Div("此操作将在导入前删除所有现有 DICOMweb 端点，且无法撤销。"));

		Button cancel = new Button("取消", event -> dialog.close());
		Button confirm = new Button("全部删除并导入", event -> {
			dialog.close();
			onConfirm.run();
		});
		confirm.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.ERROR);

		dialog.getFooter().add(cancel, confirm);
		dialog.open();
	}

	private void runOnUi(Runnable command) {
		if (ui != null) {
			ui.access(command::run);
		}
		else {
			command.run();
		}
	}

	private static String emptyToNull(String value) {
		return (value == null || value.isBlank()) ? null : value;
	}

}
