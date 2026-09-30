/*
 * Copyright (c) 2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.forwardnode.edit.destination.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.frontend.util.UIS;
import org.weasis.core.util.annotations.Generated;

/**
 * DICOM conformance report configuration, kept separate from the notification settings:
 * it has its own activation and its own recipient list (with a fallback to the
 * notification emails when left blank). Custom report options will be added here.
 */
@Generated()
@NullUnmarked
public class ConformanceReportComponent extends VerticalLayout {

	@Setter
	@Getter
	private Checkbox virtualDestination;

	@Setter
	@Getter
	private Checkbox buildConformanceReport;

	@Setter
	@Getter
	private TextField conformanceReportNotify;

	@Setter
	@Getter
	private Checkbox checkValueConformity;

	@Setter
	@Getter
	private Checkbox deepSequenceValidation;

	@Setter
	@Getter
	private Checkbox imageIdentityCheck;

	private Div optionsDiv;

	public ConformanceReportComponent() {
		setWidthFull();
		setPadding(false);
		getStyle().set("padding", "0.5rem 0.75rem");

		buildComponents();
		buildListeners();
		addComponents();
	}

	private void buildComponents() {
		buildOptionsDiv();
		buildVirtualDestination();
		buildBuildConformanceReport();
		buildConformanceReportNotify();
		buildCheckValueConformity();
		buildDeepSequenceValidation();
		buildImageIdentityCheck();
	}

	private void buildVirtualDestination() {
		virtualDestination = new Checkbox("虚拟目的地（仅报告，丢弃 DICOM）");
		// By default deactivate
		virtualDestination.setValue(false);
		UIS.setTooltip(virtualDestination,
				"不向最终节点转发任何内容：仅验证每个检查并发送符合性报告邮件。"
						+ "DICOM 被路由至 devnull，交付选项（主机/端口或 URL、传输语法、"
						+ "通知等）将被禁用。");
	}

	private void buildBuildConformanceReport() {
		buildConformanceReport = new Checkbox("生成 DICOM 符合性报告");
		// By default deactivate
		buildConformanceReport.setValue(false);
		UIS.setTooltip(buildConformanceReport,
				"对此目的地发送的每个检查进行 DICOM 标准验证，并通过邮件发送符合性报告");
	}

	private void buildConformanceReportNotify() {
		conformanceReportNotify = new TextField("符合性报告：邮箱列表");
		conformanceReportNotify.setWidth("100%");
		conformanceReportNotify.setHelperText("留空则复用通知邮箱列表");
		UIS.setTooltip(conformanceReportNotify,
				"符合性报告发送目标的逗号分隔邮箱列表。留空时使用通知邮箱列表。");
	}

	private void buildCheckValueConformity() {
		checkValueConformity = new Checkbox("检查值内容符合性（VR 规则）");
		// By default deactivate: real-world data often deviates from VR length/format
		// rules
		checkValueConformity.setValue(false);
		UIS.setTooltip(checkValueConformity,
				"同时报告违反 VR 长度或格式规则（PS3.5）的值，例如过长的字符串或格式错误的日期");
	}

	private void buildDeepSequenceValidation() {
		deepSequenceValidation = new Checkbox("深度序列验证（SR、功能组）");
		// By default deactivate: deeper recursion enlarges the in-memory snapshot
		deepSequenceValidation.setValue(false);
		UIS.setTooltip(deepSequenceValidation,
				"在每个序列层级递归执行符合性检查（例如 SR 内容树或增强多帧功能组），而非仅检查第一层");
	}

	private void buildImageIdentityCheck() {
		imageIdentityCheck = new Checkbox("检查图像中烧录的标识数据");
		// By default deactivate: relies on the external de-identification image service
		imageIdentityCheck.setValue(false);
		// Always-visible reminder: this option calls out to the external OCR service
		imageIdentityCheck.setHelperText(
				"需要去标识图像服务在 OCR_URL 处运行且可访问（默认 http://localhost:8000）");
		UIS.setTooltip(imageIdentityCheck,
				"对每个转发的图像运行 OCR（通过去标识图像服务），并在符合性报告中列出像素数据中仍可见的患者标识 DICOM Tag 值。"
						+ "这需要外部去标识图像服务在 OCR_URL 处运行且可访问"
						+ "（默认 http://localhost:8000）。服务不可用时无法分析图像，"
						+ "报告将其标记为未分析；符合性报告的其余部分不受影响。");
	}

	private void buildOptionsDiv() {
		optionsDiv = new Div();
		// By default hide
		optionsDiv.setVisible(false);
		optionsDiv.setWidthFull();
	}

	private void buildListeners() {
		buildConformanceReport.addValueChangeListener(
				event -> optionsDiv.setVisible(isVirtual() || Boolean.TRUE.equals(buildConformanceReport.getValue())));
		virtualDestination.addValueChangeListener(event -> applyVirtualReportOnly());
	}

	/**
	 * A virtual destination is report-only, so the conformance report is mandatory: force
	 * it on, show its options, and prevent toggling it off while virtual is selected.
	 */
	private void applyVirtualReportOnly() {
		boolean virtual = isVirtual();
		if (virtual) {
			buildConformanceReport.setValue(true);
		}
		buildConformanceReport.setReadOnly(virtual);
		optionsDiv.setVisible(virtual || Boolean.TRUE.equals(buildConformanceReport.getValue()));
	}

	private boolean isVirtual() {
		return Boolean.TRUE.equals(virtualDestination.getValue());
	}

	private void addComponents() {
		optionsDiv
			.add(UIS.setWidthFull(new VerticalLayout(UIS.setWidthFull(new HorizontalLayout(conformanceReportNotify)),
					UIS.setWidthFull(new HorizontalLayout(checkValueConformity)),
					UIS.setWidthFull(new HorizontalLayout(deepSequenceValidation)),
					UIS.setWidthFull(new HorizontalLayout(imageIdentityCheck)))));
		add(UIS.setWidthFull(new HorizontalLayout(virtualDestination)),
				UIS.setWidthFull(new HorizontalLayout(buildConformanceReport)), optionsDiv);
	}

	/**
	 * Init binder for the component
	 * @param binder Binder
	 */
	public void init(Binder<DestinationEntity> binder) {
		binder.forField(getVirtualDestination())
			.bind(DestinationEntity::isVirtualDestination, DestinationEntity::setVirtualDestination);

		binder.forField(getBuildConformanceReport())
			.bind(DestinationEntity::isBuildConformanceReport, DestinationEntity::setBuildConformanceReport);

		binder.forField(getConformanceReportNotify())
			.bind(DestinationEntity::getConformanceReportNotify, DestinationEntity::setConformanceReportNotify);

		binder.forField(getCheckValueConformity())
			.bind(DestinationEntity::isCheckValueConformity, DestinationEntity::setCheckValueConformity);

		binder.forField(getDeepSequenceValidation())
			.bind(DestinationEntity::isDeepSequenceValidation, DestinationEntity::setDeepSequenceValidation);

		binder.forField(getImageIdentityCheck())
			.bind(DestinationEntity::isImageIdentityCheck, DestinationEntity::setImageIdentityCheck);
	}

}
