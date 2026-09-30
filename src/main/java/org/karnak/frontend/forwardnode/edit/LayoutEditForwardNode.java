/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.forwardnode.edit;

import static org.karnak.backend.enums.PseudonymType.EXTID_API;
import static org.karnak.backend.enums.PseudonymType.EXTID_IN_TAG;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.dcm4che3.util.TagUtils;
import org.jspecify.annotations.NullUnmarked;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.backend.data.entity.DicomSourceNodeEntity;
import org.karnak.backend.data.entity.ForwardNodeEntity;
import org.karnak.backend.data.entity.SOPClassUIDEntity;
import org.karnak.backend.enums.DestinationType;
import org.karnak.backend.enums.NodeEventType;
import org.karnak.backend.model.event.NodeEvent;
import org.karnak.backend.service.AuthConfigService;
import org.karnak.backend.service.ProjectService;
import org.karnak.backend.service.SOPClassUIDService;
import org.karnak.backend.util.DoubleToIntegerConverter;
import org.karnak.backend.util.SystemPropertyUtil;
import org.karnak.frontend.component.ConfirmDialog;
import org.karnak.frontend.forwardnode.ForwardNodeLogic;
import org.karnak.frontend.forwardnode.edit.component.ButtonSaveDeleteCancel;
import org.karnak.frontend.forwardnode.edit.component.EditAETitleDescription;
import org.karnak.frontend.forwardnode.edit.component.TabSourcesDestination;
import org.karnak.frontend.forwardnode.edit.destination.DestinationView;
import org.karnak.frontend.forwardnode.edit.destination.component.DeIdentificationComponent;
import org.karnak.frontend.forwardnode.edit.destination.component.FilterBySOPClassesForm;
import org.karnak.frontend.forwardnode.edit.destination.component.NewUpdateDestination;
import org.karnak.frontend.forwardnode.edit.destination.component.NotificationComponent;
import org.karnak.frontend.forwardnode.edit.destination.component.TagMorphingComponent;
import org.karnak.frontend.forwardnode.edit.destination.component.TranscodeOnlyUncompressedComponent;
import org.karnak.frontend.forwardnode.edit.source.SourceView;
import org.karnak.frontend.forwardnode.edit.source.component.NewUpdateSourceNode;
import org.karnak.frontend.util.UIS;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.weasis.core.util.StringUtil;
import org.weasis.core.util.annotations.Generated;

/**
 * Layout of the edit forward node
 */
@Generated()
@NullUnmarked
public class LayoutEditForwardNode extends VerticalLayout {

	// Services
	private final transient ProjectService projectService;

	private final transient AuthConfigService authConfigService;

	private final SOPClassUIDService sopClassUIDService;

	// UI components
	@Getter
	private final Binder<ForwardNodeEntity> binderForwardNode;

	private final EditAETitleDescription editAETitleDescription;

	private final TabSourcesDestination tabSourcesDestination;

	private final VerticalLayout layoutDestinationsSources;

	private final DestinationView destinationView;

	private final SourceView sourceView;

	private final NewUpdateDestination newUpdateDestination;

	@Getter
	private final ButtonSaveDeleteCancel buttonForwardNodeSaveDeleteCancel;

	private final NewUpdateSourceNode newUpdateSourceNode;

	@Setter
	@Getter
	private ForwardNodeEntity currentForwardNodeEntity;

	public LayoutEditForwardNode(ForwardNodeLogic forwardNodeLogic, Environment environment) {
		this.projectService = forwardNodeLogic.getProjectService();
		this.authConfigService = forwardNodeLogic.getAuthConfigService();
		this.sopClassUIDService = forwardNodeLogic.getSopClassUIDService();
		this.currentForwardNodeEntity = null;
		this.binderForwardNode = new BeanValidationBinder<>(ForwardNodeEntity.class);
		this.tabSourcesDestination = new TabSourcesDestination();
		this.layoutDestinationsSources = new VerticalLayout();
		this.buttonForwardNodeSaveDeleteCancel = new ButtonSaveDeleteCancel();
		this.newUpdateDestination = new NewUpdateDestination();
		this.newUpdateSourceNode = new NewUpdateSourceNode();
		this.sourceView = new SourceView(forwardNodeLogic);
		this.destinationView = new DestinationView(forwardNodeLogic, environment);
		this.editAETitleDescription = new EditAETitleDescription(binderForwardNode, forwardNodeLogic, environment);

		// Init components
		initComponents();

		// Build layout
		buildLayout();

		// Events
		addEvents(environment);

		// Binder
		addBinders();
	}

