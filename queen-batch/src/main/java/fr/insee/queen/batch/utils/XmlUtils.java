package fr.insee.queen.batch.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.lunatic.conversion.data.XMLLunaticDataToJSON;
import fr.insee.queen.batch.exception.BatchException;
import fr.insee.queen.batch.exception.DataIntegrityException;
import fr.insee.queen.domain.campaign.model.CampaignSummary;
import fr.insee.queen.domain.campaign.service.CampaignService;
import fr.insee.queen.domain.common.exception.EntityNotFoundException;
import fr.insee.queen.domain.surveyunit.model.SurveyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Campaign on XML Content - getXmlNodeFile - validateXMLSchema -
 * xmlToCampaign - xmlToQuestionnaireModel - xmlToNomenclatures
 *
 * @author Claudel Benjamin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class XmlUtils {
    private final CampaignService campaignService;
    private final String tempFolder;

    /**
     * get an XML node in an XML File
     *
     * @param filename filename reference
     * @param nodeName nodeName to search
     * @return the XML node find
     */
    public static NodeList getXmlNodeFile(String filename, String nodeName) {
        try(FileInputStream fis = new FileInputStream(filename)) {
            // an instance of factory that gives a document builder
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fis);
            doc.getDocumentElement().normalize();
            return doc.getElementsByTagName(nodeName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * get campaign in xml file
     *
     * @param fileName the file name
     * @return {@link CampaignSummary} campaign summary
     */
    public CampaignSummary xmlToCampaign(String fileName) {
        NodeList lstNodeCampaign = getXmlNodeFile(fileName, "Campaign");
        if (lstNodeCampaign == null || lstNodeCampaign.getLength() != 1) {
            log.error("Log error => need to have exactly one campaign in file");
            return null;
        }

        Node nodeCampaign = lstNodeCampaign.item(0);
        if (nodeCampaign.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        Element campaignElement = (Element) nodeCampaign;
        String campaignId = campaignElement.getElementsByTagName("Id").item(0).getTextContent();
        CampaignSummary campaign;
        try {
            campaign = campaignService.getCampaignSummary(campaignId);
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            return null;
        }
        return campaign;
    }

    /**
     * get personalization json from xml
     *
     * @param personalization xml node to convert
     * @return the json personalization node
     */
    public ArrayNode getJsonPersonalization(Node personalization) {
        ArrayNode personalizationJson = JsonNodeFactory.instance.arrayNode();

        if(personalization == null) {
            return null;
        }

        if (personalization.getChildNodes().getLength() == 0) {
            return null;
        }

        if (personalization.getChildNodes().getLength() == 1 &&
                personalization.getChildNodes().item(0).getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        for (int i = 0; i < personalization.getChildNodes().getLength(); i++) {
            Node node = personalization.getChildNodes().item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("Variable")) {
                ObjectNode jsonTemp = JsonNodeFactory.instance.objectNode();
                Element e = (Element) node;
                jsonTemp.put("name", e.getElementsByTagName("Name").item(0).getTextContent());
                jsonTemp.put("value", e.getElementsByTagName("Value").item(0).getTextContent());
                personalizationJson.add(jsonTemp);
            }
        }
        return personalizationJson;
    }

    /**
     * Transform xml to data from SurveyUnit Element
     *
     * @param surveyUnit survey unit xml element
     * @return survey unit in json format
     * @throws BatchException batch exception
     */
    public ObjectNode getJsonData(Element surveyUnit) throws Exception {
        if (surveyUnit.getElementsByTagName("Data").item(0) == null) {
            throw new BatchException("Surevy unit with id : " + surveyUnit.getElementsByTagName("Id").item(0).getTextContent() + " does not have data");
        }
        return dataXmlToJSON(surveyUnit.getElementsByTagName("Data").item(0));
    }

    /**
     * transform a data xml node to a data json node
     *
     * @param data xml data node
     * @return @{@link ObjectNode} data json node
     * @throws Exception exception
     */
    public ObjectNode dataXmlToJSON(Node data) throws Exception {
        ObjectNode dataJsonObject = JsonNodeFactory.instance.objectNode();

        if(data == null) {
            return dataJsonObject;
        }

        if (data.getChildNodes().getLength() == 0) {
            return dataJsonObject;
        }

        if (data.getChildNodes().getLength() == 1 &&
                data.getChildNodes().item(0).getNodeType() != Node.ELEMENT_NODE) {
            return dataJsonObject;
        }

        Document dataXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        XMLLunaticDataToJSON xmlLunaticDataToJSON = new XMLLunaticDataToJSON();
        File fileDataXml = Files.createTempFile(Path.of(tempFolder), "tempFileData", ".xml").toFile();
        Node copyNode = dataXml.importNode(data, true);
        dataXml.appendChild(copyNode);
        transformer.transform(new DOMSource(dataXml), new StreamResult(fileDataXml));
        File dataJsonFile = xmlLunaticDataToJSON.transform(fileDataXml);

        try {
            Files.delete(Paths.get(fileDataXml.getAbsolutePath()));
        } catch (IOException e) {
            log.warn("Unable to delete tempFileData.xml temporary file : {}", e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        dataJsonObject = mapper.readValue(dataJsonFile, ObjectNode.class);
        try {
            Files.delete(Paths.get(dataJsonFile.getAbsolutePath()));
        } catch (IOException e) {
            log.warn("Unable to delete xml2json temporary file : {}", e.getMessage());
        }
        return dataJsonObject;
    }

    /***
     * This method crates a Sample object from a file.
     * @param fileName file name
     * @return Sample object
     * @throws Exception
     */
    public Sample createSample(String fileName) throws Exception {
        CampaignSummary campaign = xmlToCampaign(fileName);
        List<SurveyUnit> surveyUnits = xmlToSurveyUnits(fileName, campaign);
        return new Sample(fileName, campaign, surveyUnits);
    }

    /**
     * get survey unit in xml file
     *
     * @param fileName the file name
     * @return survey unit
     * @throws Exception exception
     */
    public List<SurveyUnit> xmlToSurveyUnits(String fileName, CampaignSummary campaign) throws Exception {
        List<SurveyUnit> surveyUnits = new ArrayList<>();
        List<String> surveyUnitsIds = new ArrayList<>();
        Set<String> questionnaireIds = campaign.getQuestionnaireIds();
        NodeList lstNodeSurveyUnit = getXmlNodeFile(fileName, "SurveyUnit");
        if (lstNodeSurveyUnit == null) {
            return new ArrayList<>();
        }

        for (int itru = 0; itru < lstNodeSurveyUnit.getLength(); itru++) {
            Node nodeSurveyUnit = lstNodeSurveyUnit.item(itru);
            if (nodeSurveyUnit.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element surveyUnit = (Element) nodeSurveyUnit;
            if (surveyUnit.getElementsByTagName("Id").item(0).getTextContent() == null) {
                continue;
            }

            String surveyUnitId = surveyUnit.getElementsByTagName("Id").item(0).getTextContent();
            if (surveyUnitsIds.contains(surveyUnitId)) {
                throw new BatchException(String.format("Survey unit with id : %s is duplicated", surveyUnitId));
            }

            String questionnaireSurveyUnitId = surveyUnit.getElementsByTagName("QuestionnaireModelId").item(0).getTextContent();
            if(!questionnaireIds.contains(questionnaireSurveyUnitId)) {
                throw new DataIntegrityException(String.format("Error on find questionnaire by id %s : questionnaire not in DB", questionnaireSurveyUnitId));
            }

            String personalizationJsonString = null;
            Node personalizationXmlNode = surveyUnit.getElementsByTagName("Personalization").item(0);
            ArrayNode personalizationJsonNode = getJsonPersonalization(personalizationXmlNode);
            ObjectNode commentJsonNode = JsonNodeFactory.instance.objectNode();
            ObjectNode dataJsonNode = getJsonData(surveyUnit);

            if(personalizationJsonNode.isEmpty()) {
                personalizationJsonString = personalizationJsonNode.toString();
            }

            SurveyUnit su = new SurveyUnit(surveyUnitId,
                    campaign.getId(),
                    questionnaireSurveyUnitId,
                    personalizationJsonString,
                    dataJsonNode.toString(),
                    commentJsonNode.toString(),
                    null);
            surveyUnits.add(su);
            surveyUnitsIds.add(surveyUnitId);
        }

        return surveyUnits;
    }

}
