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

import static org.karnak.backend.enums.PseudonymType.CACHE_EXTID;
import static org.karnak.backend.enums.PseudonymType.EXTID_API;
import static org.karnak.backend.enums.PseudonymType.EXTID_IN_TAG;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.frontend.component.ProjectDropDown;
import org.weasis.core.util.annotations.Generated;

@Getter
@Generated()
@NullUnmarked
public class DeIdentificationComponent extends VerticalLayout {

	// Labels
	private static final String LABEL_CHECKBOX_DEIDENTIFICATION = "启用去标识";

	private static final String LABEL_DISCLAIMER_DEIDENTIFICATION = "为确保完全去标识，需对元数据和图像进行目视验证。";

	private static final String LABEL_DEFAULT_ISSUER = "若填写，将与 Patient ID 组合，以确保在不同医疗系统中患者标识的唯一性。";

	private static final String LABEL_CHECKBOX_SKIP_ISSUER = "忽略 Issuer of Patient ID";

	private static final String HELPER_SKIP_ISSUER = "勾选后，Issuer of Patient ID 不会用于构建从缓存检索伪名的键。仅适用于「伪名已存储在 KARNAK 中」。";

	// Components
	private Checkbox deIdentificationCheckbox;

	private Checkbox skipIssuerOfPatientIdCheckbox;

	private NativeLabel disclaimerLabel;

	private ProjectDropDown projectDropDown;

	private PseudonymInDicomTagComponent pseudonymInDicomTagComponent;

	private PseudonymFromApi pseudonymFromApiComponent;

	@Setter
	private Binder<DestinationEntity> destinationBinder;

	private Div pseudonymDicomTagDiv;

	private Div pseudonymApi;

	private Div deIdentificationDiv;

	private ProfileLabel profileLabel;

	private WarningNoProjectsDefined warningNoProjectsDefined;

	private Select<String> pseudonymTypeSelect;

	private TextField issuerOfPatientIDByDefault;

	private final DestinationComponentUtil destinationComponentUtil;

	/**
	 * Constructor
	 */
	public DeIdentificationComponent() {
		this.destinationComponentUtil = new DestinationComponentUtil();
	}

	/**
	 * Init deidentification component
	 * @param binder Binder for checks
	 */
	public void init(final Binder<DestinationEntity> binder) {
		// Init destination binder
		setDestinationBinder(binder);

		// Build deidentification components
		buildComponents();

		// Init destination binder
		initDestinationBinder();

		// Build Listeners
		buildListeners();

		// Add components
		addComponents();
	}

	/**
	 * Add components
	 */
	private void addComponents() {
		setPadding(false);
		getStyle().set("padding", "0.5rem 0.75rem");

		// Group the skip checkbox + issuer text field in a dedicated block, with enough
		// spacing that each control reads together with its own helper text. The
		// "Ignore Issuer of Patient ID" checkbox comes first because it governs the
		// issuer field below it (checking it disables and clears that field).
		VerticalLayout issuerLayout = new VerticalLayout();
		issuerLayout.setPadding(false);
		issuerLayout.setSpacing(false);
		issuerLayout.setWidthFull();
		issuerLayout.getStyle().set("gap", "1.25rem");
		issuerLayout.add(skipIssuerOfPatientIdCheckbox, issuerOfPatientIDByDefault);

		// Keep the resolved-profile label tight under the project field, so it reads as
		// that field's helper rather than a separate control
		VerticalLayout projectGroup = new VerticalLayout();
		projectGroup.setPadding(false);
		projectGroup.setSpacing(false);
		projectGroup.setWidthFull();
		projectGroup.getStyle().set("gap", "0.25rem");
		projectGroup.add(projectDropDown, profileLabel);

		// Stack the controls in two readable sections: which project/profile to apply,
		// then how the pseudonym is generated. A generous gap keeps each field visually
		// bound to its own helper text instead of the next component's label.
		VerticalLayout content = new VerticalLayout();
		content.setPadding(false);
		content.setSpacing(false);
		content.setWidthFull();
		content.getStyle().set("gap", "1.25rem");
		content.add(disclaimerLabel, sectionTitle("项目与配置文件"), projectGroup,
				sectionTitle("伪名生成"), pseudonymTypeSelect, pseudonymDicomTagDiv, pseudonymApi,
				issuerLayout);
		deIdentificationDiv.add(content);

		// If checkbox is checked set div visible, invisible otherwise
		deIdentificationDiv.setVisible(deIdentificationCheckbox.getValue());

		// Checkbox as a full-width header row, the form fields stacked below it
		add(deIdentificationCheckbox, deIdentificationDiv);
	}

