package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.rhq.core.domain.alert.notification.AlertNotification;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AlertNotificationWrapper extends AlertNotification {

	private static final long serialVersionUID = 1L;
	
	//TODO do i need it ??
	private Integer alertDefinitionId;

	public AlertNotificationWrapper() {
	}
	
	public AlertNotificationWrapper(AlertNotification alertNotification) {
		this.setAlertDefinitionId(alertNotification.getAlertDefinition().getId());
		this.setConfiguration(alertNotification.getConfiguration());
		this.setExtraConfiguration(alertNotification.getExtraConfiguration());
		this.setSenderName(alertNotification.getSenderName());
	}

	public Integer getAlertDefinitionId() {
		return alertDefinitionId;
	}

	public void setAlertDefinitionId(Integer alertDefinitionId) {
		this.alertDefinitionId = alertDefinitionId;
	}

}