	/**
	 * Build layout
	 */
	private void buildLayout() {
		// Align content with the tab bar (no nested padding/full-height stretch)
		layoutDestinationsSources.setPadding(false);
		layoutDestinationsSources.setSpacing(false);
		layoutDestinationsSources.setWidthFull();
		// More breathing room above the tabs, and tighten the gap below them so the
		// sources/destinations content sits closer to the tab bar.
		tabSourcesDestination.getStyle().set("margin-top", "1rem").set("margin-bottom", "0");
		layoutDestinationsSources.getStyle().set("padding-top", "0.5rem");
		getStyle().set("overflow-y", "auto");
		setSizeFull();
		setEditView();
		setLayoutDestinationsSources(tabSourcesDestination.getSelectedTab().getLabel());
	}

	/**
	 * Add binders on components
	 */
	private void addBinders() {
		addBindersFilterBySOPClassesForm(newUpdateDestination.getFormDICOM().getFilterBySOPClassesForm());
		addBindersFilterBySOPClassesForm(newUpdateDestination.getFormSTOW().getFilterBySOPClassesForm());
		addBinderExtidInDicomTag(newUpdateDestination.getFormSTOW().getDeIdentificationComponent());
		addBinderExtidInDicomTag(newUpdateDestination.getFormDICOM().getDeIdentificationComponent());
		addBinderExtidFromApi(newUpdateDestination.getFormSTOW().getDeIdentificationComponent());
		addBinderExtidFromApi(newUpdateDestination.getFormDICOM().getDeIdentificationComponent());
	}

	/**
	 * Add events on components
	 */
	private void addEvents(Environment environment) {
		addEventButtonSaveNewUpdateSourceNode();
		addEventButtonDeleteNewUpdateSourceNode();
		addEventButtonSaveNewUpdateDestination();
		addEventButtonDeleteNewUpdateDestination();
		addEventCheckboxLayoutTagMorphing(newUpdateDestination.getFormDICOM().getTagMorphingComponent(),
				newUpdateDestination.getFormDICOM().getDeIdentificationComponent());
		addEventCheckboxLayoutTagMorphing(newUpdateDestination.getFormSTOW().getTagMorphingComponent(),
				newUpdateDestination.getFormSTOW().getDeIdentificationComponent());
		addEventCheckboxLayoutDesidentification(newUpdateDestination.getFormDICOM().getDeIdentificationComponent(),
				newUpdateDestination.getFormDICOM().getTagMorphingComponent());
		addEventCheckboxLayoutDesidentification(newUpdateDestination.getFormSTOW().getDeIdentificationComponent(),
				newUpdateDestination.getFormSTOW().getTagMorphingComponent());
		addEventTranscodeOnlyUncompressedWhenSomeTransferSyntax(
				newUpdateDestination.getFormSTOW().getTransferSyntaxComponent().getTransferSyntaxSelect(),
				newUpdateDestination.getFormSTOW().getTranscodeOnlyUncompressedComponent());
		addEventTranscodeOnlyUncompressedWhenSomeTransferSyntax(
				newUpdateDestination.getFormDICOM().getTransferSyntaxComponent().getTransferSyntaxSelect(),
				newUpdateDestination.getFormDICOM().getTranscodeOnlyUncompressedComponent());
		setEventChangeTabValue();
		setEventBinderForwardNode();
		setEventDestination();
		if (environment.acceptsProfiles(Profiles.of("portable"))) {
			setEventDestinationsViewLocalDICOM();
		}
		setEventDestinationsViewDICOM();
		setEventDestinationsViewSTOW();
		setEventDestinationCancelButton();
		setEventNewSourceNode();
		setEventGridSourceNode();
		setEventSourceNodeCancelButton();
	}

