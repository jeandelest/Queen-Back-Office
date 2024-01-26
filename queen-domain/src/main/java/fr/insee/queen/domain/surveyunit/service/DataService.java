package fr.insee.queen.domain.surveyunit.service;

import java.util.List;

public interface DataService {
    String getData(String surveyUnitId);
    void updateData(String surveyUnitId, String dataValue);
    void deleteDataBySurveyUnitIds(List<String> surveyUnitIds);
}
