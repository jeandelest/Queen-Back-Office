package fr.insee.queen.api.depositproof.controller;

import fr.insee.queen.api.configuration.auth.AuthorityRole;
import fr.insee.queen.api.depositproof.service.DepositProofService;
import fr.insee.queen.api.depositproof.service.exception.DepositProofException;
import fr.insee.queen.api.depositproof.service.model.PdfDepositProof;
import fr.insee.queen.api.pilotage.controller.PilotageComponent;
import fr.insee.queen.api.pilotage.service.PilotageRole;
import fr.insee.queen.api.web.authentication.AuthenticationHelper;
import fr.insee.queen.api.web.validation.IdValid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Handle survey units
 */
@RestController
@Tag(name = "06. Survey units", description = "Endpoints for survey units")
@RequestMapping(path = "/api")
@Slf4j
@RequiredArgsConstructor
@Validated
public class DepositProofController {
    private final DepositProofService depositProofService;
    private final PilotageComponent pilotageComponent;
    private final AuthenticationHelper authHelper;

    /**
     * Generate and retrieve a deposit proof (pdf file) for a survey unit
     *
     * @param surveyUnitId survey unit id
     * @param response     HttpServletResponse object
     */
    @Operation(summary = "Get deposit proof for a survey unit")
    @GetMapping(value = "/survey-unit/{id}/deposit-proof")
    @PreAuthorize(AuthorityRole.HAS_ANY_ROLE)
    public void generateDepositProof(@IdValid @PathVariable(value = "id") String surveyUnitId,
                                     HttpServletResponse response) {
        pilotageComponent.checkHabilitations(surveyUnitId, PilotageRole.INTERVIEWER, PilotageRole.REVIEWER);

        String username = authHelper.getUserId();
        PdfDepositProof depositProof = depositProofService.generateDepositProof(username, surveyUnitId);

        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=\"" + depositProof.filename() + "\"");

        try (OutputStream out = response.getOutputStream()) {
            File pdfFile = depositProof.depositProof();
            out.write(Files.readAllBytes(pdfFile.toPath()));
            Files.delete(pdfFile.toPath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new DepositProofException();
        }
    }
}
