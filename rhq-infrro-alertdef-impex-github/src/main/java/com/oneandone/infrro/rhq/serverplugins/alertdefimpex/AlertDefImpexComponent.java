package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.alert.AlertCondition;
import org.rhq.core.domain.alert.AlertDefinition;
import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.criteria.AlertDefinitionCriteria;
import org.rhq.core.domain.criteria.MeasurementDefinitionCriteria;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.criteria.ResourceTypeCriteria;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.resource.group.ResourceGroup;
import org.rhq.core.domain.util.PageControl;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.server.alert.AlertDefinitionManagerLocal;
import org.rhq.enterprise.server.alert.AlertTemplateManagerLocal;
import org.rhq.enterprise.server.alert.GroupAlertDefinitionManagerLocal;
import org.rhq.enterprise.server.measurement.MeasurementDefinitionManagerLocal;
import org.rhq.enterprise.server.plugin.pc.ControlFacet;
import org.rhq.enterprise.server.plugin.pc.ControlResults;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;
import org.rhq.enterprise.server.resource.ResourceManagerLocal;
import org.rhq.enterprise.server.resource.ResourceTypeManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;

/**
 * 
 * Class usage : Server plugin for importing/exporting alert definitions from/in an XML file.
 * 
 * TODO upload/download file Normally this should download/upload a file from the browser but this is not possible because server
 * plugins are limited, I cannot put here a {@link FileUploadForm} and also I would need to add a servlet in coregui, which is
 * beyond the scope of a server plugin. So it works with a file in the RHQ server file-system instead.
 * 
 * TODO {@link AlertDefinition} should be JAXB marshal-able, to avoid complications in java-xml transformation. 
 * some fields should be XmlTransient and some not: 
 * - alertDefinition -> groupAlertDefinition 
 * - alertDefinition -> alertNotification -> alertDefinition 
 * - alertDefinition -> alertDampening -> set of AlertDampeningEvent -> alertDefinition 
 * - alertDefinition -> condition -> MeasurementDefinition is marked as transient
 * - alertDefinition -> ResourceType -> Set<ResourceType> childResourceTypes && parentResourceTypes
 * 
 * @author <a href="mailto:vlad.craciunoiu@1and1.ro">Vlad Craciunoiu</a>
 * 
 * @created on Jul 10, 2013 6:51:44 PM
 * 
 * @version $Id$
 * 
 */
public class AlertDefImpexComponent implements ServerPluginComponent, ControlFacet {

	protected static final Log logger = LogFactory.getLog(AlertDefImpexComponent.class);

	private String fileName;

	private MeasurementDefinitionManagerLocal measurementDefinitionManager;

	private AlertDefinitionManagerLocal alertDefinitionManager;
	
	private GroupAlertDefinitionManagerLocal alertDefinitionGroupManager;

	private AlertTemplateManagerLocal alertDefinitionTemplateManager;

	private ResourceManagerLocal resourceManager;
	
	private ResourceTypeManagerLocal resourceTypeManager;

	private Subject subject;

	private Boolean onlyExportTemplates;

	public void initialize(ServerPluginContext context) throws Exception {
		logger.info("Initializing.");
		fileName = context.getPluginConfiguration().getSimple("workingFile").getStringValue();
		logger.info("File name is: " + fileName);
	}

	public ControlResults invoke(String name, Configuration parameters) {
		ControlResults results = new ControlResults();

		subject = LookupUtil.getSubjectManager().getOverlord();
		alertDefinitionManager = LookupUtil.getAlertDefinitionManager();
		alertDefinitionGroupManager = LookupUtil.getGroupAlertDefinitionManager();
		alertDefinitionTemplateManager = LookupUtil.getAlertTemplateManager();
		resourceManager = LookupUtil.getResourceManager();
		measurementDefinitionManager = LookupUtil.getMeasurementDefinitionManager();
		resourceTypeManager = LookupUtil.getResourceTypeManager();

		onlyExportTemplates = Boolean.valueOf(parameters.getSimpleValue("Templates", "false"));

		if ("exportDefinitions".equals(name)) {
			doExport(results);
		}

		if ("importDefinitions".equals(name)) {
			doImport(results);
		}

		return results;
	}

	private void doExport(ControlResults results) {
		String resultValue;
		PageList<AlertDefinition> alertDefinitions = new PageList<AlertDefinition>();
		
		AlertDefinitionCriteria criteria = new AlertDefinitionCriteria();
		// criteria.addFilterResourceOnly(true);
		criteria.addFilterAlertTemplateOnly(onlyExportTemplates);
		criteria.fetchGroupAlertDefinition(true);
		criteria.setPageControl(PageControl.getUnlimitedInstance());
		
		PageList<AlertDefinition> alertDefinitionsRaw = alertDefinitionManager.findAlertDefinitionsByCriteria(subject,
				criteria);
		
		// now we exclude unwanted alerts, which comes from a template or a group
		for (AlertDefinition alertDefinition : alertDefinitionsRaw) {
			Integer parentId = alertDefinition.getParentId();
		
			AlertDefinition groupAlertDefinition = alertDefinition.getGroupAlertDefinition();
			int groupDefId = groupAlertDefinition == null ? 0 : groupAlertDefinition.getId();
		
			Resource resource = alertDefinition.getResource();
			String resourceName = resource == null ? "" : resource.getName();
			int resourceId = resource == null ? 0 : resource.getId();
		
			ResourceGroup group = alertDefinition.getGroup();
			String groupName = group == null ? "" : group.getName();
			int groupId = group == null ? 0 : group.getId();
		
			if (parentId != 0) {
				// if it has parentId then it means it comes from a template -> ignore it
			} else {
				if (groupAlertDefinition != null) {
					// if it has group alert def then it means it comes from a group def -> ignore it
				} else {
					// now we have 3 possibilities: it's a template, a group or a solo def
					alertDefinitions.add(alertDefinition);
				}
			}
		
			System.out.println("alert=" + alertDefinition.getId() + "|" + alertDefinition.getName() + ", " + parentId
					+ ", resource=" + resourceName + "|" + resourceId + "|" + alertDefinition.getResourceType()
					+ ", groupAlertDefinitionId=" + groupDefId 
					+ ", group=" + groupName + "|" + groupId);
		}

		if (alertDefinitions.size() == 0) {
			resultValue = "There is no alert definition to export.";
		} else {
			try {
				JAXBContext contextForConfiguration = JAXBContext.newInstance(AlertDefinitionWrappers.class);
				Marshaller m = contextForConfiguration.createMarshaller();
				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

				AlertDefinitionWrappers alertDefinitionsCustom = new AlertDefinitionWrappers(
						alertDefinitions.getValues());
				m.marshal(alertDefinitionsCustom, new File(fileName));

				resultValue = "OK";
			} catch (Exception e) {
				e.printStackTrace();
				resultValue = e.getMessage();
			}
		}

		results.getComplexResults().setSimpleValue("exportResult", resultValue);
		logger.info("Alert definitions export result: " + resultValue);
	}

