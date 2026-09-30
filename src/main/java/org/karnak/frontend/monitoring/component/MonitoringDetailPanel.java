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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.karnak.backend.util.DateFormat;
import org.karnak.frontend.monitoring.component.MonitoringNode.DestinationNode;
import org.karnak.frontend.monitoring.component.MonitoringNode.ErrorNode;
import org.karnak.frontend.monitoring.component.MonitoringNode.SeriesNode;
import org.karnak.frontend.monitoring.component.MonitoringNode.StudyNode;
import org.weasis.core.util.annotations.Generated;

/**
 * Detail panel beside the monitoring tree: shows the full set of fields for the selected
 * destination / study / series / error as read-only (so values can be selected and
 * copied), plus a "Copy" button that copies the whole block to the clipboard.
 */
@Generated()
public class MonitoringDetailPanel extends VerticalLayout {

	private record Field(String label, String value, boolean multiline, boolean header) {
	}

	private final Span title = new Span("详情");

	private final Button copyButton = new Button("复制", VaadinIcon.COPY.create());

	private final FormLayout form = new FormLayout();

	private final Span placeholder = new Span("请选择目的地、检查、序列或错误以查看详情。");

	private transient String copyText = "";

	public MonitoringDetailPanel() {
		title.getStyle().set("font-weight", "600").set("font-size", "var(--aura-font-size-l)");
		copyButton.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
		copyButton.setEnabled(false);
		copyButton.addClickListener(event -> copyToClipboard());

		placeholder.addClassName("karnak-secondary-text");
		form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		form.setWidthFull();

		// The fields scroll while the Copy button stays visible at the bottom
		VerticalLayout body = new VerticalLayout(placeholder, form);
		body.getStyle().set("padding", "8px");
		body.setSpacing(true);
		body.setWidthFull();
		Scroller scroller = new Scroller(body);
		scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		scroller.setSizeFull();

		HorizontalLayout footer = new HorizontalLayout(copyButton);
		footer.setWidthFull();
		footer.setJustifyContentMode(JustifyContentMode.END);

		setSizeFull();
		setPadding(true);
		setSpacing(true);
		add(title, scroller, footer);
		setFlexGrow(1, scroller);
	}

	/**
	 * Render the details of the selected node, or the placeholder when nothing is
	 * selected.
	 */
	public void show(MonitoringNode node) {
		form.removeAll();
		if (node == null) {
			title.setText("详情");
			placeholder.setVisible(true);
			copyButton.setEnabled(false);
			copyText = "";
			return;
		}
		placeholder.setVisible(false);
		title.setText(titleFor(node));

		List<Field> fields = fieldsFor(node);
		StringBuilder copyBuilder = new StringBuilder(title.getText()).append('\n');
		for (Field field : fields) {
			if (field.header()) {
				form.add(sectionHeader(field.label()));
				copyBuilder.append('\n').append(field.label()).append('\n');
			}
			else {
				form.addFormItem(readOnly(field), field.label());
				copyBuilder.append(field.label()).append(": ").append(field.value()).append('\n');
			}
		}
		copyText = copyBuilder.toString();
		copyButton.setEnabled(true);
	}

	/**
	 * A full-width section title separating the Patient / Study / Series / Transfer
	 * groups.
	 */
	private Span sectionHeader(String title) {
		Span span = new Span(title);
		span.getStyle()
			.set("font-weight", "600")
			.set("text-transform", "uppercase")
			.set("font-size", "var(--aura-font-size-s)")
			.set("color", "var(--vaadin-text-color-secondary)")
			.set("margin-top", "var(--vaadin-gap-s)");
		return span;
	}

	private com.vaadin.flow.component.Component readOnly(Field field) {
		if (field.multiline()) {
			TextArea area = new TextArea();
			area.setValue(field.value());
			area.setReadOnly(true);
			area.setWidthFull();
			return area;
		}
		TextField textField = new TextField();
		textField.setValue(field.value());
		textField.setReadOnly(true);
		textField.setWidthFull();
		return textField;
	}

	private String titleFor(MonitoringNode node) {
		return switch (node) {
			case DestinationNode d -> d.displayName();
			case StudyNode s -> "检查 " + StringUtils.defaultString(s.studyUid());
			case SeriesNode se -> "序列 " + StringUtils.defaultString(se.serieUid());
			case ErrorNode e -> e.errors() > 0 ? "错误" : "已排除";
		};
	}

