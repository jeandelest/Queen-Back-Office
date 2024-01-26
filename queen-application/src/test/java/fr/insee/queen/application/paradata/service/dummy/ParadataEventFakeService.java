package fr.insee.queen.application.paradata.service.dummy;

import fr.insee.queen.domain.paradata.model.Paradata;
import fr.insee.queen.domain.paradata.service.ParadataEventService;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class ParadataEventFakeService implements ParadataEventService {
    @Getter
    private boolean created = false;

    @Override
    public void createParadataEvent(String surveyUnitId, String paradataValue) {
        this.created = true;
    }

    @Override
    public List<Paradata> findParadatasBySurveyUnitId(String surveyUnitId) {
        return null;
    }

    @Override
    public void deleteParadata(UUID paradataId) {

    }
}
