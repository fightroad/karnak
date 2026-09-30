/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.forwardnode.edit.source.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import org.apache.commons.lang3.StringUtils;
import org.karnak.backend.data.entity.DicomSourceNodeEntity;
import org.karnak.frontend.forwardnode.edit.component.ButtonSaveDeleteCancel;
import org.karnak.frontend.util.UIS;
import org.weasis.core.util.annotations.Generated;

@Generated()
public class FormSourceNode extends VerticalLayout {

	private final Binder<DicomSourceNodeEntity> binder;

	private final TextField aeTitle;

	private final TextField description;

	private final TextField hostname;

	private final Checkbox checkHostname;

	public FormSourceNode(Binder<DicomSourceNodeEntity> binder, ButtonSaveDeleteCancel buttonSaveDeleteCancel) {
		setSizeFull();
		this.binder = binder;
		aeTitle = new TextField("AE Title");
		description = new TextField("描述");
		hostname = new TextField("主机名");
		checkHostname = new Checkbox("检查主机名");

		setElements();
		setBinder();

		add(UIS.setWidthFull(new HorizontalLayout(aeTitle, description)),
				UIS.setWidthFull(new HorizontalLayout(hostname)), checkHostname,
				UIS.setWidthFull(buttonSaveDeleteCancel));
	}

	private void setElements() {
		aeTitle.setWidth("30%");
		description.setWidth("70%");
		hostname.setWidth("70%");
		UIS.setTooltip(checkHostname,
				"勾选后，在 DICOM 关联期间检查主机名；若不匹配则中止连接");
	}

	private void setBinder() {
		binder.forField(aeTitle)
			.withValidator(StringUtils::isNotBlank, "AE Title 为必填项")
			.bind(DicomSourceNodeEntity::getAeTitle, DicomSourceNodeEntity::setAeTitle);
		binder.forField(description).bind(DicomSourceNodeEntity::getDescription, DicomSourceNodeEntity::setDescription);

		binder.forField(hostname).bind(DicomSourceNodeEntity::getHostname, DicomSourceNodeEntity::setHostname);

		binder.forField(checkHostname)
			.bind(DicomSourceNodeEntity::getCheckHostname, DicomSourceNodeEntity::setCheckHostname);
		binder.bindInstanceFields(this);
	}

}