	/**
	 * Build a small uppercase heading that groups related fields inside the card
	 * @param text Heading text
	 * @return Styled heading span
	 */
	private static Span sectionTitle(String text) {
		Span title = new Span(text);
		title.addClassName("karnak-section-title");
		return title;
	}

	/**
	 * Build listeners
	 */
	private void buildListeners() {
		buildPseudonymTypeListener();
		buildSkipIssuerListener();
		destinationComponentUtil.buildWarningNoProjectDefinedListener(warningNoProjectsDefined,
				deIdentificationCheckbox);
		destinationComponentUtil.buildProjectDropDownListener(projectDropDown, profileLabel);
	}

	/**
	 * Build deidentification components
	 */
	private void buildComponents() {
		buildIssuerOfPatientID();
		buildSkipIssuerOfPatientIdCheckbox();
		projectDropDown = destinationComponentUtil.buildProjectDropDown();
		profileLabel = new ProfileLabel();
		warningNoProjectsDefined = destinationComponentUtil.buildWarningNoProjectDefined();
		deIdentificationCheckbox = destinationComponentUtil.buildActivateCheckbox(LABEL_CHECKBOX_DEIDENTIFICATION);
		buildDisclaimerLabel();
		buildPseudonymTypeSelect();
		buildPseudonymInDicomTagComponent();
		buildPseudonymFromApiComponent();
		deIdentificationDiv = destinationComponentUtil.buildActivateDiv();
		buildPseudonymDicomTagDiv();
		buildPseudonymApi();
	}

	/**
	 * Build Pseudonym In Dicom Tag Component
	 */
	private void buildPseudonymInDicomTagComponent() {
		pseudonymInDicomTagComponent = new PseudonymInDicomTagComponent(destinationBinder);
	}

	private void buildPseudonymFromApiComponent() {
		pseudonymFromApiComponent = new PseudonymFromApi(destinationBinder);
	}

	/**
	 * Build Pseudonym Dicom Tag Div which is visible if "Pseudonym is in a dicom tag" is
	 * selected
	 */
	private void buildPseudonymDicomTagDiv() {
		pseudonymDicomTagDiv = new Div();
		pseudonymDicomTagDiv.add(pseudonymInDicomTagComponent);
	}

	private void buildPseudonymApi() {
		pseudonymApi = new Div();
		pseudonymApi.add(pseudonymFromApiComponent);
	}

	/**
	 * Build pseudonym type
	 */
	private void buildPseudonymTypeSelect() {
		pseudonymTypeSelect = new Select<>();
		pseudonymTypeSelect.setLabel("伪名类型");
		// Keep the selector compact rather than spanning the whole panel; the option
		// labels never need the full destination-panel width.
		pseudonymTypeSelect.setWidth("350px");
		pseudonymTypeSelect.setHelperText(
				"Karnak 获取伪名的方式：从自有缓存、从图像中的 DICOM Tag，或从外部 API。");
		pseudonymTypeSelect.setItems(CACHE_EXTID.getValue(), EXTID_IN_TAG.getValue(), EXTID_API.getValue());
		pseudonymTypeSelect.setItemLabelGenerator(value -> switch (value) {
			case String v when v.equals(CACHE_EXTID.getValue()) -> "伪名已存储在 KARNAK 中";
			case String v when v.equals(EXTID_IN_TAG.getValue()) -> "伪名在 DICOM Tag 中";
			case String v when v.equals(EXTID_API.getValue()) -> "从外部 API 获取伪名";
			default -> value;
		});
	}

	/**
	 * Build disclaimer
	 */
	private void buildDisclaimerLabel() {
		disclaimerLabel = new NativeLabel(LABEL_DISCLAIMER_DEIDENTIFICATION);
		disclaimerLabel.addClassName("karnak-note-text");
		disclaimerLabel.setWidthFull();
	}

	/**
	 * Build issuer of patient ID
	 */
	private void buildIssuerOfPatientID() {
		issuerOfPatientIDByDefault = new TextField();
		issuerOfPatientIDByDefault.setLabel("默认 Issuer of Patient ID");
		// Half-width is plenty for an issuer identifier; no need to span the whole panel.
		issuerOfPatientIDByDefault.setWidth("50%");
		issuerOfPatientIDByDefault.setPlaceholder("例如：医院标识符");
		issuerOfPatientIDByDefault.setHelperText(LABEL_DEFAULT_ISSUER);
		// Only relevant to CACHE_EXTID ("Pseudonym is already stored in KARNAK"), where
		// it
		// is the fallback issuer used to build the cache key; hidden for the other types.
		issuerOfPatientIDByDefault.setVisible(false);
	}

