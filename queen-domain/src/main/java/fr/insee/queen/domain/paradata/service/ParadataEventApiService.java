package fr.insee.queen.domain.paradata.service;

import fr.insee.queen.domain.paradata.gateway.ParadataEventRepository;
import fr.insee.queen.domain.paradata.model.Paradata;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ParadataEventApiService implements ParadataEventService {

    private final ParadataEventRepository paradataEventRepository;

    @Override
    public void createParadataEvent(String surveyUnitId, String paradataValue) {
        paradataEventRepository.createParadataEvent(UUID.randomUUID(), paradataValue, surveyUnitId);
    }

    @Override
    public List<Paradata> findParadatasBySurveyUnitId(String surveyUnitId) {
        return paradataEventRepository.findParadatasBySurveyUnitId(surveyUnitId);
    }

    @Override
    public void deleteParadata(UUID paradataId) {
        paradataEventRepository.deleteParadata(paradataId);
    }
}