	private void initComponents() {
		// FormDicom
		newUpdateDestination.getFormDICOM()
			.getDeIdentificationComponent()
			.getProjectDropDown()
			.setItems(projectService.retrieveAllProjects());
		newUpdateDestination.getFormDICOM()
			.getTagMorphingComponent()
			.getProjectDropDown()
			.setItems(projectService.retrieveAllProjects());
		newUpdateDestination.getFormDICOM()
			.getFilterBySOPClassesForm()
			.getSopFilter()
			.setItems(sopClassUIDService.getAllSOPClassUIDsName());
		newUpdateDestination.getFormDICOM()
			.getDeIdentificationComponent()
			.getPseudonymFromApiComponent()
			.getAuthConfig()
			.setItems(authConfigService.getAllAuthConfigCodes());

		// FormStow
		newUpdateDestination.getFormSTOW()
			.getDeIdentificationComponent()
			.getProjectDropDown()
			.setItems(projectService.retrieveAllProjects());
		newUpdateDestination.getFormSTOW()
			.getTagMorphingComponent()
			.getProjectDropDown()
			.setItems(projectService.retrieveAllProjects());
		newUpdateDestination.getFormSTOW()
			.getFilterBySOPClassesForm()
			.getSopFilter()
			.setItems(sopClassUIDService.getAllSOPClassUIDsName());
		newUpdateDestination.getFormSTOW()
			.getDeIdentificationComponent()
			.getPseudonymFromApiComponent()
			.getAuthConfig()
			.setItems(authConfigService.getAllAuthConfigCodes());

		// Set button in view in order to be able to enable/disable it
		destinationView.setButtonForwardNodeSaveDeleteCancel(buttonForwardNodeSaveDeleteCancel);

		// Set editable form in order to retrieve save button to enable/disable it
		destinationView.setNewUpdateDestination(newUpdateDestination);

		// Place the Save/Delete/Cancel buttons on the AE title / description row so they
		// align with the two text fields.
		editAETitleDescription.setActionButtons(buttonForwardNodeSaveDeleteCancel);
	}

	public void setEditView() {
		removeAll();
		// Save/Delete/Cancel act on the forward node (AE title / description) and sit on
		// the
		// same row as those fields (see initComponents). Sources and destinations have
		// their
		// own save/delete in their forms below.
		add(UIS.setWidthFull(this.editAETitleDescription), UIS.setWidthFull(this.tabSourcesDestination),
				UIS.setWidthFull(this.layoutDestinationsSources));
	}

	public void load(ForwardNodeEntity forwardNodeEntity) {
		this.currentForwardNodeEntity = forwardNodeEntity;
		this.editAETitleDescription.setForwardNode(forwardNodeEntity);

		destinationView.loadForwardNode(forwardNodeEntity);
		sourceView.loadForwardNode(forwardNodeEntity);

		setEditView();
		if (forwardNodeEntity == null) {
			this.tabSourcesDestination.setEnabled(false);
			this.buttonForwardNodeSaveDeleteCancel.setEnabled(false);
		}
		else {
			this.tabSourcesDestination.setEnabled(true);
			this.buttonForwardNodeSaveDeleteCancel.setEnabled(true);
		}
	}

	private void setEventChangeTabValue() {
		this.tabSourcesDestination.addSelectedChangeListener(event -> {
			Tab selectedTab = event.getSource().getSelectedTab();
			setLayoutDestinationsSources(selectedTab.getLabel());
		});
	}

	private void setLayoutDestinationsSources(String currentTab) {
		this.layoutDestinationsSources.removeAll();
		if (currentTab.equals(this.tabSourcesDestination.LABEL_SOURCES)) {
			this.layoutDestinationsSources.add(this.sourceView);
		}
		else if (currentTab.equals(this.tabSourcesDestination.LABEL_DESTINATIONS)) {
			this.layoutDestinationsSources.add(this.destinationView);
		}
	}

	private void setEventDestinationsViewLocalDICOM() {
		this.destinationView.getNewLocalDICOM().addClickListener(event -> {
			DestinationEntity dicomEntity = new DestinationEntity();
			dicomEntity.setDestinationType(DestinationType.dicom);
			String localAET = SystemPropertyUtil.retrieveSystemProperty("LOCAL_NODE_AE_TITLE", "KARNAK-LOCAL");
			Integer localPort = SystemPropertyUtil.retrieveIntegerSystemProperty("LOCAL_NODE_PORT", null);
			String folder = SystemPropertyUtil.retrieveSystemProperty("LOCAL_NODE_STORAGE_PATH", null);
			if (localPort != null && StringUtil.hasText(folder)) {
				dicomEntity.setDescription("本地 DICOM 文件夹：" + folder);
				dicomEntity.setAeTitle(localAET);
				dicomEntity.setHostname("localhost");
				dicomEntity.setPort(localPort);
				dicomEntity.setActivate(true);
			}
			else {
				dicomEntity = null;
			}

			this.newUpdateDestination.load(dicomEntity, DestinationType.dicom);
			addFormView(this.newUpdateDestination);
		});
	}