	private List<Field> fieldsFor(MonitoringNode node) {
		List<Field> fields = new ArrayList<>();
		switch (node) {
			case DestinationNode d -> {
				text(fields, "转发 AET", d.forwardAet());
				text(fields, "目的地", d.destinationLabel());
				number(fields, "检查", d.studies());
				number(fields, "序列", d.series());
				number(fields, "实例", d.instances());
				number(fields, "重试", d.retries());
				number(fields, "已发送", d.sent());
				number(fields, "错误", d.errors());
				number(fields, "已排除", d.excluded());
			}
			case StudyNode s -> {
				section(fields, "患者");
				pair(fields, "患者 ID", s.patientIdOriginal(), s.patientIdToSend());
				section(fields, "检查");
				pair(fields, "检查 UID", s.studyUid(), s.studyUidToSend());
				pair(fields, "检查号", s.accessionNumberOriginal(), s.accessionNumberToSend());
				pair(fields, "描述", s.description(), s.descriptionToSend());
				datePair(fields, "检查日期", s.studyDateOriginal(), s.studyDateToSend());
				section(fields, "传输");
				number(fields, "序列", s.series());
				number(fields, "实例", s.instances());
				number(fields, "重试", s.retries());
				number(fields, "已发送", s.sent());
				number(fields, "错误", s.errors());
				number(fields, "已排除", s.excluded());
				date(fields, "首次出现", s.firstSeen());
				date(fields, "最后出现", s.lastSeen());
			}
			case SeriesNode se -> {
				section(fields, "患者");
				pair(fields, "患者 ID", se.patientIdOriginal(), se.patientIdToSend());
				section(fields, "检查");
				pair(fields, "检查 UID", se.studyUid(), se.studyUidToSend());
				pair(fields, "检查号", se.accessionNumberOriginal(), se.accessionNumberToSend());
				pair(fields, "描述", se.studyDescriptionOriginal(), se.studyDescriptionToSend());
				datePair(fields, "检查日期", se.studyDateOriginal(), se.studyDateToSend());
				section(fields, "序列");
				pair(fields, "序列 UID", se.serieUid(), se.serieUidToSend());
				pair(fields, "描述", se.description(), se.descriptionToSend());
				text(fields, "模态", se.modality());
				text(fields, "SOP Class", se.sopClassUids());
				datePair(fields, "序列日期", se.serieDateOriginal(), se.serieDateToSend());
				section(fields, "传输");
				number(fields, "实例", se.instances());
				number(fields, "重试", se.retries());
				number(fields, "已发送", se.sent());
				number(fields, "错误", se.errors());
				number(fields, "已排除", se.excluded());
				date(fields, "首次出现", se.firstSeen());
				date(fields, "最后出现", se.lastSeen());
			}
			case ErrorNode e -> {
				fields.add(new Field("原因", StringUtils.defaultString(e.reason()), true, false));
				number(fields, "实例", e.instances());
				number(fields, "错误", e.errors());
				number(fields, "已排除", e.excluded());
				number(fields, "重试", e.retries());
			}
		}
		return fields;
	}

	private void section(List<Field> fields, String title) {
		fields.add(new Field(title, "", false, true));
	}

	private void text(List<Field> fields, String label, String value) {
		if (StringUtils.isNotBlank(value)) {
			fields.add(new Field(label, value, false, false));
		}
	}

	private void number(List<Field> fields, String label, long value) {
		fields.add(new Field(label, Long.toString(value), false, false));
	}

	private void date(List<Field> fields, String label, LocalDateTime value) {
		if (value != null) {
			fields.add(new Field(label, formatDate(value), false, false));
		}
	}

	/**
	 * Shows the collected (original) value and, when de-identification changed it, the
	 * value actually sent on a second "(de-identified)" line — so both the original and
	 * final data are visible side by side.
	 */
	private void pair(List<Field> fields, String label, String original, String toSend) {
		if (StringUtils.isNotBlank(original)) {
			fields.add(new Field(label, original, false, false));
		}
		if (StringUtils.isNotBlank(toSend) && !Objects.equals(original, toSend)) {
			fields.add(new Field(label + "（已去标识）", toSend, false, false));
		}
	}

	/** Date variant of {@link #pair}: original date and, when changed, the sent date. */
	private void datePair(List<Field> fields, String label, LocalDateTime original, LocalDateTime toSend) {
		if (original != null) {
			fields.add(new Field(label, formatDate(original), false, false));
		}
		if (toSend != null && !Objects.equals(original, toSend)) {
			fields.add(new Field(label + "（已去标识）", formatDate(toSend), false, false));
		}
	}

	private String formatDate(LocalDateTime value) {
		return DateFormat.format(value, DateFormat.FORMAT_DDMMYYYY_SLASH_HHMMSS_2POINTS);
	}

	private void copyToClipboard() {
		copyButton.getElement().executeJs("navigator.clipboard.writeText($0).then(() => {}, () => {})", copyText);
		Notification notification = Notification.show("详情已复制到剪贴板");
		notification.addThemeVariants(NotificationVariant.SUCCESS);
		notification.setDuration(2000);
		notification.setPosition(Position.MIDDLE);
	}

}
