/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.authconfig;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.karnak.backend.data.entity.AuthConfigEntity;
import org.karnak.backend.enums.AuthConfigType;
import org.karnak.frontend.MainLayout;
import org.karnak.frontend.authconfig.component.AuthConfigComponent;
import org.karnak.frontend.authconfig.component.NewAuthConfigComponent;
import org.karnak.frontend.component.WarningConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.weasis.core.util.annotations.Generated;

@Route(value = AuthConfigView.ROUTE, layout = MainLayout.class)
@PageTitle("Karnak - 认证配置")
@Tag("auth-config-view")
@RolesAllowed("admin")
@Generated()
public class AuthConfigView extends HorizontalLayout {

	public static final String VIEW_NAME = "认证配置";

	public static final String ROUTE = "auth-config";

	private final AuthConfigLogic authConfigLogic;

	private final AuthConfigComponent authConfigComponent;

	private final Grid<AuthConfigEntity> authConfigGrid;

	private VerticalLayout barAndGridLayout;

	private NewAuthConfigComponent newAuthConfigComponent;

	@Autowired
	public AuthConfigView(AuthConfigLogic authConfigLogic) {

		this.authConfigLogic = authConfigLogic;

		authConfigGrid = new Grid<>();
		authConfigComponent = new AuthConfigComponent();
		authConfigComponent.setWidth("100%");

		initComponents();
		buildLayout();

		addEventGridSelection();
		addEventDeleteAuthConfig();
		addEventSaveAuthConfig();
		addEventCreateAuthConfig();
		addEventCancelAuthConfig();

		setAlignItems(Alignment.STRETCH);
		this.getStyle().setDisplay(Style.Display.FLEX);

		SplitLayout splitLayout = new SplitLayout(barAndGridLayout, authConfigComponent);
		splitLayout.setSizeFull();
		splitLayout.setSplitterPosition(25);
		add(splitLayout);
	}

	private void initComponents() {
		authConfigGrid.setItems(authConfigLogic.getItems());
		authConfigGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		authConfigGrid.addColumn(a -> a.getAuthConfigType().getCode()).setHeader("类型");
		authConfigGrid.addColumn(AuthConfigEntity::getCode).setHeader("标识符");

		newAuthConfigComponent = new NewAuthConfigComponent();
	}

	private void buildLayout() {
		setSizeFull();

		barAndGridLayout = new VerticalLayout();
		barAndGridLayout.add(newAuthConfigComponent);
		barAndGridLayout.add(authConfigGrid);
		barAndGridLayout.setWidth("100%");

	}

	private void addEventGridSelection() {
		authConfigGrid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				authConfigComponent.displayData(authConfigLogic.retrieveAuthConfig(event.getValue().getCode()));
			}
		});
	}

	private void addEventDeleteAuthConfig() {
		authConfigComponent.getDeleteBtn().addClickListener(buttonClickEvent -> {
			Div dialogContent = new Div();
			dialogContent
				.add(new Text("确定删除条目 " + authConfigComponent.getAuthConfigCode()
						+ " 吗？请确认它未被使用，否则可能导致错误。"));
			WarningConfirmDialog dialog = new WarningConfirmDialog(dialogContent);
			dialog.addConfirmationListener(componentEvent -> {
				authConfigLogic.deleteAuthConfig(authConfigComponent.getAuthConfigCode());
				authConfigLogic.refreshAll();
				authConfigGrid.setItems(authConfigLogic.getItems());
				authConfigComponent.cancel();
			});
			dialog.open();
		});
	}

	private void addEventCreateAuthConfig() {
		newAuthConfigComponent.getDialog().setOnConfirm(this::validateIdentifier);
	}

	/**
	 * Validate the identifier entered in the new-auth-config popup.
	 * @return {@code true} when the identifier is valid (the popup closes and the empty
	 * form is shown), {@code false} to keep the popup open with the error message
	 */
	private boolean validateIdentifier() {
		String name = newAuthConfigComponent.getNewNameField().getValue();
		if (name == null || name.isEmpty()) {
			newAuthConfigComponent.getNewNameField().setInvalid(true);
			newAuthConfigComponent.getNewNameField().setErrorMessage("标识符为必填项");
			return false;
		}
		else if (authConfigLogic.contains(name)) {
			newAuthConfigComponent.getNewNameField().setInvalid(true);
			newAuthConfigComponent.getNewNameField().setErrorMessage("该标识符已存在");
			return false;
		}
		newAuthConfigComponent.getNewNameField().setInvalid(false);
		authConfigComponent.displayEmptyForm(name);
		return true;
	}

	private void addEventSaveAuthConfig() {
		authConfigComponent.getSaveBtn().addClickListener(buttonClickEvent -> {
			if (authConfigComponent.isValid()) {
				AuthConfigEntity newEntity = authConfigComponent.getData();
				authConfigLogic.createAuthConfig(authConfigComponent.getAuthConfigCode(), AuthConfigType.OAUTH2,
						newEntity);
				authConfigLogic.refreshAll();
				authConfigGrid.setItems(authConfigLogic.getItems());
				authConfigGrid.select(newEntity);
			}
		});
	}

	private void addEventCancelAuthConfig() {
		authConfigComponent.getCancelBtn().addClickListener(buttonClickEvent -> {
			authConfigComponent.cancel();
			authConfigGrid.deselectAll();
		});
	}

}