	private void setEventDestinationsViewDICOM() {
		this.destinationView.getNewDestinationDICOM().addClickListener(event -> {
			this.newUpdateDestination.load(null, DestinationType.dicom);
			addFormView(this.newUpdateDestination);
		});
	}

	private void setEventDestinationsViewSTOW() {
		this.destinationView.getNewDestinationSTOW().addClickListener(event -> {
			this.newUpdateDestination.load(null, DestinationType.stow);
			addFormView(this.newUpdateDestination);
		});
	}

	private void setEventNewSourceNode() {
		this.sourceView.getNewSourceNode().addClickListener(event -> {
			this.newUpdateSourceNode.load(null);
			addFormView(this.newUpdateSourceNode);
		});
	}

	private void addFormView(Component form) {
		removeAll();
		add(form);
	}

	private void setEventBinderForwardNode() {
		this.binderForwardNode.addStatusChangeListener(event -> {
			boolean isValid = !event.hasValidationErrors();
			// The forward-node binder uses setBean (write-through): a valid edit is
			// written
			// to the bean immediately, so hasChanges() turns false and would wrongly
			// disable
			// Save right after typing. Keep Save enabled whenever a node is loaded and
			// valid.
			boolean nodeLoaded = this.currentForwardNodeEntity != null;
			this.buttonForwardNodeSaveDeleteCancel.getSave().setEnabled(nodeLoaded && isValid);
		});
	}

	private void setEventDestination() {
		this.destinationView.getGridDestination().addItemClickListener(event -> {
			DestinationEntity destinationEntity = event.getItem();
			// refresh destination entity in case changes occurs
			destinationEntity = destinationView.getDestinationLogic()
				.retrieveDestinationEntity(destinationEntity.getId());
			this.newUpdateDestination.load(destinationEntity, destinationEntity.getDestinationType());
			addFormView(this.newUpdateDestination);
		});
	}

	private void setEventDestinationCancelButton() {
		this.newUpdateDestination.getButtonDICOMCancel().addClickListener(event -> setEditView());
		this.newUpdateDestination.getButtonSTOWCancel().addClickListener(event -> setEditView());
	}

	private void setEventGridSourceNode() {
		this.sourceView.getGridSourceNode().addItemClickListener(event -> {
			DicomSourceNodeEntity dicomSourceNodeEntity = event.getItem();
			this.newUpdateSourceNode.load(dicomSourceNodeEntity);
			addFormView(this.newUpdateSourceNode);
		});
	}

	private void setEventSourceNodeCancelButton() {
		this.newUpdateSourceNode.getButtonCancel().addClickListener(event -> setEditView());
	}

	public void updateForwardNodeInEditView() {
		this.load(currentForwardNodeEntity);
	}

	private void addEventButtonSaveNewUpdateSourceNode() {
		newUpdateSourceNode.getButtonSaveDeleteCancel().getSave().addClickListener(event -> {
			NodeEventType nodeEventType = newUpdateSourceNode.getCurrentSourceNode().getId() == null ? NodeEventType.ADD
					: NodeEventType.UPDATE;
			if (newUpdateSourceNode.getBinderFormSourceNode()
				.writeBeanIfValid(newUpdateSourceNode.getCurrentSourceNode())) {
				sourceView.getSourceLogic().saveSourceNode(newUpdateSourceNode.getCurrentSourceNode());
				load(getCurrentForwardNodeEntity());
				sourceView.getSourceLogic()
					.publishEvent(new NodeEvent(newUpdateSourceNode.getCurrentSourceNode(), nodeEventType));
			}
		});
	}

