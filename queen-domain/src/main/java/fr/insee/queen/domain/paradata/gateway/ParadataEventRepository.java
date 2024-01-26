package fr.insee.queen.domain.paradata.gateway;

import fr.insee.queen.domain.paradata.model.Paradata;

import java.util.List;
import java.util.UUID;

public interface ParadataEventRepository {
    /**
     * Create paradata for a survey unit
     *
     * @param id paradata id
     * @param paradataValue paradata value (json format)
     * @param surveyUnitId survey unit id
     */
    void createParadataEvent(UUID id, String paradataValue, String surveyUnitId);

    /**
     * Retrieve paradatas for a survey unit
     *
     * @param surveyUnitId survey unit id
     * @return @{@link Paradata} list of paradatas
     */
    List<Paradata> findParadatasBySurveyUnitId(String surveyUnitId);

    /**
     * Delete paradata by its id
     * @param paradataId paradata id
     */
    void deleteParadata(UUID paradataId);
}
