package fr.insee.queen.domain.paradata.service;

import fr.insee.queen.domain.paradata.model.Paradata;

import java.util.List;
import java.util.UUID;

public interface ParadataEventService {
    void createParadataEvent(String surveyUnitId, String paradataValue);

    /**
     * find paradatas for a survey unit
     * @param surveyUnitId survey unit id
     */
    List<Paradata> findParadatasBySurveyUnitId(String surveyUnitId);

    /**
     * Delete paradata by its id
     * @param paradataId paradata id
     */
    void deleteParadata(UUID paradataId);
}
