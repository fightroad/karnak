/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.authentication;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import org.karnak.frontend.image.LogoKarnak;
import org.weasis.core.util.annotations.Generated;

/**
 * UI content when the user is not logged in yet.
 */
@Slf4j
@Route(LoginScreen.ROUTE)
@PageTitle("Karnak - 登录")
@AnonymousAllowed
@Generated()
public class LoginScreen extends FlexLayout implements BeforeEnterObserver {

	public static final String ROUTE = "login";

	static final String KARNAK_TITLE = "KARNAK";
	static final String LOGO_SIZE = "225px";

	private static final String ERROR_QUERY_PARAM = "error";

	private final LoginForm loginForm;

	public LoginScreen() {
		this.loginForm = new LoginForm();
		buildUI();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey(ERROR_QUERY_PARAM)) {
			loginForm.setError(true);
		}
	}

	private void buildUI() {
		setSizeFull();
		setClassName("login-screen");

		ThemeUtil.initializeTheme();
		add(buildLoginMainComponent());
		focusUsernameField();
	}

	private Component buildLoginMainComponent() {
		loginForm.setAction(ROUTE);
		loginForm.setI18n(createChineseLoginI18n());

		loginForm.setForgotPasswordButtonVisible(false);

		VerticalLayout loginInformation = new VerticalLayout();
		loginInformation.setJustifyContentMode(JustifyContentMode.CENTER);
		loginInformation.setAlignItems(Alignment.CENTER);
		LogoKarnak logoKarnak = new LogoKarnak(KARNAK_TITLE, LOGO_SIZE);
		loginInformation.add(logoKarnak, new H1(KARNAK_TITLE), loginForm);

		return loginInformation;
	}

	private static LoginI18n createChineseLoginI18n() {
		LoginI18n i18n = LoginI18n.createDefault();
		LoginI18n.Form form = i18n.getForm();
		form.setTitle("登录");
		form.setUsername("用户名");
		form.setPassword("密码");
		form.setSubmit("登录");
		form.setForgotPassword("忘记密码");
		i18n.setForm(form);
		LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
		errorMessage.setTitle("登录失败");
		errorMessage.setMessage("用户名或密码错误，请重试。");
		errorMessage.setUsername("用户名不能为空");
		errorMessage.setPassword("密码不能为空");
		i18n.setErrorMessage(errorMessage);
		return i18n;
	}

	private void focusUsernameField() {
		UI.getCurrent().getPage().executeJs("document.getElementById('vaadinLoginUsername')?.focus();");
	}

}
