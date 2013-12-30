package com.oneandone.infrro.rhq.serverplugins.alertdefimpex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.rhq.core.domain.alert.AlertCondition;
import org.rhq.core.domain.alert.AlertDampening;
import org.rhq.core.domain.alert.AlertDefinition;
import org.rhq.core.domain.alert.AlertPriority;
import org.rhq.core.domain.alert.BooleanExpression;
import org.rhq.core.domain.alert.AlertDampening.Category;
import org.rhq.core.domain.alert.notification.AlertNotification;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.util.PageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Util {

	private static final String ENABLED = "enabled";
	private static final String CONDITION_EXPRESSION = "conditionExpression";
	private static final String RECOVERY_ID = "recoveryId";
	private static final String DAMPENING_CATEGORY = "dampeningCategory";
	private static final String RESOURCE_TYPE = "resourceType";
	private static final String RESOURCE_NAME = "resourceName";
	private static final String ALERT_DEFINITION = "alertDefinition";
	private static final String ALERT_PRIORITY = "priority";
	private static final String ALERT_DESCRIPTION = "description";
	private static final String ALERT_NAME = "alertName";
	
	private void buildXmlFromJavaObject(PageList<AlertDefinition> alertDefinitions) throws Exception {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
		Document doc1 = docBuilder.newDocument();

		// this is for alert.notification.configuration, that class can be marshaled
	    JAXBContext contextForConfiguration = JAXBContext.newInstance(Configuration.class);
	    Marshaller m = contextForConfiguration.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	    StringWriter sw = new StringWriter(); 
		
	    Element root = doc1.createElement("alertDefinitions");
		doc1.appendChild(root);
		
		for (AlertDefinition alertDefinition : alertDefinitions) {
			Node alertDefinitionNode = doc1.createElement(ALERT_DEFINITION);
			root.appendChild(alertDefinitionNode);
			
			String resourceName = alertDefinition.getResource().getName();
			Node resourceNameNode = doc1.createElement(RESOURCE_NAME);
			resourceNameNode.setTextContent(resourceName);
			alertDefinitionNode.appendChild(resourceNameNode);
			
			String resourceType = alertDefinition.getResource().getResourceType().getName();
			Node resourceTypeNode = doc1.createElement(RESOURCE_TYPE);
			resourceTypeNode.setTextContent(resourceType);
			alertDefinitionNode.appendChild(resourceTypeNode);
			
			String alertName = alertDefinition.getName();
			Node alertNameNode = doc1.createElement(ALERT_NAME);
			alertNameNode.setTextContent(alertName);
			alertDefinitionNode.appendChild(alertNameNode);

			String description = alertDefinition.getDescription();
			Node descriptionNode = doc1.createElement(ALERT_DESCRIPTION);
			descriptionNode.setTextContent(description);
			alertDefinitionNode.appendChild(descriptionNode);

			String priority = alertDefinition.getPriority().getName();
			Node priorityNode = doc1.createElement(ALERT_PRIORITY);
			priorityNode.setTextContent(priority);
			alertDefinitionNode.appendChild(priorityNode);

			Boolean enabled = alertDefinition.getEnabled();
			Node enabledNode = doc1.createElement(ENABLED);
			enabledNode.setTextContent(enabled.toString());
			alertDefinitionNode.appendChild(enabledNode);
			
			// conditions
			Set<AlertCondition> conditions = alertDefinition.getConditions();
			Node conditionsNode = doc1.createElement("conditions");
			alertDefinitionNode.appendChild(conditionsNode);
			
			for (AlertCondition condition : conditions) {
				Node conditionNode = doc1.createElement("condition");
				alertDefinitionNode.appendChild(conditionNode);

				String conditionCategoryName = condition.getCategory().getName();
				Node conditionCategoryNameNode = doc1.createElement("categoryName");
				conditionCategoryNameNode.setTextContent(conditionCategoryName);
				conditionNode.appendChild(conditionCategoryNameNode);
				
				String comparator = condition.getComparator();
				Node comparatorNode = doc1.createElement("comparator");
				comparatorNode.setTextContent(comparator);
				conditionNode.appendChild(comparatorNode);
				
				String logs = condition.getConditionLogs().toString();
				Node logsNode = doc1.createElement("logs");
				logsNode.setTextContent(logs);
				conditionNode.appendChild(logsNode);
				
				MeasurementDefinition measurementDefinition2 = condition.getMeasurementDefinition();
				String measurementDefinition = measurementDefinition2==null ? "" : measurementDefinition2.toString();
				
				Node measurementDefinitionNode = doc1.createElement("measurementDefinition");
				measurementDefinitionNode.setTextContent(measurementDefinition);
				conditionNode.appendChild(measurementDefinitionNode);
				
				String conditionName = condition.getName();
				Node conditionNameNode = doc1.createElement("conditionName");
				conditionNameNode.setTextContent(conditionName);
				conditionNode.appendChild(conditionNameNode);
				
				String option = condition.getOption();
				Node optionNode = doc1.createElement("option");
				optionNode.setTextContent(option);
				conditionNode.appendChild(optionNode);
				
				Double threshold = condition.getThreshold();
				Node thresholdNode = doc1.createElement("threshold");
				thresholdNode.setTextContent(String.valueOf(threshold));
				conditionNode.appendChild(thresholdNode);
			}
			
			String conditionExpression = alertDefinition.getConditionExpression().name();
			Node conditionExpressionNode = doc1.createElement(CONDITION_EXPRESSION);
			conditionExpressionNode.setTextContent(conditionExpression);
			alertDefinitionNode.appendChild(conditionExpressionNode);
			
			// notifications
			List<AlertNotification> alertNotifications = alertDefinition.getAlertNotifications();
			Node notificationsNode = doc1.createElement("notifications");
			alertDefinitionNode.appendChild(notificationsNode);
			
			for (AlertNotification alertNotification : alertNotifications) {
				Node notificationNode = doc1.createElement("notification");
				notificationsNode.appendChild(notificationNode);

				Configuration configuration = alertNotification.getConfiguration();
				m.marshal(configuration, sw);
				String configurationMarshalledString = sw.toString();
				Node configurationNode = doc1.createElement("configuration");
				configurationNode.setTextContent(configurationMarshalledString);
				notificationNode.appendChild(configurationNode);
				    
				Configuration extraConfiguration = alertNotification.getExtraConfiguration();
				m.marshal(extraConfiguration, sw);
				String extraConfigurationMarshalledString = sw.toString();
				Node extraConfigurationNode = doc1.createElement("extraConfiguration");
				extraConfigurationNode.setTextContent(extraConfigurationMarshalledString);
				notificationNode.appendChild(extraConfigurationNode);
				
				String senderName = alertNotification.getSenderName();
				Node senderNode = doc1.createElement("sender");
				senderNode.setTextContent(senderName);
				senderNode.appendChild(extraConfigurationNode);
			}

			// dampening
			AlertDampening alertDampening = alertDefinition.getAlertDampening();
			Node dampeningNode = doc1.createElement("dampening");
			alertDefinitionNode.appendChild(dampeningNode);

			String dampeningCategory = alertDampening.getCategory().name();
			Node dampeningCategoryNode = doc1.createElement(DAMPENING_CATEGORY);
			dampeningCategoryNode.setTextContent(dampeningCategory);
			alertDefinitionNode.appendChild(dampeningCategoryNode);

//			alertDampening.getPeriod();
//			alertDampening.getPeriodUnits();
//			alertDampening.getValue();
//			alertDampening.getValueUnits();
			// alertDefinition.getAlertDampeningEvents();
			
			Integer recoveryId = alertDefinition.getRecoveryId();
			Node recoveryNode = doc1.createElement(RECOVERY_ID);
			recoveryNode.setTextContent(String.valueOf(recoveryId));
			alertDefinitionNode.appendChild(recoveryNode);

			sw.close();
			
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

			StringWriter swb = new StringWriter();
			StreamResult result = new StreamResult(swb);
			DOMSource source = new DOMSource(doc1);
			transformer.transform(source, result);

			String xmlString = swb.toString();
			swb.close();

			// write to file
			File file = new File("zdgfsdkjf");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			bw.write(xmlString);
			bw.flush();
			bw.close();
		}
	}

	private AlertDefinition buildJavaObjectFromXml(Element alertElement) {
		AlertDefinition alertDefinition = new AlertDefinition();
		
		String alertName = alertElement.getElementsByTagName(ALERT_NAME).item(0).getTextContent();;
		alertDefinition.setName(alertName);
		
		String alertDescription = alertElement.getElementsByTagName(ALERT_DESCRIPTION).item(0).getTextContent();
		alertDefinition.setDescription(alertDescription);
		
		Boolean enabled = Boolean.valueOf(alertElement.getElementsByTagName(ENABLED).item(0).getTextContent());
		alertDefinition.setEnabled(enabled); 
		
		String alertPriority = alertElement.getElementsByTagName(ALERT_PRIORITY).item(0).getTextContent();;
		AlertPriority priority = AlertPriority.valueOf(alertPriority);
		alertDefinition.setPriority(priority);

		String conditionExpressionString = alertElement.getElementsByTagName(CONDITION_EXPRESSION).item(0).getTextContent();
		BooleanExpression conditionExpression = BooleanExpression.valueOf(conditionExpressionString);
		alertDefinition.setConditionExpression(conditionExpression);
		
		String categoryString = alertElement.getElementsByTagName(DAMPENING_CATEGORY).item(0).getTextContent();
		Category category = Category.valueOf(categoryString);
		AlertDampening alertDampening = new AlertDampening(category);
		alertDefinition.setAlertDampening(alertDampening);
		
		Integer recoveryId = Integer.parseInt(alertElement.getElementsByTagName(RECOVERY_ID).item(0).getTextContent());
		alertDefinition.setRecoveryId(recoveryId);
		
//						alertDefinition.setConditions(null);
//						alertDefinition.setConditionExpression(null);
//						alertDefinition.setAlertNotifications(null);
//						alertDefinition.setAlertDampening(null);
//						alertDefinition.setAlertDampeningEvents();

		return alertDefinition;
	}

	public void doImportTry() {
		/*			
		// read file
		try {
			File file = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			NodeList alertsNodeList = doc.getElementsByTagName(ALERT_DEFINITION);
			
			for (int i=0; i<alertsNodeList.getLength(); i++) {
				Node alertNode = alertsNodeList.item(i);
				Element alertElement = (Element) alertNode;
				
				String resourceName = alertElement.getElementsByTagName(RESOURCE_NAME).item(0).getTextContent();
				String resourceType = alertElement.getElementsByTagName(RESOURCE_TYPE).item(0).getTextContent();

				// find the resource to create the alert def on it
				ResourceCriteria criteria = new ResourceCriteria();
				criteria.addFilterName(resourceName);
				criteria.addFilterResourceTypeName(resourceType);
				PageList<Resource> resources = LookupUtil.getResourceManager().findResourcesByCriteria(subject, criteria);

				// theoretically there may be more resources with the same name and the same type !!! oare ?
				for (int j = 0; j < resources.size(); j++) {
					Integer resourceId = resources.get(j).getId();
					
					AlertDefinition alertDefinition = buildJavaObjectFromXml(alertElement);

					//TODO what happens if an alert already exists ? is it possible ?
					alertDefinitionManager.createAlertDefinitionInNewTransaction(subject, alertDefinition, resourceId, false);
				}
			}
			resultValue = "OK";
		} catch (Exception e) {
			e.printStackTrace();
			resultValue = e.getMessage();
		}
*/			
		
	}
	
}
