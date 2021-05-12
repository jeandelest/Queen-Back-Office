package fr.insee.queen.api.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.insee.queen.api.domain.Metadata;
import fr.insee.queen.api.dto.metadata.MetadataDto;
import fr.insee.queen.api.repository.ApiRepository;
import fr.insee.queen.api.repository.MetadataRepository;
import fr.insee.queen.api.service.AbstractService;
import fr.insee.queen.api.service.MetadataService;

@Service
public class MetadataServiceImpl extends AbstractService<Metadata, UUID> implements MetadataService {

    protected final MetadataRepository metadataRepository;

    @Autowired
    public MetadataServiceImpl(MetadataRepository repository) {
        this.metadataRepository = repository;
    }

    @Override
    protected ApiRepository<Metadata, UUID> getRepository() {
        return metadataRepository;
    }

	@Override
	public void save(Metadata metadata) {
		metadataRepository.save(metadata);
	}

	@Override
	public MetadataDto findDtoByCampaignId(String id) {
		return metadataRepository.findDtoByCampaignId(id);
	}

	@Override
	public Optional<Metadata> findById(UUID uuid) {
		return metadataRepository.findById(uuid);
	}
}