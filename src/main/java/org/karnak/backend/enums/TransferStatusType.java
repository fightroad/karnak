/*
 * Copyright (c) 2022-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.backend.enums;

import lombok.Getter;
import org.jspecify.annotations.NullUnmarked;

/**
 * Enum for the transfer status
 */
@Getter
@NullUnmarked
public enum TransferStatusType {

	ALL(null, null, "全部"), SENT(true, false, "已发送"), NOT_SENT(false, null, "未发送"),
	EXCLUDED(false, false, "已排除"), ERROR(false, true, "错误");

	/**
	 * Predicate value for the sent attribute
	 */
	private final Boolean sent;

	/**
	 * Predicate value for the error attribute
	 */
	private final Boolean error;

	/**
	 * Label of the filter value
	 */
	private final String label;

	TransferStatusType(Boolean sent, Boolean error, String label) {
		this.label = label;
		this.sent = sent;
		this.error = error;
	}

}
