package fr.insee.queen.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
import fr.insee.queen.api.configuration.cache.CacheName;
import fr.insee.queen.api.dto.integration.IntegrationResultDto;
import fr.insee.queen.api.dto.integration.IntegrationResultUnitDto;
import fr.insee.queen.api.dto.input.CampaignInputDto;
import fr.insee.queen.api.dto.input.MetadataInputDto;
import fr.insee.queen.api.dto.input.NomenclatureInputDto;
import fr.insee.queen.api.dto.input.QuestionnaireModelInputDto;
import fr.insee.queen.api.dto.IntegrationStatus;
import fr.insee.queen.api.service.exception.IntegrationServiceException;
import fr.insee.queen.api.service.campaign.CampaignExistenceService;
import fr.insee.queen.api.service.campaign.CampaignService;
import fr.insee.queen.api.service.questionnaire.NomenclatureService;
import fr.insee.queen.api.service.questionnaire.QuestionnaireModelExistenceService;
import fr.insee.queen.api.service.questionnaire.QuestionnaireModelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.XML;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
@AllArgsConstructor
public class IntegrationService {

	private final CampaignService campaignService;
	private final CampaignExistenceService campaignExistenceService;
	private final QuestionnaireModelExistenceService questionnaireModelExistenceService;
	private final QuestionnaireModelService questionnaireModelService;
	private final NomenclatureService nomenclatureService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CacheManager cacheManager;

	private static final String CAMPAIGN_XML = "campaign.xml";
	private static final String NOMENCLATURES_XML = "nomenclatures.xml";
	private static final String QUESTIONNAIREMODELS_XML = "questionnaireModels.xml";
	private static final String LABEL = "Label";
	private static final String METADATA = "Metadata";
	private static final String ID = "Id";
	private static final String FILENAME = "FileName";
	private static final String CAMPAIGN_ID = "CampaignId";
	private static final String NOMENCLATURE = "Nomenclature";

	@Transactional
	public IntegrationResultDto integrateContext(MultipartFile file) {
		try {
			File zip = File.createTempFile(UUID.randomUUID().toString(), "temp");
			return integrateContext(zip, file);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new IntegrationServiceException(e.getMessage());
		}
	}

	private IntegrationResultDto integrateContext(File zip, MultipartFile file) throws IOException {
		try (FileOutputStream o = new FileOutputStream(zip)) {
			IOUtils.copy(file.getInputStream(), o);
			return doIntegration(zip);
		} catch (ParserConfigurationException | SAXException | JSONException | XPathExpressionException e) {
			log.error(e.getMessage(), e);
			throw new IntegrationServiceException(e.getMessage());
		}
	}

	private IntegrationResultDto doIntegration(File zip) throws ParserConfigurationException, SAXException, XPathExpressionException, JSONException {
		IntegrationResultDto result = new IntegrationResultDto();
		ZipEntry campaignXmlFile = null;
		ZipEntry nomenclaturesXmlFile =  null;
		ZipEntry questionnaireModelsXmlFile = null;
		HashMap<String, ZipEntry> nomenclatureJsonFiles = new HashMap<>();
		HashMap<String, ZipEntry> questionnaireModelJsonFiles = new HashMap<>();

		try(ZipFile zf = new ZipFile(zip)){
			String nomenclaturesPattern = "nomenclatures/.*json";
			String questionnairesPattern = "questionnaireModels/.*json";
			Enumeration<? extends ZipEntry> e = zf.entries();

			while(e.hasMoreElements()){
				ZipEntry entry = e.nextElement();
				switch(entry.getName()) {
					case CAMPAIGN_XML -> campaignXmlFile = entry;
					case NOMENCLATURES_XML -> nomenclaturesXmlFile = entry;
					case QUESTIONNAIREMODELS_XML -> questionnaireModelsXmlFile = entry;
					default -> {
						if(Pattern.matches(nomenclaturesPattern,entry.getName())){
							nomenclatureJsonFiles.put(entry.getName(), entry);

						}
						if(Pattern.matches(questionnairesPattern,entry.getName())){
							questionnaireModelJsonFiles.put(entry.getName(), entry);
						}
					}
				}
			}

			// Nomenclatures process
			validateAndProcessNomenclatures(zf, nomenclaturesXmlFile, nomenclatureJsonFiles, result);

			// Campaign process
			validateAndProcessCampaign(zf, campaignXmlFile, result);

			// Questionnaire models process
			validateAndProcessQuestionnaireModels(zf, questionnaireModelsXmlFile, questionnaireModelJsonFiles, result);


			return result;
		}
		catch(IOException e) {
			return null;
		}
	}

