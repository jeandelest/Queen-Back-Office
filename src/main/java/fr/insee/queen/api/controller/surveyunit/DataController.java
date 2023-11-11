package fr.insee.queen.api.controller.surveyunit;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.queen.api.configuration.auth.AuthorityRole;
import fr.insee.queen.api.controller.utils.HabilitationComponent;
import fr.insee.queen.api.controller.validation.IdValid;
import fr.insee.queen.api.service.pilotage.PilotageRole;
import fr.insee.queen.api.service.surveyunit.DataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * These endpoints handle the questionnaire form data of a survey unit
 */
@RestController
@Tag(name = "06. Survey units")
@RequestMapping(path = "/api")
@Slf4j
@AllArgsConstructor
@Validated
public class DataController {
	private final DataService dataService;
	private final HabilitationComponent habilitationComponent;
	
	/**
	* Retrieve the questionnaire form data of a survey unit
	* 
	* @param surveyUnitId the id of reporting unit
	* @param auth authentication object
	* @return {@link String} the questionnaire form data of a survey unit
	*/
	@Operation(summary = "Get data for a survey unit")
	@GetMapping(path = "/survey-unit/{id}/data")
	@PreAuthorize(AuthorityRole.HAS_ANY_ROLE)
	public String getDataBySurveyUnit(@IdValid @PathVariable(value = "id") String surveyUnitId,
									  Authentication auth) {
		log.info("GET Data for reporting unit with id {}", surveyUnitId);
		habilitationComponent.checkHabilitations(auth, surveyUnitId, PilotageRole.INTERVIEWER);
		return dataService.getData(surveyUnitId);
	}

	
	/**
	* Update the questionnaire form data of a survey unit
	* 
	* @param dataValue the questionnaire form data to update
	* @param surveyUnitId the id of the survey unit
	* @param auth authentication object
	*/
	@Operation(summary = "Update data for a survey unit")
	@PutMapping(path = "/survey-unit/{id}/data")
	@PreAuthorize(AuthorityRole.HAS_ANY_ROLE)
	public void updateData(@NotNull @RequestBody ObjectNode dataValue,
						@IdValid @PathVariable(value = "id") String surveyUnitId,
						Authentication auth) {
		log.info("PUT data for reporting unit with id {}", surveyUnitId);
		habilitationComponent.checkHabilitations(auth, surveyUnitId, PilotageRole.INTERVIEWER);
		dataService.updateData(surveyUnitId, dataValue);
	}
}
