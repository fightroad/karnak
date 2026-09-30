/*
 * Copyright (c) 2020-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.project.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import org.karnak.backend.data.entity.DestinationEntity;
import org.karnak.backend.data.entity.ProjectEntity;
import org.weasis.core.util.annotations.Generated;

@Generated()
public class WarningRemoveProjectUsed extends Dialog {

	public void setText(ProjectEntity projectEntity) {
		removeAll();
		Div divTitle = new Div();
		divTitle.setText(String.format("无法删除项目 %s", projectEntity.getName()));
		divTitle.addClassNames("karnak-dialog-title", "karnak-error-text");

		Div divContent = new Div();
		Div divIntro = new Div();
		divIntro.setText("该项目正被以下目的地使用");
		divIntro.getStyle().set("padding-bottom", "10px");

		divContent.add(divIntro);
		if (projectEntity.getAllDestinations() != null) {
			for (DestinationEntity destinationEntity : projectEntity.getAllDestinations()) {
				Div divDestination = new Div();
				divDestination.setText(String.format("类型: %s, 描述: %s, 转发节点: %s",
						destinationEntity.getDestinationType(), destinationEntity.getDescription(),
						destinationEntity.getForwardNodeEntity().getFwdAeTitle()));
				divDestination.getStyle().set("padding-left", "20px").set("padding-bottom", "5px");
				divContent.add(divDestination);
			}
		}

		Button cancelButton = new Button("取消", event -> close());

		cancelButton.getStyle().set("margin-left", "75%");
		add(divTitle, divContent, cancelButton);
	}

}
