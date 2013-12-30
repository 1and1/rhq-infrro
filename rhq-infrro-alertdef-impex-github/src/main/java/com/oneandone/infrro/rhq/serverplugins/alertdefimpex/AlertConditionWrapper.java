package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.rhq.core.domain.alert.AlertCondition;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementCategory;
import org.rhq.core.domain.measurement.MeasurementDefinition;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AlertConditionWrapper extends AlertCondition {

	private static final long serialVersionUID = 1L;
	
	private MeasurementCategory mDefCategory;
	private DataType mDefDataType;
//	private String mDefDisplayName;
	private String mDefName;
	private String mDefResTypeName;
	private String mDefPlugin;
	
	public AlertConditionWrapper() {
	}
	
	public AlertConditionWrapper(AlertCondition condition) {
		this.setCategory(condition.getCategory());
		this.setComparator(condition.getComparator());
		condition.getConditionLogs();
		
		MeasurementDefinition measurementDefinition = condition.getMeasurementDefinition();
		if (measurementDefinition != null) {
			mDefCategory = measurementDefinition.getCategory();
			mDefDataType = measurementDefinition.getDataType();
//			mDefDisplayName = measurementDefinition.getDisplayName();
			mDefName = measurementDefinition.getName();
			mDefResTypeName = measurementDefinition.getResourceType().getName();
			setmDefPlugin(measurementDefinition.getResourceType().getPlugin());
		}
		
		this.setName(condition.getName());
		this.setOption(condition.getOption());
		this.setThreshold(condition.getThreshold());
		this.setTriggerId(condition.getTriggerId());
	}

	public MeasurementCategory getmDefCategory() {
	    return mDefCategory;
	}

	public void setmDefCategory(MeasurementCategory mDefCategory) {
	    this.mDefCategory = mDefCategory;
	}

	public DataType getmDefDataType() {
	    return mDefDataType;
	}

	public void setmDefDataType(DataType mDefDataType) {
	    this.mDefDataType = mDefDataType;
	}

	public String getmDefName() {
	    return mDefName;
	}

	public void setmDefName(String mDefName) {
	    this.mDefName = mDefName;
	}

	public String getmDefResTypeName() {
	    return mDefResTypeName;
	}

	public void setmDefResTypeName(String mDefResTypeName) {
	    this.mDefResTypeName = mDefResTypeName;
	}

	public String getmDefPlugin() {
	    return mDefPlugin;
	}

	public void setmDefPlugin(String mDefPlugin) {
	    this.mDefPlugin = mDefPlugin;
	}

}
