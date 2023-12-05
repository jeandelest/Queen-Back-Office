package fr.insee.queen.api.campaign.controller.dto.input;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.queen.api.campaign.service.model.QuestionnaireModel;
import fr.insee.queen.api.web.validation.IdValid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Questionnaire data used to create questionnaire
 * @param idQuestionnaireModel questionnaire id
 * @param label questionnaire label
 * @param value json data structure of the questionnaire
 * @param requiredNomenclatureIds nomenclature ids linked to the questionnaire
 */
public record QuestionnaireModelCreationData(

        @IdValid
        String idQuestionnaireModel,
        @NotEmpty
        String label,
        @NotNull
        ObjectNode value,
        Set<String> requiredNomenclatureIds) {

    public static QuestionnaireModel toModel(QuestionnaireModelCreationData questionnaire) {
        Set<String> nomenclatureIds = questionnaire.requiredNomenclatureIds();
        if (nomenclatureIds == null) {
            nomenclatureIds = new HashSet<>();
        }

        return QuestionnaireModel.createQuestionnaireWithoutCampaign(
                questionnaire.idQuestionnaireModel,
                questionnaire.label,
                questionnaire.value.toString(),
                nomenclatureIds);
    }
}