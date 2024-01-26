package fr.insee.queen.domain.surveyunit.service;

import fr.insee.queen.domain.surveyunit.model.*;
import fr.insee.queen.domain.surveyunit.service.exception.StateDataInvalidDateException;

import java.util.List;
import java.util.Optional;

public interface SurveyUnitService {
    boolean existsById(String surveyUnitId);

    void throwExceptionIfSurveyUnitNotExist(String surveyUnitId);

    void throwExceptionIfSurveyUnitExist(String surveyUnitId);

    SurveyUnit getSurveyUnit(String id);

    List<SurveyUnitSummary> findSummariesByCampaignId(String campaignId);

    List<String> findAllSurveyUnitIds();

    void updateSurveyUnit(SurveyUnit surveyUnit);

    void updateSurveyUnitSummary(SurveyUnitSummary surveyUnit);

    void createSurveyUnit(SurveyUnit surveyUnit) throws StateDataInvalidDateException;

    List<SurveyUnitSummary> findSummariesByIds(List<String> surveyUnits);

    Optional<SurveyUnitSummary> findSummaryById(String surveyUnitId);

    void delete(String surveyUnitId);

    SurveyUnitDepositProof getSurveyUnitDepositProof(String surveyUnitId);

    SurveyUnitSummary getSurveyUnitWithCampaignById(String surveyUnitId);

    List<SurveyUnit> findByIds(List<String> surveyUnitIds);

    List<SurveyUnit> findAllSurveyUnits();

    /**
     * Retrieve survey units with existing state by campaign
     *
     * @param campaignId campaign on which we retrieve survey units
     * @return survey units ids
     */
    List<String> findSurveyUnitsIdsWithExistingState(String campaignId);

    /**
     * Retrieve survey units with existing state by campaign id and state data
     *
     * @param campaignId campaign on which we retrieve survey units
     * @param stateDataTypes states to filter on
     * @return survey units ids
     */
    List<String> findSurveyUnitsIds(String campaignId, StateDataType... stateDataTypes);

    /**
     * Retrieve survey units with existing state by survey unit ids
     *
     * @param surveyUnits ids to search
     * @return {@link SurveyUnitState} survey units
     */
    List<SurveyUnitState> findSurveyUnitsWithState(List<String> surveyUnits);

    /**
     * Retrieve survey units with existing state by campaign id
     *
     * @param campaignId campaign id
     * @return {@link SurveyUnitState} survey units
     */
    List<SurveyUnitState> findSurveyUnitsWithState(String campaignId);
}