	private void validateAndProcessNomenclatures(ZipFile zf, ZipEntry nomenclaturesXmlFile,
												 HashMap<String, ZipEntry> nomenclatureJsonFiles, IntegrationResultDto result) throws ParserConfigurationException, SAXException, IOException {
		if(nomenclaturesXmlFile == null) {
			result.nomenclatures(new ArrayList<>());
			result.nomenclatures().add(new IntegrationResultUnitDto(
					NOMENCLATURES_XML,
					IntegrationStatus.ERROR,
					"No file nomenclatures.xml found"));
			return;
		}

		boolean validation = false;
		try {
			validation = validateAgainstSchema(zf.getInputStream(nomenclaturesXmlFile), "nomenclatures_integration_template.xsd");
		}
		catch(Exception ex) {
			result.nomenclatures(new ArrayList<>());
			result.nomenclatures().add(new IntegrationResultUnitDto(
					NOMENCLATURES_XML,
					IntegrationStatus.ERROR,
					"File nomenclatures.xml does not fit the required template (" + ex.getMessage() + ")"));
		}
		if(validation) {
			processNomenclatures(zf, nomenclaturesXmlFile, nomenclatureJsonFiles, result);
		}
	}

	private void validateAndProcessCampaign(ZipFile zf, ZipEntry campaignXmlFile,
											IntegrationResultDto result) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, JSONException {
		if(campaignXmlFile == null) {
			result.campaign(new IntegrationResultUnitDto(CAMPAIGN_XML,
					IntegrationStatus.ERROR,
					"No file campaign.xml found"));
			return;
		}

		boolean validation = false;
		try {
			validation = validateAgainstSchema(zf.getInputStream(campaignXmlFile), "campaign_integration_template.xsd");
		}
		catch(Exception ex) {
			result.campaign(new IntegrationResultUnitDto(CAMPAIGN_XML,
					IntegrationStatus.ERROR,
					"File campaign.xml does not fit the required template (" + ex.getMessage() + ")"));
		}
		if(validation) {
			processCampaign(zf, campaignXmlFile, result);
		}
	}

	private void validateAndProcessQuestionnaireModels(ZipFile zf, ZipEntry questionnaireModelsXmlFile,
													   HashMap<String, ZipEntry> questionnaireModelJsonFiles, IntegrationResultDto result) throws ParserConfigurationException, SAXException, IOException {
		if(questionnaireModelsXmlFile == null) {
			result.questionnaireModels(new ArrayList<>());
			result.questionnaireModels().add(new IntegrationResultUnitDto(
					QUESTIONNAIREMODELS_XML,
					IntegrationStatus.ERROR,
					"No file questionnaireModels.xml found"));
			return;
		}

		boolean validation = false;
		try {
			validation = validateAgainstSchema(zf.getInputStream(questionnaireModelsXmlFile), "questionnaireModels_integration_template.xsd");
		}
		catch(Exception ex) {
			result.questionnaireModels(new ArrayList<>());
			result.questionnaireModels().add(new IntegrationResultUnitDto(
					QUESTIONNAIREMODELS_XML,
					IntegrationStatus.ERROR,
					"File questionnaireModels.xml does not fit the required template (" + ex.getMessage() + ")"));
		}
		if(validation) {
			processQuestionnaireModels(zf, questionnaireModelsXmlFile, questionnaireModelJsonFiles, result);
		}
	}


