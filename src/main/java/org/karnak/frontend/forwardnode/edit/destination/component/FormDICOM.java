/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.forwardnode.edit.destination.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.frontend.component.BoxShadowComponent;
import org.karnak.frontend.component.converter.HStringToIntegerConverter;
import org.karnak.frontend.forwardnode.edit.component.ButtonSaveDeleteCancel;
import org.karnak.frontend.util.UIS;
import org.weasis.core.util.annotations.Generated;

@Generated()
@NullUnmarked
public class FormDICOM extends VerticalLayout {

	private Binder<DestinationEntity> binder;

	private TextField aeTitle;

	private TextField description;

	private TextField hostname;

	private TextField port;

	private TextField concurrentConnections;

	private Checkbox useAETitleCheckbox;

	private Checkbox activate;

	@Getter
	private final DeIdentificationComponent deIdentificationComponent;

	@Getter
	private final TagMorphingComponent tagMorphingComponent;

	@Getter
	private final FilterBySOPClassesForm filterBySOPClassesForm;

	private final DestinationCondition destinationCondition;

	@Getter
	private final NotificationComponent notificationComponent;

	@Getter
	private final ConformanceReportComponent conformanceReportComponent;

	@Getter
	private final TransferSyntaxComponent transferSyntaxComponent;

	@Getter
	private final TranscodeOnlyUncompressedComponent transcodeOnlyUncompressedComponent;

	public FormDICOM() {
		this.deIdentificationComponent = new DeIdentificationComponent();
		this.tagMorphingComponent = new TagMorphingComponent();
		this.filterBySOPClassesForm = new FilterBySOPClassesForm();
		this.destinationCondition = new DestinationCondition();
		this.notificationComponent = new NotificationComponent();
		this.conformanceReportComponent = new ConformanceReportComponent();
		this.transferSyntaxComponent = new TransferSyntaxComponent();
		this.transcodeOnlyUncompressedComponent = new TranscodeOnlyUncompressedComponent();
	}

