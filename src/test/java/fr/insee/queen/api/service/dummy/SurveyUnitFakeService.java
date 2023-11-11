package fr.insee.queen.api.service.dummy;

import fr.insee.queen.api.dto.depositproof.PdfDepositProof;
import fr.insee.queen.api.dto.input.SurveyUnitCreateInputDto;
import fr.insee.queen.api.dto.input.SurveyUnitUpdateInputDto;
import fr.insee.queen.api.dto.surveyunit.*;
import fr.insee.queen.api.service.surveyunit.SurveyUnitService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class SurveyUnitFakeService implements SurveyUnitService {

    public static final String SURVEY_UNIT1_ID = "survey-unit1";

    @Override
    public boolean existsById(String surveyUnitId) {
        return false;
    }

    @Override
    public void throwExceptionIfSurveyUnitNotExist(String surveyUnitId) {

    }

    @Override
    public SurveyUnitDto getSurveyUnit(String id) {
        return null;
    }

    @Override
    public List<SurveyUnitSummaryDto> findByCampaignId(String campaignId) {
        return List.of(
                new SurveyUnitSummaryDto(SURVEY_UNIT1_ID, "questionnaire-id"),
                new SurveyUnitSummaryDto("survey-unit2", "questionnaire-id"),
                new SurveyUnitSummaryDto("survey-unit3", "questionnaire-id")
        );
    }

    @Override
    public List<String> findAllSurveyUnitIds() {
        return null;
    }

    @Override
    public void updateSurveyUnit(String surveyUnitId, SurveyUnitUpdateInputDto surveyUnit) {

    }

    @Override
    public PdfDepositProof generateDepositProof(String userId, String surveyUnitId) {
        return null;
    }

    @Override
    public void createSurveyUnit(String campaignId, SurveyUnitCreateInputDto surveyUnit) {

    }

    @Override
    public List<SurveyUnitSummaryDto> findSummaryByIds(List<String> surveyUnits) {
        return null;
    }

    @Override
    public Optional<SurveyUnitSummaryDto> findSummaryById(String surveyUnitId) {
        SurveyUnitSummaryDto surveyUnit = new SurveyUnitSummaryDto(surveyUnitId, "questionnaire-id");
        return Optional.of(surveyUnit);
    }

    @Override
    public List<SurveyUnitWithStateDto> findWithStateByIds(List<String> surveyUnits) {
        return null;
    }

    @Override
    public void delete(String surveyUnitId) {

    }

    @Override
    public SurveyUnitDepositProofDto getSurveyUnitDepositProof(String surveyUnitId) {
        return null;
    }

    @Override
    public SurveyUnitHabilitationDto getSurveyUnitWithCampaignById(String surveyUnitId) {
        return new SurveyUnitHabilitationDto("survey-unit1", "campaign-id");
    }
}
