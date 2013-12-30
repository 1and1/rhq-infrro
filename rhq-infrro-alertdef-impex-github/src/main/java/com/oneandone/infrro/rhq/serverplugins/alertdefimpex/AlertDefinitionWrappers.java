package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.rhq.core.domain.alert.AlertDefinition;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AlertDefinitionWrappers {

	// @XmlJavaTypeAdapter(AlertDefinitionAdapter.class)
	List<AlertDefinitionWrapper> values = new ArrayList<AlertDefinitionWrapper>();

	public AlertDefinitionWrappers() {
	}

	public AlertDefinitionWrappers(List<AlertDefinition> valorile) {
		for (AlertDefinition alertDefinition : valorile) {
			AlertDefinitionWrapper alertDefinitionModified = new AlertDefinitionWrapper(alertDefinition);
			this.values.add(alertDefinitionModified);
		}
	}

	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer();
		for (AlertDefinition alertDefinition : values) {
			tmp.append(alertDefinition.toString());
			tmp.append("\n");

		}
		return tmp.toString();
	}

}
