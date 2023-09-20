package fr.insee.queen.api.controller;

import fr.insee.queen.api.configuration.auth.AuthorityRole;
import fr.insee.queen.api.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MetadataController is the Controller using to manage metadatas
 * entity
 * 
 * @author Corcaud Samuel
 * 
 */
@RestController
@RequestMapping(path = "/api")
@Slf4j
@AllArgsConstructor
public class MetadataController {

	private final MetadataService metadataService;
	
	/**
	* This method is using to get the metadata associated to a specific campaign 
	* 
	* @param campaignId the id of the campaign
	* @return the metadata associated to the reporting unit
	*/
	@Operation(summary = "Get metadata by campaign Id ")
	@GetMapping(path = "/campaign/{id}/metadata")
	@PreAuthorize(AuthorityRole.HAS_ANY_ROLE)
	public String getMetadataByCampaignId(@NotBlank @PathVariable(value = "id") String campaignId){
		log.info("GET metadata for campaign with id {}", campaignId);
		return metadataService.getMetadata(campaignId).value();
	}
	
	/**
	* This method is using to get the metadata associated to a specific questionnaire 
	* 
	* @param questionnaireId the id of the campaign
	* @return the metadata associated to the reporting unit
	*/
	@Operation(summary = "Get metadata by questionnaire Id ")
	@GetMapping(path = "/questionnaire/{id}/metadata")
	@PreAuthorize(AuthorityRole.HAS_ANY_ROLE)
	public String getMetadataByQuestionnaireId(@NotBlank @PathVariable(value = "id") String questionnaireId) {
		log.info("GET metadata for questionnaire with id {}", questionnaireId);
		return metadataService.getMetadataByQuestionnaireId(questionnaireId).value();
	}
}