	/**
	 * Build skip issuer of patient ID checkbox — hidden by default until CACHE_EXTID is
	 * selected
	 */
	private void buildSkipIssuerOfPatientIdCheckbox() {
		skipIssuerOfPatientIdCheckbox = new Checkbox(LABEL_CHECKBOX_SKIP_ISSUER);
		skipIssuerOfPatientIdCheckbox.setVisible(false);
		skipIssuerOfPatientIdCheckbox.setHelperText(HELPER_SKIP_ISSUER);
	}

	/**
	 * Listener on skip issuer checkbox: disables the issuer text field when checked
	 */
	private void buildSkipIssuerListener() {
		skipIssuerOfPatientIdCheckbox.addValueChangeListener(event -> {
			boolean skip = Boolean.TRUE.equals(event.getValue());
			issuerOfPatientIDByDefault.setEnabled(!skip);
			if (skip) {
				issuerOfPatientIDByDefault.clear();
			}
		});
	}

	/**
	 * Listener on pseudonym type
	 */
	private void buildPseudonymTypeListener() {
		pseudonymTypeSelect.addValueChangeListener(event -> {
			if (event.getValue() != null) {
				pseudonymDicomTagDiv.setVisible(Objects.equals(event.getValue(), EXTID_IN_TAG.getValue()));
				pseudonymApi.setVisible(Objects.equals(event.getValue(), EXTID_API.getValue()));
				updateSkipIssuerCheckboxState(event.getValue());
			}
		});
	}

	/**
	 * Show the skip issuer checkbox only for CACHE_EXTID, hide and reset it otherwise
	 * @param pseudonymTypeValue Currently selected pseudonym type value
	 */
	private void updateSkipIssuerCheckboxState(String pseudonymTypeValue) {
		boolean isCacheExtid = Objects.equals(pseudonymTypeValue, CACHE_EXTID.getValue());
		issuerOfPatientIDByDefault.setVisible(isCacheExtid);
		skipIssuerOfPatientIdCheckbox.setVisible(isCacheExtid);
		if (!isCacheExtid) {
			skipIssuerOfPatientIdCheckbox.setValue(false);
			issuerOfPatientIDByDefault.setEnabled(true);
		}
	}

	private void initDestinationBinder() {
		destinationBinder.forField(issuerOfPatientIDByDefault)
			.bind(DestinationEntity::getIssuerByDefault, (destinationEntity, s) -> {
				if (deIdentificationCheckbox.getValue()) {
					destinationEntity.setIssuerByDefault(s);
				}
				else {
					destinationEntity.setIssuerByDefault("");
				}
			});
		destinationBinder.forField(deIdentificationCheckbox)
			.bind(DestinationEntity::isDesidentification, DestinationEntity::setDesidentification);
		destinationBinder.forField(skipIssuerOfPatientIdCheckbox)
			.bind(DestinationEntity::isSkipIssuerOfPatientId, DestinationEntity::setSkipIssuerOfPatientId);
		destinationBinder.forField(projectDropDown)
			.withValidator(project -> project != null || !deIdentificationCheckbox.getValue(), "请选择项目")
			.bind(DestinationEntity::getDeIdentificationProjectEntity,
					DestinationEntity::setDeIdentificationProjectEntity);

		destinationBinder.forField(pseudonymTypeSelect)
			.withValidator(Objects::nonNull, "请选择伪名类型\n")
			.bind(destination -> destination.getPseudonymType().getValue(), (destination, s) -> {
				if (s.equals(EXTID_IN_TAG.getValue())) {
					destination.setPseudonymType(EXTID_IN_TAG);
				}
				else if (s.equals(EXTID_API.getValue())) {
					destination.setPseudonymType(EXTID_API);
				}
				else if (s.equals(CACHE_EXTID.getValue())) {
					destination.setPseudonymType(CACHE_EXTID);
				}
			});
	}

	/**
	 * Clean fields of destination which are not saved because not selected by user
	 * @param destinationEntity Destination to clean
	 */
	public void cleanUnSavedData(DestinationEntity destinationEntity) {
		// Reset the destination for the part tag is in dicom tag in case the pseudonym
		// type selected is not pseudonym in dicom tag or deidentification not active
		if (!destinationEntity.isDesidentification()
				|| !Objects.equals(destinationEntity.getPseudonymType(), EXTID_IN_TAG)) {
			destinationEntity.setTag(null);
			destinationEntity.setDelimiter(null);
			destinationEntity.setPosition(null);
			destinationEntity.setSavePseudonym(null);
		}

		if (!destinationEntity.isDesidentification()) {
			// Reset the destination for pseudonym type, project, issuer of patient id,
			// skip issuer
			destinationEntity.setDeIdentificationProjectEntity(null);
			destinationEntity.setPseudonymType(CACHE_EXTID);
			destinationEntity.setIssuerByDefault(null);
			destinationEntity.setSkipIssuerOfPatientId(false);
		}
	}

}
