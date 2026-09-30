/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.profile;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import jakarta.annotation.security.RolesAllowed;
import java.io.InputStream;
import lombok.Getter;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.ProfileEntity;
import org.karnak.frontend.MainLayout;
import org.karnak.frontend.component.ButtonFactory;
import org.karnak.frontend.component.NewItemDialog;
import org.karnak.frontend.profile.component.ProfileGrid;
import org.karnak.frontend.profile.component.editprofile.ProfileEditorPanel;
import org.karnak.frontend.profile.component.errorprofile.ProfileErrorView;
import org.springframework.beans.factory.annotation.Autowired;
import org.weasis.core.util.annotations.Generated;

@Route(value = ProfileView.ROUTE, layout = MainLayout.class)
@PageTitle("Karnak - 配置文件")
@RolesAllowed("admin")
@Generated()
@NullUnmarked
public class ProfileView extends HorizontalLayout implements HasUrlParameter<String> {

	public static final String VIEW_NAME = "配置文件";

	public static final String ROUTE = "profile";

	private final ProfileLogic profileLogic;

	@Getter
	private final ProfileEditorPanel profileEditorPanel;

	@Getter
	private final ProfileGrid profileGrid;

	@Getter
	private final ProfileErrorView profileErrorView;

	private VerticalLayout barAndGridLayout;

	private VerticalLayout rightPanel;

	private Upload uploadProfile;

	private UI ui;

	@Autowired
	public ProfileView(final ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
		this.profileLogic.setProfileView(this);

		profileGrid = new ProfileGrid();
		profileEditorPanel = new ProfileEditorPanel(profileLogic);
		profileErrorView = new ProfileErrorView();

		initComponents();
		buildLayout();

		addAttachListener(event -> this.ui = event.getUI());
	}

	@Override
	public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
		ProfileEntity currentProfileEntity = null;
		profileLogic.refreshAll();
		if (parameter != null) {
			final Long idProfilePipe = profileLogic.enter(parameter);
			if (idProfilePipe != null) {
				currentProfileEntity = profileLogic.retrieveProfile(idProfilePipe);
			}
			showEditorPanel();
		}
		profileGrid.selectRow(currentProfileEntity);
		profileEditorPanel.setProfile(currentProfileEntity);
	}

	private void buildLayout() {
		setSizeFull();
		setPadding(false);
		setSpacing(false);

		Button newProfileButton = ButtonFactory.createAddButton("新建配置文件");
		newProfileButton.addClickListener(event -> openNewProfileDialog());
		Button addGroupButton = profileGrid.createAddGroupButton();
		barAndGridLayout = new VerticalLayout();
		barAndGridLayout.add(newProfileButton);
		barAndGridLayout.add(uploadProfile);
		barAndGridLayout.add(addGroupButton);
		barAndGridLayout.add(profileGrid);
		barAndGridLayout.setFlexGrow(0, newProfileButton);
		barAndGridLayout.setFlexGrow(0, uploadProfile);
		barAndGridLayout.setFlexGrow(0, addGroupButton);
		barAndGridLayout.setFlexGrow(1, profileGrid);
		barAndGridLayout.setSizeFull();

		profileEditorPanel.setSizeFull();
		profileErrorView.setSizeFull();

		rightPanel = new VerticalLayout();
		rightPanel.setPadding(false);
		rightPanel.setSpacing(false);
		rightPanel.setSizeFull();
		rightPanel.add(profileEditorPanel);

		// Draggable splitter between the profile list (left) and the edit panel (right).
		SplitLayout splitLayout = new SplitLayout(barAndGridLayout, rightPanel);
		splitLayout.setSizeFull();
		splitLayout.setSplitterPosition(25);
		add(splitLayout);
	}

	/** Show the profile editor panel on the right side of the split. */
	public void showEditorPanel() {
		rightPanel.removeAll();
		rightPanel.add(profileEditorPanel);
	}

	/** Show the profile error view on the right side of the split. */
	public void showErrorView() {
		rightPanel.removeAll();
		rightPanel.add(profileErrorView);
	}

	/** Clear the right side of the split (e.g. after a profile is deleted). */
	public void clearRightPanel() {
		rightPanel.removeAll();
	}

	private void initComponents() {
		initUploadProfile();
		profileGrid.init(profileLogic, this::navigateProfile);
	}

	private void initUploadProfile() {
		uploadProfile = new Upload((UploadHandler) upload -> {
			InputStream inputStream = upload.getInputStream();
			if (ui != null) {
				ui.access(() -> profileLogic.setProfileComponent(inputStream));
			}
		});
		uploadProfile.setDropLabel(new Span("将配置文件拖放到此处"));
		uploadProfile.setUploadButton(new Button("上传文件..."));
	}

	private void openNewProfileDialog() {
		TextField name = new TextField("名称");
		TextField version = new TextField("版本");
		TextField minVersion = new TextField("最低 Karnak 版本（可选）");
		name.setWidthFull();
		version.setWidthFull();
		minVersion.setWidthFull();
		NewItemDialog dialog = new NewItemDialog("新建配置文件", "创建", name, version, minVersion).formSized();
		dialog.setOnConfirm(() -> {
			if (name.getValue() == null || name.getValue().isBlank()) {
				name.setInvalid(true);
				name.setErrorMessage("名称为必填项");
				return false;
			}
			profileLogic.createProfile(name.getValue().trim(), version.getValue(), minVersion.getValue());
			return true;
		});
		dialog.open();
	}

	/**
	 * Navigation to the profile in parameter
	 * @param profileEntity Profile to navigate to
	 */
	public void navigateProfile(ProfileEntity profileEntity) {
		if (ui != null) {
			ui.access(() -> {
				if (profileEntity == null) {
					ui.navigate(ProfileView.class, "");
				}
				else {
					String profileID = String.valueOf(profileEntity.getId());
					ui.navigate(ProfileView.class, profileID);
				}
			});
		}
	}

}