	private void doImport(ControlResults results) {
		String resultValue;

		try {
			JAXBContext contextForConfiguration = JAXBContext.newInstance(AlertDefinitionWrappers.class);
			Unmarshaller m = contextForConfiguration.createUnmarshaller();

			AlertDefinitionWrappers alertDefinitionsCustom = (AlertDefinitionWrappers) m
					.unmarshal(new File(fileName));

			ResourceCriteria criteria;
			ResourceTypeCriteria resourceTypeCriteria;

			for (AlertDefinitionWrapper alertDefWrapper : alertDefinitionsCustom.values) {

				AlertDefinition alertDef = new AlertDefinition(alertDefWrapper);

				// TODO maybe I need to set some of the values [back] in the REAL alert definition object
				// ... groupAlertDefinitionId ?
				// alertDefWrapper.setResource(resources.get(0));
				
				manipulateConditions(alertDefWrapper, alertDef);

				// after I have created the REAL alert definition object, I persist it
				
				String resourceTypeName = alertDefWrapper.getResourceTypeName();
				if (alertDef.getResourceType() != null) {
					resourceTypeCriteria = new ResourceTypeCriteria();
					resourceTypeCriteria.addFilterName(resourceTypeName);
					PageList<ResourceType> resourceTypes = 	resourceTypeManager.findResourceTypesByCriteria(subject, resourceTypeCriteria);
					ResourceType resourceType = resourceTypes.get(0);
					if (resourceType == null) {
						logger.info("couldn't find resource type with name " + resourceTypeName + ". Aborting the creation of this alert definition.");
					} else {
						Integer resourceTypeId = ((ResourceType) resourceType).getId();
						alertDefinitionTemplateManager.createAlertTemplate(subject, alertDef, resourceTypeId);
					}
				} else if (alertDef.getGroup() != null) {
					alertDefinitionGroupManager.createGroupAlertDefinitions(subject, alertDef, alertDef.getGroup().getId());
				} else {
					// find the resource to create the alert def on it
					criteria = new ResourceCriteria();
					criteria.addFilterName(alertDefWrapper.getResourceName());
					criteria.addFilterResourceTypeName(resourceTypeName);
					// criteria.addFilterPluginName(filterPluginName);

					PageList<Resource> resources = resourceManager.findResourcesByCriteria(subject, criteria);

					// theoretically there may be more resources with the same name and the same type !!! oare ?
					Resource resource = resources.get(0);
					alertDefinitionManager.createAlertDefinitionInNewTransaction(subject, alertDef, resource.getId(), false);
				}
			}

			resultValue = "OK";

		} catch (Exception e) {
			e.printStackTrace();
			resultValue = e.getMessage();
		}

		results.getComplexResults().setSimpleValue("importResult", resultValue);
		logger.info("Alert definitions import result: " + resultValue);
	}

	private void manipulateConditions(AlertDefinitionWrapper alertDefWrapper, AlertDefinition alertDef) {
		Set<AlertCondition> conditions = new HashSet<AlertCondition>();

		for (AlertConditionWrapper conditionWrapper : alertDefWrapper.getConditionWrappers()) {
			AlertCondition condition = new AlertCondition(conditionWrapper);

			MeasurementDefinitionCriteria measurementDefCriteria = new MeasurementDefinitionCriteria();

			measurementDefCriteria.addFilterCategory(conditionWrapper.getmDefCategory());
			measurementDefCriteria.addFilterDataType(conditionWrapper.getmDefDataType());
			measurementDefCriteria.addFilterDisplayName(condition.getName());
			measurementDefCriteria.addFilterName(conditionWrapper.getmDefName());
			measurementDefCriteria.addFilterResourceTypeName(conditionWrapper.getmDefResTypeName());

			PageList<MeasurementDefinition> mdefs = measurementDefinitionManager
					.findMeasurementDefinitionsByCriteria(subject, measurementDefCriteria);

			if (mdefs.size() > 0) {
				for (MeasurementDefinition mdef : mdefs) {
					if (mdef.getResourceType().getPlugin().equals(conditionWrapper.getmDefPlugin())
							&& mdef.getName().equals(conditionWrapper.getmDefName())) {
						condition.setMeasurementDefinition(mdef);
						break;
					}
				}
			}

			conditions.add(condition);
		}

		alertDef.setConditions(conditions);
	}

	public void start() {
		logger.info("Starting.");
	}

	public void stop() {
		logger.info("Stopping.");
	}

	public void shutdown() {
		logger.info("Shutting down.");
	}

}
