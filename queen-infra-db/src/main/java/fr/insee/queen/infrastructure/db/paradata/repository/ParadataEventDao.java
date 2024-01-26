package fr.insee.queen.infrastructure.db.paradata.repository;

import fr.insee.queen.domain.paradata.gateway.ParadataEventRepository;
import fr.insee.queen.domain.paradata.model.Paradata;
import fr.insee.queen.infrastructure.db.paradata.repository.jpa.ParadataEventJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ParadataEventDao implements ParadataEventRepository {
    private final ParadataEventJpaRepository jpaRepository;

    @Override
    public void createParadataEvent(UUID id, String paradataValue, String surveyUnitId) {
        jpaRepository.createParadataEvent(id, paradataValue, surveyUnitId);
    }

    @Override
    public List<Paradata> findParadatasBySurveyUnitId(String surveyUnitId) {
        return jpaRepository.findParadatasBySurveyUnitId(surveyUnitId);
    }

    @Override
    public void deleteParadata(UUID paradataId) {
        jpaRepository.deleteById(paradataId);
    }
}
