package fr.insee.queen.api.controller.surveyunit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.queen.api.configuration.auth.AuthorityRole;
import fr.insee.queen.api.controller.utils.HabilitationComponent;
import fr.insee.queen.api.service.exception.EntityNotFoundException;
import fr.insee.queen.api.service.pilotage.PilotageRole;
import fr.insee.queen.api.service.surveyunit.ParadataEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Handle creation of paradata events for a survey unit. Paradatas are data not linked with a questionnaire,
 * and give additional informations about how the user is filling the questionnaire
 */
@RestController
@Tag(name = "07. Paradata events", description = "Endpoints for paradata events")
@RequestMapping(path = "/api")
@Slf4j
@AllArgsConstructor
@Validated
public class ParadataEventController {
	private final ParadataEventService paradataEventService;
	private final HabilitationComponent habilitationComponent;

	/**
	 * Create a paradata event for a survey unit
	 *
	 * @param paradataValue paradata value
	 * @param auth authentication object
	 */
	@Operation(summary = "Create paradata event for a survey unit")
	@PostMapping(path = "/paradata")
	@PreAuthorize(AuthorityRole.HAS_ADMIN_PRIVILEGES + "||" + AuthorityRole.HAS_ROLE_INTERVIEWER)
	@ResponseStatus(HttpStatus.OK)
	public void addParadata(@NotNull @RequestBody ObjectNode paradataValue, Authentication auth) {
		String paradataSurveyUnitIdParameter = "idSU";
		log.info("POST ParadataEvent");
		if(!paradataValue.has(paradataSurveyUnitIdParameter)) {
			throw new EntityNotFoundException("Paradata does not contain the survey unit id");
		}

		JsonNode surveyUnitNode = paradataValue.get(paradataSurveyUnitIdParameter);
		if(!surveyUnitNode.isTextual() || surveyUnitNode.textValue() == null) {
			throw new EntityNotFoundException("Paradata does not contain the survey unit id");
		}

		String surveyUnitId = surveyUnitNode.textValue();
		habilitationComponent.checkHabilitations(auth, surveyUnitId, PilotageRole.INTERVIEWER);
		paradataEventService.createParadataEvent(surveyUnitId, paradataValue.toString());
	}
}
