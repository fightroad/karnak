/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.authconfig.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import org.karnak.frontend.component.ButtonFactory;
import org.karnak.frontend.component.NewItemDialog;
import org.weasis.core.util.annotations.Generated;

/**
 * Toolbar entry that creates a new authentication config: a "New authentication config"
 * button which opens a popup ({@link NewItemDialog}) where the identifier is entered.
 * Mirrors the new-project / new-profile pattern.
 */
@Generated()
public class NewAuthConfigComponent extends HorizontalLayout {

	@Getter
	private final Button buttonNewAuthConfig;

	@Getter
	private final NewItemDialog dialog;

	@Getter
	private final TextField newNameField;

	public NewAuthConfigComponent() {
		newNameField = new TextField();
		newNameField.setLabel("标识符");
		newNameField.setPlaceholder("请输入标识符");
		newNameField.setWidthFull();

		dialog = new NewItemDialog("新建认证配置", "添加", newNameField).compact();

		buttonNewAuthConfig = ButtonFactory.createAddButton("新建认证配置");
		buttonNewAuthConfig.addClickListener(click -> openDialog());

		setPadding(false);
		add(buttonNewAuthConfig);
	}

	private void openDialog() {
		newNameField.clear();
		newNameField.setInvalid(false);
		dialog.open();
		newNameField.focus();
	}

}
