/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.forwardnode.edit.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.backend.data.entity.ForwardNodeEntity;
import org.karnak.backend.model.forwardnode.ForwardNodeModel;
import org.karnak.backend.util.SystemPropertyUtil;
import org.karnak.frontend.forwardnode.ForwardNodeLogic;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.annotations.Generated;
import org.weasis.dicom.op.CStore;
import org.weasis.dicom.param.DicomNode;

@Generated()
public class EditAETitleDescription extends VerticalLayout {

	private final TextField textFieldAETitle;

	private final TextField textFieldDescription;

	private final Button selectFolderButton;

	private final HorizontalLayout fieldsRow;

	private final Binder<ForwardNodeEntity> binder;

	public EditAETitleDescription(Binder<ForwardNodeEntity> binder, ForwardNodeLogic forwardNodeLogic,
			Environment environment) {
		this.binder = binder;
		setPadding(false);
		setSpacing(false);

		this.textFieldAETitle = new TextField("转发 AE Title");
		this.textFieldDescription = new TextField("描述");
		this.selectFolderButton = new Button("上传本地文件夹", VaadinIcon.FOLDER_OPEN.create());
		selectFolderButton.addClickListener(event -> {
			ForwardNodeEntity forwardNode = binder.getBean();

			if (forwardNode != null) {
				forwardNode = forwardNodeLogic.retrieveForwardNodeById(forwardNode.getId());
			}

			if (forwardNode != null && forwardNode.getDestinationEntities() != null
					&& !forwardNode.getDestinationEntities().isEmpty()
					&& forwardNode.getDestinationEntities().stream().anyMatch(DestinationEntity::isActivate)) {
				selectFolder();
			}
			else {
				Notification.show("此转发节点未配置启用的目的地", 3000,
						Notification.Position.MIDDLE);
			}
		});

		// AE title and description on one row; keep description narrower so Save/Delete/Cancel fit.
		textFieldAETitle.setWidth("30%");
		textFieldDescription.setWidth("50%");
		fieldsRow = new HorizontalLayout(textFieldAETitle, textFieldDescription);
		fieldsRow.setWidthFull();
		fieldsRow.setAlignItems(Alignment.BASELINE);
		add(fieldsRow);

		if (environment.acceptsProfiles(Profiles.of("portable"))) {
			add(buildUploadFolderRow());
		}
		setBinder();
	}

	/**
	 * Place the forward-node action buttons on the fields row, aligned with the fields.
	 */
	public void setActionButtons(Component actions) {
		fieldsRow.add(actions);
		fieldsRow.setVerticalComponentAlignment(Alignment.BASELINE, actions);
	}

	/** The local-folder upload sits on its own line with a short explanation. */
	private HorizontalLayout buildUploadFolderRow() {
		Span explanation = new Span(
				"将本地文件夹中的 DICOM 文件发送到该转发节点的已启用目的地。");
		explanation.getStyle()
			.set("color", "var(--vaadin-text-color-secondary)")
			.set("font-size", "var(--aura-font-size-s)");
		HorizontalLayout uploadRow = new HorizontalLayout(selectFolderButton, explanation);
		uploadRow.setVerticalComponentAlignment(Alignment.CENTER, selectFolderButton, explanation);
		uploadRow.getStyle().set("margin-top", "var(--vaadin-gap-s)");
		return uploadRow;
	}

	private void selectFolder() {
		Dialog dialog = new Dialog();
		dialog.setHeaderTitle("从本地文件夹上传 DICOM 文件");

		TextField pathField = new TextField("文件夹路径");
		pathField.setPlaceholder("请输入文件夹的绝对路径（例如 /home/user/documents）");
		pathField.setWidthFull();

		Span errorMessage = new Span();
		errorMessage.addClassName("karnak-error-text");
		errorMessage.setVisible(false);

		Button confirmButton = new Button("确认", e -> {
			dicomSend(pathField, dialog, errorMessage);
		});

		dialog.add(pathField, errorMessage);
		dialog.getFooter().add(new Button("取消", e -> dialog.close()), confirmButton);
		dialog.setWidth("500px");
		dialog.setHeight("195px");
		dialog.open();
	}

	private void dicomSend(TextField pathField, Dialog dialog, Span errorMessage) {
		String path = pathField.getValue();
		if (StringUtil.hasText(path)) {
			Path folderPath = Path.of(path);
			if (Files.isDirectory(folderPath)) {
				textFieldDescription.setValue(path);
				DicomNode callingNode = new DicomNode("LOCAL_FOLDER");
				int port = SystemPropertyUtil.retrieveIntegerSystemProperty("DICOM_LISTENER_PORT", 11119);
				DicomNode remoteNode = new DicomNode(textFieldAETitle.getValue(), "localhost", port);
				dialog.close();
				CompletableFuture.runAsync(() -> CStore.process(callingNode, remoteNode, List.of(path), null));
			}
			else {
				errorMessage.setText("指定路径不是有效的文件夹");
				errorMessage.setVisible(true);
			}
		}
		else {
			errorMessage.setText("请输入文件夹路径");
			errorMessage.setVisible(true);
		}
	}

	public void setForwardNode(ForwardNodeEntity forwardNodeEntity) {
		if (forwardNodeEntity != null) {
			binder.readBean(forwardNodeEntity);
			binder.setBean(forwardNodeEntity);
			setEnabled(true);
			selectFolderButton.setEnabled(true);
		}
		else {
			binder.readBean(null);
			binder.setBean(null);
			textFieldDescription.clear();
			textFieldAETitle.clear();
			setEnabled(false);
			selectFolderButton.setEnabled(false);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		textFieldAETitle.setEnabled(enabled);
		textFieldDescription.setEnabled(enabled);
	}

	private void setBinder() {
		binder.forField(textFieldAETitle)
			.withValidator(value -> !value.isEmpty(), "转发 AE Title 为必填项")
			.withValidator(value -> value.length() <= 16, "转发 AE Title 超过 16 个字符")
			// Same constraint as the REST API: the AETitle reaches the DICOM gateway
			// and its logs, where a pasted CR/LF would forge log records
			.withValidator(value -> value.matches(ForwardNodeModel.AE_TITLE),
					"转发 AE Title 包含不允许的字符")
			.bind(ForwardNodeEntity::getFwdAeTitle, ForwardNodeEntity::setFwdAeTitle);
		binder.forField(textFieldDescription)
			.withValidator(value -> value.matches(ForwardNodeModel.NO_CONTROL_CHARACTERS),
					"转发描述包含控制字符")
			.bind(ForwardNodeEntity::getFwdDescription, ForwardNodeEntity::setFwdDescription);
	}

}