	private void addEventButtonDeleteNewUpdateSourceNode() {
		newUpdateSourceNode.getButtonSaveDeleteCancel().getDelete().addClickListener(event -> {
			if (newUpdateSourceNode.getCurrentSourceNode() != null) {
				ConfirmDialog dialog = new ConfirmDialog("确定要删除 DICOM 源节点 "
						+ newUpdateSourceNode.getCurrentSourceNode().getAeTitle() + " 吗？");
				dialog.addConfirmationListener(componentEvent -> {
					NodeEvent nodeEvent = new NodeEvent(newUpdateSourceNode.getCurrentSourceNode(),
							NodeEventType.REMOVE);
					sourceView.getSourceLogic().deleteSourceNode(newUpdateSourceNode.getCurrentSourceNode());
					load(getCurrentForwardNodeEntity());
					sourceView.getSourceLogic().publishEvent(nodeEvent);
				});
				dialog.open();
			}
		});
	}

	private void addEventButtonSaveNewUpdateDestination() {
		addEventButtonSaveNewUpdateDestinationDICOM();
		addEventButtonSaveNewUpdateDestinationSTOW();
	}

	private void addEventButtonSaveNewUpdateDestinationSTOW() {
		newUpdateDestination.getButtonDestinationSTOWSaveDeleteCancel().getSave().addClickListener(event -> {
			DestinationEntity currentDestinationEntity = newUpdateDestination.getCurrentDestinationEntity();

			if (currentDestinationEntity.getDestinationType() == DestinationType.stow
					&& newUpdateDestination.getBinderFormSTOW().writeBeanIfValid(currentDestinationEntity)) {
				// Reset / set defaults and save destination
				resetDefaultValuesAndSaveDestination(currentDestinationEntity,
						newUpdateDestination.getFormSTOW().getNotificationComponent(),
						newUpdateDestination.getFormSTOW().getDeIdentificationComponent());
			}
		});
	}

	private void addEventButtonSaveNewUpdateDestinationDICOM() {
		newUpdateDestination.getButtonDestinationDICOMSaveDeleteCancel().getSave().addClickListener(event -> {
			DestinationEntity currentDestinationEntity = newUpdateDestination.getCurrentDestinationEntity();

			if (currentDestinationEntity.getDestinationType() == DestinationType.dicom
					&& newUpdateDestination.getBinderFormDICOM().writeBeanIfValid(currentDestinationEntity)) {
				// Reset / set defaults and save destination
				resetDefaultValuesAndSaveDestination(currentDestinationEntity,
						newUpdateDestination.getFormDICOM().getNotificationComponent(),
						newUpdateDestination.getFormDICOM().getDeIdentificationComponent());
			}
		});
	}

	/**
	 * Reset / set defaults and save destination
	 * @param destinationEntity Destination
	 * @param notificationComponent Notification component
	 * @param deIdentificationComponent DeIdentification component
	 */
	private void resetDefaultValuesAndSaveDestination(DestinationEntity destinationEntity,
			NotificationComponent notificationComponent, DeIdentificationComponent deIdentificationComponent) {

		// Update notification default values if empty
		notificationComponent.updateDefaultValuesNotification(destinationEntity);

		// Clean fields of destination which are not saved because not selected by user
		deIdentificationComponent.cleanUnSavedData(destinationEntity);

		NodeEventType nodeEventType = destinationEntity.getId() == null ? NodeEventType.ADD : NodeEventType.UPDATE;

		// Save
		saveCurrentDestination(nodeEventType);
	}

	private void saveCurrentDestination(NodeEventType nodeEventType) {
		destinationView.getDestinationLogic().saveDestination(newUpdateDestination.getCurrentDestinationEntity());
		load(getCurrentForwardNodeEntity());
		destinationView.getDestinationLogic()
			.publishEvent(new NodeEvent(newUpdateDestination.getCurrentDestinationEntity(), nodeEventType));
	}

	private void addEventButtonDeleteNewUpdateDestination() {
		newUpdateDestination.getButtonDestinationDICOMSaveDeleteCancel()
			.getDelete()
			.addClickListener(event -> removeCurrentDestination());
		newUpdateDestination.getButtonDestinationSTOWSaveDeleteCancel()
			.getDelete()
			.addClickListener(event -> removeCurrentDestination());
	}

	private void removeCurrentDestination() {
		if (newUpdateDestination.getCurrentDestinationEntity() != null) {
			ConfirmDialog dialog = new ConfirmDialog("确定要删除目的地 "
					+ newUpdateDestination.getCurrentDestinationEntity().getDescription() + " ["
					+ newUpdateDestination.getCurrentDestinationEntity().getDestinationType() + "] 吗？"
					+ "<br>同时将删除监控视图中的相关条目。");
			dialog.addConfirmationListener(componentEvent -> {
				NodeEvent nodeEvent = new NodeEvent(newUpdateDestination.getCurrentDestinationEntity(),
						NodeEventType.REMOVE);
				destinationView.getDestinationLogic()
					.deleteDestination(newUpdateDestination.getCurrentDestinationEntity());
				destinationView.getDestinationLogic().publishEvent(nodeEvent);
				load(getCurrentForwardNodeEntity());
			});
			dialog.open();
		}
	}

