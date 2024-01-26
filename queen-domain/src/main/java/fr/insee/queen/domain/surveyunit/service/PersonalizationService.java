package fr.insee.queen.domain.surveyunit.service;

public interface PersonalizationService {
    String getPersonalization(String surveyUnitId);

    void updatePersonalization(String surveyUnitId, String personalizationValue);
}