	public void init(Binder<DestinationEntity> binder, ButtonSaveDeleteCancel buttonSaveDeleteCancel) {
		this.binder = binder;
		deIdentificationComponent.init(this.binder);
		tagMorphingComponent.init(this.binder);
		filterBySOPClassesForm.init(this.binder);
		destinationCondition.init(this.binder);
		notificationComponent.init(this.binder);
		conformanceReportComponent.init(this.binder);
		transferSyntaxComponent.init(this.binder);
		transcodeOnlyUncompressedComponent.init(this.binder);

		setSizeFull();
		setPadding(false);
		setSpacing(true);
		buttonSaveDeleteCancel.getStyle().set("padding-bottom", "var(--vaadin-gap-l)");

		aeTitle = new TextField("AE Title");
		description = new TextField("描述");
		hostname = new TextField("主机名");
		port = new TextField("端口");
		concurrentConnections = new TextField("并发连接数");
		useAETitleCheckbox = new Checkbox("使用目的地 AE Title");
		activate = new Checkbox("启用目的地");

		// Define layout
		VerticalLayout destinationLayout = new VerticalLayout(
				UIS.setWidthFull(new HorizontalLayout(aeTitle, description)), destinationCondition,
				UIS.setWidthFull(new HorizontalLayout(hostname, port, concurrentConnections)));
		VerticalLayout transferLayout = new VerticalLayout(
				new HorizontalLayout(transferSyntaxComponent, transcodeOnlyUncompressedComponent));
		VerticalLayout useaetdestLayout = new VerticalLayout(new HorizontalLayout(useAETitleCheckbox));
		VerticalLayout activateLayout = new VerticalLayout(activate);

		// Compact card insets (was default VerticalLayout padding)
		compactCardPadding(destinationLayout, transferLayout, useaetdestLayout, activateLayout);

		// Add components
		add(UIS.setWidthFull(new BoxShadowComponent(destinationLayout)),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(transferLayout))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(useaetdestLayout))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(notificationComponent))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(conformanceReportComponent))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(tagMorphingComponent))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(deIdentificationComponent))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(filterBySOPClassesForm))),
				UIS.setWidthFull(new BoxShadowComponent(UIS.setWidthFull(activateLayout))),
				UIS.setWidthFull(buttonSaveDeleteCancel));

		setElements();
		setBinder();

		// When the destination becomes virtual (report-only) the delivery fields are
		// irrelevant: disable them. Re-validate on user toggle so stale mandatory-field
		// errors clear once the fields no longer apply.
		conformanceReportComponent.getVirtualDestination().addValueChangeListener(event -> {
			updateVirtualState();
			if (event.isFromClient()) {
				binder.validate();
			}
		});
	}

	/**
	 * Enable or disable the delivery-related fields depending on whether the destination
	 * is virtual (report-only). Kept package-visible so the parent view can re-apply it
	 * after reading a bean.
	 */
	public void updateVirtualState() {
		boolean delivery = !isVirtual();
		aeTitle.setEnabled(delivery);
		hostname.setEnabled(delivery);
		port.setEnabled(delivery);
		concurrentConnections.setEnabled(delivery);
		useAETitleCheckbox.setEnabled(delivery);
		transferSyntaxComponent.setEnabled(delivery);
		notificationComponent.setEnabled(delivery);
		if (!delivery) {
			// Transcode is otherwise driven by the selected transfer syntax; only force
			// it off for a virtual destination.
			transcodeOnlyUncompressedComponent.setEnabled(false);
		}
	}

	private boolean isVirtual() {
		return Boolean.TRUE.equals(conformanceReportComponent.getVirtualDestination().getValue());
	}

	private static void compactCardPadding(VerticalLayout... layouts) {
		for (VerticalLayout layout : layouts) {
			layout.setPadding(false);
			layout.getStyle().set("padding", "0.5rem 0.75rem");
		}
	}

	private void setElements() {
		aeTitle.setWidth("30%");

		description.setWidth("70%");

		hostname.setWidth("70%");
		hostname.setRequired(true);

		port.setWidth("30%");
		port.addThemeVariants(TextFieldVariant.ALIGN_RIGHT);

		concurrentConnections.setWidth("30%");
		concurrentConnections.addThemeVariants(TextFieldVariant.ALIGN_RIGHT);
		UIS.setTooltip(concurrentConnections,
				"与此目的地建立的并行 DICOM 关联数（1 = 单连接）。"
						+ "多源转发负载较高时可增大此值，但请勿超过目的地 PACS 的并发关联上限。");

		UIS.setTooltip(useAETitleCheckbox,
				"若启用，则使用目的地 AE Title 作为 Calling AE Title，而非转发节点 AE Title");
	}

	private void setBinder() {
		// A virtual (report-only) destination forwards nothing, so the delivery fields
		// are
		// not mandatory: every delivery validator is bypassed while "virtual" is checked.
		binder.forField(aeTitle)
			.withValidator(value -> isVirtual() || StringUtils.isNotBlank(value), "AE Title 为必填项")
			.withValidator(value -> isVirtual() || value.length() <= 16, "AE Title 超过 16 个字符")
			.withValidator(value -> isVirtual() || UIS.containsNoWhitespace(value), "AE Title 包含空格")
			.bind(DestinationEntity::getAeTitle, DestinationEntity::setAeTitle);

		binder.forField(description).bind(DestinationEntity::getDescription, DestinationEntity::setDescription);
		binder.forField(hostname)
			.withValidator(value -> isVirtual() || StringUtils.isNotBlank(value), "主机名为必填项")
			.bind(DestinationEntity::getHostname, DestinationEntity::setHostname);
		binder.forField(port)
			.withConverter(new HStringToIntegerConverter())
			.withValidator(value -> isVirtual() || Objects.nonNull(value), "端口为必填项")
			.withValidator(value -> isVirtual() || (1 <= value && value <= 65535), "端口应在 1 至 65535 之间")
			.bind(DestinationEntity::getPort, DestinationEntity::setPort);

		binder.forField(concurrentConnections)
			.withConverter(new HStringToIntegerConverter())
			.withValidator(value -> isVirtual() || Objects.nonNull(value), "并发连接数为必填项")
			.withValidator(value -> isVirtual() || (1 <= value && value <= 50),
					"并发连接数应在 1 至 50 之间")
			.bind(DestinationEntity::getConcurrentConnections, DestinationEntity::setConcurrentConnections);

		binder.forField(useAETitleCheckbox).bind(DestinationEntity::getUseaetdest, DestinationEntity::setUseaetdest);

		binder.forField(activate).bind(DestinationEntity::isActivate, DestinationEntity::setActivate);
		binder.bindInstanceFields(this);
	}

}
