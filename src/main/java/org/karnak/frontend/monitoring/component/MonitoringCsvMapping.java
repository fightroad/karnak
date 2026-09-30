/*
 * Copyright (c) 2022-2026 Karnak Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.karnak.frontend.monitoring.component;

/**
 * Mapping for the per-series monitoring CSV export. Handles the order of the columns and
 * the mapping to fields of {@code TransferSeriesStatusEntity} (and the recursed forward
 * node / destination). The order of the enum determines the column order in the CSV file.
 */
public enum MonitoringCsvMapping {

	FORWARD_AETITLE("fwdAeTitle", "转发 AE Title"), FORWARD_DESCRIPTION("fwdDescription", "转发描述"),
	DESTINATION_HOSTNAME("hostname", "目标主机名"), DESTINATION_AETITLE("aeTitle", "目标 aeTitle"),
	DESTINATION_PORT("port", "目标端口"), DESTINATION_URL("url", "目标 URL"),
	DESTINATION_DESCRIPTION("description", "目标描述"),
	PATIENT_ID_ORIGINAL("patientIdOriginal", "患者 ID 原始值"),
	PATIENT_ID_TO_SEND("patientIdToSend", "患者 ID 发送值"),
	ACCESSION_NUMBER_ORIGINAL("accessionNumberOriginal", "检查号 原始值"),
	ACCESSION_NUMBER_TO_SEND("accessionNumberToSend", "检查号 发送值"),
	STUDY_UID_ORIGINAL("studyUidOriginal", "检查 UID 原始值"),
	STUDY_UID_TO_SEND("studyUidToSend", "检查 UID 发送值"),
	STUDY_DESCRIPTION_ORIGINAL("studyDescriptionOriginal", "检查描述 原始值"),
	STUDY_DESCRIPTION_TO_SEND("studyDescriptionToSend", "检查描述 发送值"),
	STUDY_DATE_ORIGINAL("studyDateOriginal", "检查日期 原始值"),
	STUDY_DATE_TO_SEND("studyDateToSend", "检查日期 发送值"),
	SERIE_UID_ORIGINAL("serieUidOriginal", "序列 UID 原始值"),
	SERIE_UID_TO_SEND("serieUidToSend", "序列 UID 发送值"),
	SERIE_DESCRIPTION_ORIGINAL("serieDescriptionOriginal", "序列描述 原始值"),
	SERIE_DESCRIPTION_TO_SEND("serieDescriptionToSend", "序列描述 发送值"),
	SERIE_DATE_ORIGINAL("serieDateOriginal", "序列日期 原始值"),
	SERIE_DATE_TO_SEND("serieDateToSend", "序列日期 发送值"), MODALITY("modality", "模态"),
	SOP_CLASS_UIDS("sopClassUids", "SOP Class UID"), INSTANCES("instances", "实例"),
	RETRIES("retries", "重试"), SENT("sent", "已发送"), ERRORS("errors", "错误"), EXCLUDED("excluded", "已排除"),
	REASONS("reasons", "原因"), FIRST_SEEN("firstSeen", "首次出现"), LAST_SEEN("lastSeen", "最后出现");

	// Name of the field of the entity
	private final String nameFieldEntity;

	// Name of the column header of the Csv file
	private final String labelCsv;

	/**
	 * Constructor
	 * @param nameFieldEntity Name of the field of the entity to retrieve
	 * @param labelCsv Name of the column header of the Csv file
	 */
	MonitoringCsvMapping(String nameFieldEntity, String labelCsv) {
		this.nameFieldEntity = nameFieldEntity;
		this.labelCsv = labelCsv;
	}

	public String getNameFieldEntity() {
		return nameFieldEntity;
	}

	public String getLabelCsv() {
		return labelCsv;
	}

}
