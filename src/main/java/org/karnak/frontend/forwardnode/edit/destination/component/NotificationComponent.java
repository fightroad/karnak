/*
 * Copyright (c) 2021-2026 Karnak Team and other contributors.
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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.constant.Notification;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.frontend.component.converter.HStringToIntegerConverter;
import org.karnak.frontend.util.UIS;
import org.weasis.core.util.annotations.Generated;

/**
 * Create a notification component
 */
@Generated()
@NullUnmarked
public class NotificationComponent extends VerticalLayout {

	// Components
	@Setter
	@Getter
	private TextField notify;

	@Setter
	@Getter
	private TextField notifyObjectErrorPrefix;

	@Setter
	@Getter
	private TextField notifyObjectRejectionPrefix;

	@Setter
	@Getter
	private TextField notifyObjectPattern;

	@Setter
	@Getter
	private TextField notifyObjectValues;

	@Setter
	@Getter
	private TextField notifyInterval;

	@Setter
	@Getter
	private Checkbox activateNotification;

	private Div notificationInputsDiv;

	private Div notificationObjectsDiv;

	/**
	 * Constructor
	 */
	public NotificationComponent() {
		// Size
		setWidthFull();

		setPadding(false);
		getStyle().set("padding", "0.5rem 0.75rem");

		// Build notification components
		buildComponents();

		// Build listeners
		buildListeners();

		// Add components
		addComponents();
	}

	/**
	 * Add components in notification components
	 */
	private void addComponents() {
		notificationInputsDiv.add(UIS.setWidthFull(new VerticalLayout(UIS.setWidthFull(new HorizontalLayout(notify)),
				spreadRow(new HorizontalLayout(notifyObjectErrorPrefix, notifyObjectRejectionPrefix)),
				spreadRow(new HorizontalLayout(notifyObjectPattern, notifyObjectValues, notifyInterval)))));

		add(UIS.setWidthFull(new HorizontalLayout(activateNotification)), notificationInputsDiv);
	}

	/**
	 * Make a row fill the full width and spread its fixed-width fields so the last one
	 * aligns with the right edge.
	 * @param row Row to configure
	 * @return the configured row
	 */
	private static HorizontalLayout spreadRow(HorizontalLayout row) {
		row.setWidthFull();
		row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		return row;
	}

	/**
	 * Build listeners on components
	 */
	private void buildListeners() {
		buildListenerActivateNotification();
	}

	/**
	 * Listener activate notification
	 */
	private void buildListenerActivateNotification() {
		activateNotification.addValueChangeListener(event -> {
			if (event != null && event.getValue()) {
				// Set default values if null or empty
				updateDefaultValuesNotificationTextFields();
			}
			updateNotificationInputsVisibility();
		});
	}

	/**
	 * The notification inputs (recipients...) are needed as soon as the notification is
	 * enabled
	 */
	private void updateNotificationInputsVisibility() {
		boolean visible = Boolean.TRUE.equals(activateNotification.getValue());
		notificationInputsDiv.setVisible(visible);
		notificationObjectsDiv.setVisible(visible);
	}

	/**
	 * Set default values if textfield values are null or empty
	 */
	private void updateDefaultValuesNotificationTextFields() {
		if (notifyObjectErrorPrefix.getValue() == null || notifyObjectErrorPrefix.getValue().trim().isEmpty()) {
			notifyObjectErrorPrefix.setValue(Notification.DEFAULT_SUBJECT_ERROR_PREFIX);
		}
		if (notifyObjectRejectionPrefix.getValue() == null || notifyObjectRejectionPrefix.getValue().trim().isEmpty()) {
			notifyObjectRejectionPrefix.setValue(Notification.DEFAULT_SUBJECT_REJECTION_PREFIX);
		}
		if (notifyObjectPattern.getValue() == null || notifyObjectPattern.getValue().trim().isEmpty()) {
			notifyObjectPattern.setValue(Notification.DEFAULT_SUBJECT_PATTERN);
		}
		if (notifyObjectValues.getValue() == null || notifyObjectValues.getValue().trim().isEmpty()) {
			notifyObjectValues.setValue(Notification.DEFAULT_SUBJECT_VALUES);
		}
		if (notifyInterval.getValue() == null || notifyInterval.getValue().trim().isEmpty()) {
			notifyInterval.setValue(Notification.DEFAULT_INTERVAL);
		}
	}

