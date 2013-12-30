package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.rhq.core.domain.alert.AlertCondition;
import org.rhq.core.domain.alert.AlertDampening;
import org.rhq.core.domain.alert.AlertDefinition;
import org.rhq.core.domain.alert.notification.AlertNotification;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AlertDefinitionWrapper extends AlertDefinition {

	private static final long serialVersionUID = 1L;

	private Integer groupAlertDefinitionId;

	private String resourceName;
	
	private String resourceTypeName;
	
	private Set<AlertConditionWrapper> conditionWrappers = new HashSet<AlertConditionWrapper>();
	
	public AlertDefinitionWrapper() {
	}
	
	public AlertDefinitionWrapper(AlertDefinition alertDefinition) {
		// TODO maybe will be needed for some special cases
//		alertDefinition.getResource().getResourceType().getPlugin();

		// i need the resourceType if is a template definition
		Integer parentId = alertDefinition.getParentId();
		if (parentId == 0) {
			this.setResourceTypeName(alertDefinition.getResourceType().getName());
		} else {
			// i need the group if is a group definition
			AlertDefinition groupAlertDefinition = alertDefinition.getGroupAlertDefinition();
			if (groupAlertDefinition == null) {
				this.setGroupAlertDefinition(alertDefinition.getGroupAlertDefinition());
			} else {
				// i need the resource name and type if is a solo definition
				this.setResourceName(alertDefinition.getResource().getName());
				this.setResourceTypeName(alertDefinition.getResource().getResourceType().getName());
			}
		}
		
		// dampening events create cycle in JAXB, and we don't even need them, so we remove them
		AlertDampening alertDampening = alertDefinition.getAlertDampening();
		alertDampening.clearAlertDampeningEvents();
		
		this.setAlertDampening(alertDampening);
		this.setConditionExpression(alertDefinition.getConditionExpression());
		this.setDescription(alertDefinition.getDescription());
		this.setEnabled(alertDefinition.getEnabled());
		this.setName(alertDefinition.getName());
		this.setPriority(alertDefinition.getPriority());
		this.setRecoveryId(alertDefinition.getRecoveryId());
		this.setWillRecover(alertDefinition.getWillRecover());
		
		// now process fields which cause infinite cycle in object graph
		
		int id = alertDefinition.getGroupAlertDefinition()==null ? -1 : alertDefinition.getGroupAlertDefinition().getId();
		this.setGroupAlertDefinitionId(id);
		this.setGroupAlertDefinition(null);
		
		Set<AlertCondition> conditions = alertDefinition.getConditions();
		Set<AlertConditionWrapper> conditionsModified = new HashSet<AlertConditionWrapper>();
		for (AlertCondition condition : conditions) {
			AlertConditionWrapper conditionModified = new AlertConditionWrapper(condition);
			conditionsModified.add(conditionModified);
		}
		this.setConditionWrappers(conditionsModified);

		List<AlertNotification> alertNotifications = alertDefinition.getAlertNotifications();
		List<AlertNotification> alertNotificationsModified = new ArrayList<AlertNotification>();
		for (AlertNotification alertNotification : alertNotifications) {
			AlertNotificationWrapper alertNotificationModified = new AlertNotificationWrapper(alertNotification);
			alertNotificationsModified.add(alertNotificationModified);
		}
		this.setAlertNotifications(alertNotificationsModified); 

	}
	
	public Integer getGroupAlertDefinitionId() {
		return groupAlertDefinitionId;
	}

	public void setGroupAlertDefinitionId(Integer groupAlertDefinitionId) {
		this.groupAlertDefinitionId = groupAlertDefinitionId;
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Set<AlertConditionWrapper> getConditionWrappers() {
	    return conditionWrappers;
	}

	public void setConditionWrappers(Set<AlertConditionWrapper> conditionWrappers) {
	    this.conditionWrappers = conditionWrappers;
	}

}