	private boolean validateAgainstSchema(InputStream xmlStream, String schemaFileName) throws SAXException, IOException {
		InputStream templateStream = getClass().getClassLoader().getResourceAsStream("templates//" + schemaFileName);
		SchemaFactory facto = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		facto.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		facto.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		Source schemaSource = new StreamSource(templateStream);
		Schema schema = facto.newSchema(schemaSource);
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(xmlStream));
		return true;
	}

	private void processCampaign(ZipFile zf, ZipEntry campaignXmlFile, IntegrationResultDto result) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, JSONException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(zf.getInputStream(campaignXmlFile));

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("/Campaign/Id/text()");

		String id = expr.evaluate(doc, XPathConstants.STRING).toString().toUpperCase();

		NodeList metadataTags = doc.getElementsByTagName(METADATA);
		NodeList labelTags = doc.getElementsByTagName(LABEL);

		MetadataInputDto metadataInputDto = new MetadataInputDto(objectMapper.createObjectNode());
		if(metadataTags.getLength() > 0) {
			log.info("Setting metadata for campaign {}", id);
			metadataInputDto = new MetadataInputDto(convertMetadataValueToObjectNode(metadataTags.item(0)));
		}

		String label = "";
		if(labelTags.getLength() > 0) {
			log.info("Setting label for campaign {}", id);
			label = labelTags.item(0).getTextContent();
		}

		IntegrationStatus status;
		CampaignInputDto campaign = new CampaignInputDto(id, label, new HashSet<>(), metadataInputDto);
		if(campaignExistenceService.existsById(id)) {
			log.info("Updating campaign {}", id);
			campaignService.updateCampaign(campaign);
			status = IntegrationStatus.UPDATED;
		}
		else {
			log.info("Creating campaign {}", id);
			campaignService.createCampaign(campaign);
			status = IntegrationStatus.CREATED;
		}

		Objects.requireNonNull(cacheManager.getCache(CacheName.CAMPAIGN_NOMENCLATURES))
				.evict(campaign.id());
		Objects.requireNonNull(cacheManager.getCache(CacheName.METADATA_BY_QUESTIONNAIRE))
				.evict(campaign.id());
		Objects.requireNonNull(cacheManager.getCache(CacheName.QUESTIONNAIRE_EXIST)).clear();
		Objects.requireNonNull(cacheManager.getCache(CacheName.QUESTIONNAIRE)).clear();
		Objects.requireNonNull(cacheManager.getCache(CacheName.QUESTIONNAIRE_NOMENCLATURES)).clear();
		Objects.requireNonNull(cacheManager.getCache(CacheName.METADATA_BY_QUESTIONNAIRE)).clear();
		result.campaign(new IntegrationResultUnitDto(id, status, null));
	}

	public ObjectNode convertMetadataValueToObjectNode(Node xmlNode) throws JsonProcessingException, JSONException {
		ObjectMapper mapper = new ObjectMapper();

		String jsonString = XML.toJSONObject(toString(xmlNode, true, true)).toString();
		log.error(jsonString);
		return mapper.readValue(jsonString, ObjectNode.class);
	}

	public JsonNode removeArrayLevel(JsonNode node, ObjectMapper mapper) {

		if(node == null || node.isValueNode()) {
			return node;
		}
		if(node.isArray()) {
			ArrayNode arrNode = mapper.createArrayNode();
			for (int i = 0; i < node.size(); i++) {
				arrNode.add(removeArrayLevel(node.get(i),mapper));
			}
			return arrNode;
		}
		if(node.isObject()) {
			if(Iterators.size(node.elements()) == 1 && node.elements().next().isArray()) {
				String keyName = node.fieldNames().next();
				return removeArrayLevel(node.get(keyName), mapper);
			}
			else {
				ObjectNode objNode = mapper.createObjectNode();
				Iterator<Entry<String, JsonNode>> it = node.fields();
				while(it.hasNext()) {
					Entry<String, JsonNode> e = it.next();
					objNode.set(e.getKey(), removeArrayLevel(e.getValue(), mapper));
				}
				return objNode;
			}
		}
		return node;
	}

	private void processNomenclatures(ZipFile zf, ZipEntry nomenclaturesXmlFile,
									  HashMap<String, ZipEntry> nomenclatureJsonFiles, IntegrationResultDto result) throws ParserConfigurationException, SAXException, IOException {
		List<IntegrationResultUnitDto> results = new ArrayList<>();
		result.nomenclatures(results);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(zf.getInputStream(nomenclaturesXmlFile));
		NodeList nomenclatureNodes = doc.getElementsByTagName("Nomenclatures").item(0).getChildNodes();
		for (int i = 0; i < nomenclatureNodes.getLength(); i++) {
			if(nomenclatureNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
				Element nomenclature = (Element) nomenclatureNodes.item(i);
				processNomenclature(zf, nomenclature, nomenclatureJsonFiles, results);
			}

		}
	}

	private void processNomenclature(ZipFile zf, Element nomenclature,
									 HashMap<String, ZipEntry> nomenclatureJsonFiles,
									 List<IntegrationResultUnitDto> results) {
		String nomenclatureId = nomenclature.getElementsByTagName(ID).item(0).getTextContent();
		String nomenclatureLabel = nomenclature.getElementsByTagName(LABEL).item(0).getTextContent();
		String nomenclatureFilename = nomenclature.getElementsByTagName(FILENAME).item(0).getTextContent();

		if(nomenclatureService.existsById(nomenclatureId)) {
			log.info("Nomenclature {} already exists", nomenclatureId);
			results.add(new IntegrationResultUnitDto(
					nomenclatureId,
					IntegrationStatus.ERROR,
					"A nomenclature with this id already exists")
			);
			return;
		}

		ZipEntry nomenclatureValueEntry = nomenclatureJsonFiles.get("nomenclatures/" +nomenclatureFilename);
		if(nomenclatureValueEntry == null) {
			log.info("Nomenclature file {} could not be found in input zip", nomenclatureFilename );
			results.add(new IntegrationResultUnitDto(
					nomenclatureId,
					IntegrationStatus.ERROR,
					"Nomenclature file '" + nomenclatureFilename + "' could not be found in input zip")
			);
			return;
		}

		ArrayNode nomenclatureValue;
		try {
			nomenclatureValue = objectMapper.readValue(zf.getInputStream(nomenclatureValueEntry), ArrayNode.class);
			NomenclatureInputDto nomenclatureToSave = new NomenclatureInputDto(nomenclatureId, nomenclatureLabel, nomenclatureValue);

			log.info("Creating nomenclature {}", nomenclatureId);
			results.add(new IntegrationResultUnitDto(
					nomenclatureId,
					IntegrationStatus.CREATED,
					null));
			nomenclatureService.saveNomenclature(nomenclatureToSave);
		} catch (IOException e) {
			log.info("Could not parse json in file {}", nomenclatureFilename);
			results.add(new IntegrationResultUnitDto(
					nomenclatureId,
					IntegrationStatus.ERROR,
					"Could not parse json in file '" + nomenclatureFilename + "'")
			);
		}
	}

	private void processQuestionnaireModels(ZipFile zf, ZipEntry questionnaireModelsXmlFile,
											HashMap<String, ZipEntry> questionnaireModelJsonFiles, IntegrationResultDto result) throws ParserConfigurationException, SAXException, IOException {
		List<IntegrationResultUnitDto> results = new ArrayList<>();
		result.questionnaireModels(results);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(zf.getInputStream(questionnaireModelsXmlFile));
		NodeList qmNodes = doc.getElementsByTagName("QuestionnaireModels").item(0).getChildNodes();
		for (int i = 0; i < qmNodes.getLength(); i++) {
			if(qmNodes.item(i).getNodeType() == Node.ELEMENT_NODE){
				Element qm = (Element) qmNodes.item(i);
				processQuestionnaireModel(zf, qm, results, questionnaireModelJsonFiles);
			}
		}
	}

	private void processQuestionnaireModel(ZipFile zf, Element qm,
										   List<IntegrationResultUnitDto> results,
										   HashMap<String, ZipEntry> questionnaireModelJsonFiles) {
		String qmId = qm.getElementsByTagName(ID).item(0).getTextContent();
		String qmLabel = qm.getElementsByTagName(LABEL).item(0).getTextContent();
		String qmFilename = qm.getElementsByTagName(FILENAME).item(0).getTextContent();
		String campaignId = qm.getElementsByTagName(CAMPAIGN_ID).item(0).getTextContent();
		NodeList requiredNomNodes = qm.getElementsByTagName(NOMENCLATURE);
		List<String> requiredNomenclatureIds = IntStream.range(0, requiredNomNodes.getLength())
				.filter(j-> requiredNomNodes.item(j).getNodeType() == Node.ELEMENT_NODE)
				.mapToObj(j -> requiredNomNodes.item(j).getTextContent())
				.toList();
		// Checking if campaign exists
		if(!campaignExistenceService.existsById(campaignId)) {
			log.info("Could not create Questionnaire model {}, campaign {} does not exist", qmId, campaignId);
			results.add(new IntegrationResultUnitDto(
					qmId,
					IntegrationStatus.ERROR,
					"The campaign '" + campaignId + "' does not exist")
			);
			return;
		}

		// Checking if required nomenclatures exist
		for(String id : requiredNomenclatureIds) {
			if(!nomenclatureService.existsById(id)) {
				log.info("Could not create Questionnaire model {}, nomenclature {} does not exist", qmId, id);
				results.add(new IntegrationResultUnitDto(
						qmId,
						IntegrationStatus.ERROR,
						"The nomenclature '" + id + "' does not exist")
				);
				return;
			}
		}

		ZipEntry qmValueEntry = questionnaireModelJsonFiles.get("questionnaireModels/" +qmFilename);
		if(qmValueEntry == null) {
			log.info("Questionnaire model file {} could not be found in input zip", qmFilename);
			results.add(new IntegrationResultUnitDto(
					qmId,
					IntegrationStatus.ERROR,
					"Questionnaire model file '" + qmFilename + "' could not be found in input zip")
			);
			return;
		}

		ObjectNode qmValue;
		try {
			qmValue = objectMapper.readValue(zf.getInputStream(qmValueEntry), ObjectNode.class);
			QuestionnaireModelInputDto questionnaireModel = new QuestionnaireModelInputDto(qmId, qmLabel, qmValue, new HashSet<>(requiredNomenclatureIds));

			IntegrationStatus status;
			if(questionnaireModelExistenceService.existsById(qmId)) {
				log.info("QuestionnaireModel {} already exists", qmId);
				questionnaireModelService.updateQuestionnaire(questionnaireModel, campaignId);
				status = IntegrationStatus.UPDATED;
			}
			else {
				log.info("Creating questionnaire model {}", qmId);
				questionnaireModelService.createQuestionnaire(questionnaireModel, campaignId);
				status = IntegrationStatus.CREATED;
			}

			results.add(new IntegrationResultUnitDto(
					qmId,
					status,
					null)
			);

			Objects.requireNonNull(cacheManager.getCache(CacheName.QUESTIONNAIRE))
					.evict(qmId);
			Objects.requireNonNull(cacheManager.getCache(CacheName.QUESTIONNAIRE_NOMENCLATURES))
					.evict(qmId);
			Objects.requireNonNull(cacheManager.getCache(CacheName.METADATA_BY_QUESTIONNAIRE))
					.evict(qmId);
		} catch (IOException e) {
			log.info("Could not parse json in file {}", qmFilename);
			results.add(new IntegrationResultUnitDto(
					qmId,
					IntegrationStatus.ERROR,
					"Could not parse json in file '" + qmFilename + "'")
			);
		}
	}

	public static String toString(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
		if (node == null) {
			throw new IllegalArgumentException("node is null.");
		}
		try {
			// Remove unwanted whitespaces
			node.normalize();
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
			NodeList nodeList = (NodeList)expr.evaluate(node, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); ++i) {
				Node nd = nodeList.item(i);
				nd.getParentNode().removeChild(nd);
			}

			// Create and setup transformer
			System.setProperty("javax.xml.transform.TransformerFactory",
					"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer =  tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			if (omitXmlDeclaration) {
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

			if (prettyPrint) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			}

			// Turn the node into a string
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerException | XPathExpressionException e) {
			return null;
		}
	}
}