	/**
	 * Set default values if notification values are null or empty
	 * @param destinationEntity Destination to update
	 */
	public void updateDefaultValuesNotification(DestinationEntity destinationEntity) {
		if (destinationEntity.getNotifyObjectErrorPrefix() == null
				|| destinationEntity.getNotifyObjectErrorPrefix().trim().isEmpty()) {
			destinationEntity.setNotifyObjectErrorPrefix(Notification.DEFAULT_SUBJECT_ERROR_PREFIX);
		}
		if (destinationEntity.getNotifyObjectRejectionPrefix() == null
				|| destinationEntity.getNotifyObjectRejectionPrefix().trim().isEmpty()) {
			destinationEntity.setNotifyObjectRejectionPrefix(Notification.DEFAULT_SUBJECT_REJECTION_PREFIX);
		}
		if (destinationEntity.getNotifyObjectPattern() == null
				|| destinationEntity.getNotifyObjectPattern().trim().isEmpty()) {
			destinationEntity.setNotifyObjectPattern(Notification.DEFAULT_SUBJECT_PATTERN);
		}
		if (destinationEntity.getNotifyObjectValues() == null
				|| destinationEntity.getNotifyObjectValues().trim().isEmpty()) {
			destinationEntity.setNotifyObjectValues(Notification.DEFAULT_SUBJECT_VALUES);
		}
		if (destinationEntity.getNotifyInterval() == null || destinationEntity.getNotifyInterval() == 0) {
			destinationEntity.setNotifyInterval(Integer.parseInt(Notification.DEFAULT_INTERVAL));
		}
	}

	/**
	 * Build components used in Notification component
	 */
	private void buildComponents() {
		buildNotificationInputsDiv();
		buildNotificationObjectsDiv();
		buildActivateNotification();
		buildNotify();
		buildNotifyObjectErrorPrefix();
		buildNotifyObjectRejectionPrefix();
		buildNotifyObjectPattern();
		buildNotifyObjectValues();
		buildNotifyInterval();
	}

	/**
	 * Notify interval
	 */
	private void buildNotifyInterval() {
		notifyInterval = new TextField(String.format("通知：间隔（默认：%s）", Notification.DEFAULT_INTERVAL));
		notifyInterval.setWidth("32%");
		notifyInterval.addThemeVariants(TextFieldVariant.ALIGN_RIGHT);
		UIS.setTooltip(notifyInterval, String.format(
				"发送通知的间隔秒数（归档文件夹中无新图像到达时）。默认值：%s",
				Notification.DEFAULT_INTERVAL));
	}

	/**
	 * Notify Object Values
	 */
	private void buildNotifyObjectValues() {
		notifyObjectValues = new TextField(
				String.format("通知：主题值（默认：%s）", Notification.DEFAULT_SUBJECT_VALUES));
		notifyObjectValues.setWidth("32%");
		UIS.setTooltip(notifyObjectValues, String.format(
				"注入模式 [PatientID StudyDescription StudyDate StudyInstanceUID] 的值。默认值：%s",
				Notification.DEFAULT_SUBJECT_VALUES));
	}

	/**
	 * Notify Object Pattern
	 */
	private void buildNotifyObjectPattern() {
		notifyObjectPattern = new TextField(
				String.format("通知：主题模式（默认：%s）", Notification.DEFAULT_SUBJECT_PATTERN));
		notifyObjectPattern.setWidth("32%");
		UIS.setTooltip(notifyObjectPattern, String.format(
				"邮件主题的模式，参见 https://dzone.com/articles/java-string-format-examples。默认值：%s",
				Notification.DEFAULT_SUBJECT_PATTERN));
	}

