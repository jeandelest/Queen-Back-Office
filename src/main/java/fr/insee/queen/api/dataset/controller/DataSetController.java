package fr.insee.queen.api.dataset.controller;

import fr.insee.queen.api.configuration.auth.AuthorityRole;
import fr.insee.queen.api.configuration.swagger.role.DisplayRolesOnUI;
import fr.insee.queen.api.dataset.service.DataSetInjectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create a dataset for demo purposes
 */
@RestController
@Tag(name = "10. Create Data Set", description = "Endpoints for creating dataset")
@RequestMapping(path = "/api")
@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(name = "feature.enable.dataset", havingValue = "true")
public class DataSetController {
    private final DataSetInjectorService injector;

    /**
     * Create dataset for demo environments
     */
    @Operation(summary = "Create dataset")
    @PostMapping(path = "/create-dataset")
    @DisplayRolesOnUI
    @PreAuthorize(AuthorityRole.HAS_ADMIN_PRIVILEGES)
    @ResponseStatus(HttpStatus.CREATED)
    public void createDataSet() {
        injector.createDataSet();
    }

}