	/**
	 * Add event checkbox deidentification
	 * @param deIdentificationComponent DeIdentification Component
	 * @param tagMorphingComponent Tag Morphing Component
	 */
	private void addEventCheckboxLayoutDesidentification(DeIdentificationComponent deIdentificationComponent,
			TagMorphingComponent tagMorphingComponent) {
		deIdentificationComponent.getDeIdentificationCheckbox().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				if (event.getValue()) {
					// Deactivate tag morphing
					tagMorphingComponent.getTagMorphingCheckbox().setValue(false);
					if (!projectService.retrieveAllProjects().isEmpty()) {
						deIdentificationComponent.getDeIdentificationDiv().setVisible(true);
						deIdentificationComponent.getDestinationComponentUtil()
							.setTextOnSelectionProject(deIdentificationComponent.getProjectDropDown().getValue(),
									deIdentificationComponent.getProfileLabel());
					}
					else {
						deIdentificationComponent.getWarningNoProjectsDefined().open();
					}
				}
				else {
					deIdentificationComponent.getDeIdentificationDiv().setVisible(false);
				}
			}
		});
	}

	/**
	 * Add event tag morphing checkbox
	 * @param deIdentificationComponent DeIdentification Component
	 * @param tagMorphingComponent Tag Morphing Component
	 */
	private void addEventCheckboxLayoutTagMorphing(TagMorphingComponent tagMorphingComponent,
			DeIdentificationComponent deIdentificationComponent) {
		tagMorphingComponent.getTagMorphingCheckbox().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				if (event.getValue()) {
					// Deactivate deidentification
					deIdentificationComponent.getDeIdentificationCheckbox().setValue(false);

					if (!projectService.retrieveAllProjects().isEmpty()) {
						tagMorphingComponent.getTagMorphingDiv().setVisible(true);
						tagMorphingComponent.getDestinationComponentUtil()
							.setTextOnSelectionProject(tagMorphingComponent.getProjectDropDown().getValue(),
									tagMorphingComponent.getProfileLabel());
					}
					else {
						tagMorphingComponent.getWarningNoProjectsDefined().open();
					}
				}
				else {
					tagMorphingComponent.getTagMorphingDiv().setVisible(false);
				}
			}
		});
	}

	private void addBindersFilterBySOPClassesForm(FilterBySOPClassesForm filterBySOPClassesForm) {
		filterBySOPClassesForm.getBinder()
			.forField(filterBySOPClassesForm.getSopFilter())
			.withValidator(
					listOfSOPFilter -> !listOfSOPFilter.isEmpty()
							|| !filterBySOPClassesForm.getFilterBySOPClassesCheckbox().getValue(),
					"未应用任何筛选\n")
			.bind(DestinationEntity::retrieveSOPClassUIDFiltersName, (destination, sopClassNames) -> {
				Set<SOPClassUIDEntity> newSOPClassUIDEntities = new HashSet<>();
				sopClassNames.forEach(sopClasseName -> {
					SOPClassUIDEntity sopClassUIDEntity = sopClassUIDService.getByName(sopClasseName);
					newSOPClassUIDEntities.add(sopClassUIDEntity);
				});
				destination.setSOPClassUIDEntityFilters(newSOPClassUIDEntities);
			});

		filterBySOPClassesForm.getBinder()
			.forField(filterBySOPClassesForm.getFilterBySOPClassesCheckbox()) //
			.bind(DestinationEntity::isFilterBySOPClasses, DestinationEntity::setFilterBySOPClasses);
	}

	public void addBinderExtidInDicomTag(DeIdentificationComponent deIdentificationComponent) {
		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymInDicomTagComponent().getTag())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value, "必须是 Tag")
			.withValidator(tag -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_IN_TAG.getValue())) {
					return true;
				}
				final String cleanTag = tag.replaceAll("[(),]", "").toUpperCase();
				try {
					TagUtils.intFromHexString(cleanTag);
				}
				catch (Exception e) {
					return false;
				}
				return !tag.isEmpty() && cleanTag.length() == 8;
			}, "请选择有效的 Tag\n")
			.bind(DestinationEntity::getTag, DestinationEntity::setTag);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymInDicomTagComponent().getDelimiter())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value, "必须是分隔符")
			.withValidator(delimiter -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_IN_TAG.getValue())) {
					return true;
				}
				if (deIdentificationComponent.getPseudonymInDicomTagComponent().getPosition().getValue() != null
						&& deIdentificationComponent.getPseudonymInDicomTagComponent().getPosition().getValue() > 0) {
					return delimiter != null && !delimiter.isEmpty();
				}
				return true;
			}, "存在位置时必须定义分隔符")
			.bind(DestinationEntity::getDelimiter, DestinationEntity::setDelimiter);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymInDicomTagComponent().getPosition())
			.withConverter(new DoubleToIntegerConverter())
			.withValidator(position -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_IN_TAG.getValue())) {
					return true;
				}
				if (deIdentificationComponent.getPseudonymInDicomTagComponent().getDelimiter().getValue() != null
						&& !deIdentificationComponent.getPseudonymInDicomTagComponent()
							.getDelimiter()
							.getValue()
							.isEmpty()) {
					return position != null && position >= 0;
				}
				return true;
			}, "存在分隔符时必须定义位置")
			.bind(DestinationEntity::getPosition, DestinationEntity::setPosition);
	}

	public void addBinderExtidFromApi(DeIdentificationComponent deIdentificationComponent) {
		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymFromApiComponent().getUrl())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value, "必须是 URL")
			.withValidator(url -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_API.getValue())) {
					return true;
				}
				return (url != null && !url.isEmpty());
			}, "请输入有效的 URL\n")
			.bind(DestinationEntity::getPseudonymUrl, DestinationEntity::setPseudonymUrl);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymFromApiComponent().getMethod())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value, "必须是 GET 或 POST 方法")
			.withValidator(method -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_API.getValue())) {
					return true;
				}
				return method.equals("GET") || method.equals("POST");
			}, "方法必须为 GET 或 POST")
			.bind(DestinationEntity::getMethod, DestinationEntity::setMethod);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymFromApiComponent().getBody())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value)
			.withValidator(body -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_API.getValue())) {
					return true;
				}
				if (deIdentificationComponent.getPseudonymFromApiComponent().getMethod().getValue().equals("POST")) {
					return body != null && !body.isEmpty();
				}
				return true;
			}, "POST 请求必须填写 Body")
			.bind(DestinationEntity::getBody, DestinationEntity::setBody);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymFromApiComponent().getAuthConfig())
			// The combo yields null when no identifier is selected; store it as an empty
			// string rather than the literal "null".
			.withConverter(value -> (value == null) ? "" : value,
					value -> (value == null || value.isEmpty()) ? null : value)
			.bind(DestinationEntity::getAuthConfig, DestinationEntity::setAuthConfig);

		deIdentificationComponent.getDestinationBinder()
			.forField(deIdentificationComponent.getPseudonymFromApiComponent().getResponsePath())
			.withConverter(String::valueOf, value -> (value == null) ? "" : value, "Response Path 为必填项")
			.withValidator(responsePath -> {
				if (!deIdentificationComponent.getDeIdentificationCheckbox().getValue()
						|| !deIdentificationComponent.getPseudonymTypeSelect()
							.getValue()
							.equals(EXTID_API.getValue())) {
					return true;
				}
				return responsePath != null && !responsePath.isEmpty();
			}, "JSON Response Path 为必填项")
			.bind(DestinationEntity::getResponsePath, DestinationEntity::setResponsePath);
	}

	/**
	 * Event on checkbox Transcode Only Uncompressed: deactivate for some transfer syntax
	 * Keep original + Explicit VR
	 */
	private void addEventTranscodeOnlyUncompressedWhenSomeTransferSyntax(Select<String> transferSyntaxSelect,
			TranscodeOnlyUncompressedComponent transcodeOnlyUncompressedComponent) {
		transferSyntaxSelect.addValueChangeListener(
				value -> newUpdateDestination.handleEventTranscodeOnlyUncompressedWhenSomeTransferSyntax(
						transcodeOnlyUncompressedComponent, value.getValue(), true));
	}

}