	/**
	 * Notify Object Error Prefix
	 */
	private void buildNotifyObjectErrorPrefix() {
		notifyObjectErrorPrefix = new TextField(
				String.format("通知：错误主题前缀（默认：%s）", Notification.DEFAULT_SUBJECT_ERROR_PREFIX));
		notifyObjectErrorPrefix.setWidth("49%");
		UIS.setTooltip(notifyObjectErrorPrefix,
				String.format("邮件主题在包含问题时的前缀。默认值：%s",
						Notification.DEFAULT_SUBJECT_ERROR_PREFIX));
	}

	/**
	 * Notify Object Rejection Prefix
	 */
	private void buildNotifyObjectRejectionPrefix() {
		notifyObjectRejectionPrefix = new TextField(String.format("通知：拒收主题前缀（默认：%s）",
				Notification.DEFAULT_SUBJECT_REJECTION_PREFIX));
		notifyObjectRejectionPrefix.setWidth("49%");
		UIS.setTooltip(notifyObjectRejectionPrefix,
				String.format("拒收时邮件主题的前缀。默认值：%s",
						Notification.DEFAULT_SUBJECT_REJECTION_PREFIX));
	}

	/**
	 * Notify
	 */
	private void buildNotify() {
		notify = new TextField("通知：邮箱列表");
		notify.setWidth("100%");
		notify.getStyle().set("padding-top", "0");
		notify.getStyle().set("padding", "0");
	}

	/**
	 * Activate Notification
	 */
	private void buildActivateNotification() {
		activateNotification = new Checkbox("启用通知");
		// By default deactivate
		activateNotification.setValue(false);
	}

	/**
	 * Notification Inputs Div
	 */
	private void buildNotificationInputsDiv() {
		notificationInputsDiv = new Div();
		// By default hide
		notificationInputsDiv.setVisible(false);
		notificationInputsDiv.setWidthFull();
	}

	/**
	 * Notification Inputs Div
	 */
	private void buildNotificationObjectsDiv() {
		notificationObjectsDiv = new Div();
		// By default hide
		notificationObjectsDiv.setVisible(false);
		notificationObjectsDiv.setWidthFull();
	}

	/**
	 * Init binder for the component
	 * @param binder Binder
	 */
	public void init(Binder<DestinationEntity> binder) {

		// Activate notification
		binder.forField(getActivateNotification())
			.bind(DestinationEntity::isActivateNotification, DestinationEntity::setActivateNotification);

		// List of emails
		binder.forField(getNotify()).withValidator((s, valueContext) -> {
			if (StringUtils.isBlank(s) && getActivateNotification().getValue()) {
				return ValidationResult.error("至少需要一个邮箱地址");
			}
			return ValidationResult.ok();
		}).bind(DestinationEntity::getNotify, DestinationEntity::setNotify);

		// Interval
		binder.forField(getNotifyInterval()) //
			.withNullRepresentation("") //
			.withConverter(new HStringToIntegerConverter()) //
			.bind(DestinationEntity::getNotifyInterval, DestinationEntity::setNotifyInterval);

		// Error Prefix
		binder.forField(getNotifyObjectErrorPrefix())
			.bind(DestinationEntity::getNotifyObjectErrorPrefix, DestinationEntity::setNotifyObjectErrorPrefix);

		// Error Prefix
		binder.forField(getNotifyObjectRejectionPrefix())
			.bind(DestinationEntity::getNotifyObjectRejectionPrefix, DestinationEntity::setNotifyObjectRejectionPrefix);

		// Subject Pattern
		binder.forField(getNotifyObjectPattern())
			.bind(DestinationEntity::getNotifyObjectPattern, DestinationEntity::setNotifyObjectPattern);

		// Subject Values
		binder.forField(getNotifyObjectValues())
			.bind(DestinationEntity::getNotifyObjectValues, DestinationEntity::setNotifyObjectValues);
	}

}
