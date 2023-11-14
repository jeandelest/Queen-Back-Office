package fr.insee.queen.api.pilotage.repository.dummy;

import fr.insee.queen.api.pilotage.service.PilotageRole;
import fr.insee.queen.api.pilotage.service.gateway.PilotageRepository;
import fr.insee.queen.api.pilotage.service.model.PilotageCampaign;
import fr.insee.queen.api.surveyunit.service.model.SurveyUnitSummary;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

public class PilotageFakeRepository implements PilotageRepository {

    public static final String INTERVIEWER_CAMPAIGN1_ID = "interviewer-campaign1";
    public static final String CURRENT_SU_CAMPAIGN1_ID = "su-campaign1";

    @Getter
    private boolean wentThroughIsClosedCampaign = false;
    @Getter
    private boolean wentThroughHasHabilitation = true;
    @Setter
    private boolean nullInterviewerCampaigns = false;
    @Setter
    private boolean nullCurrentSurveyUnit = false;

    @Override
    public boolean isClosed(String campaignId, String authToken) {
        wentThroughIsClosedCampaign = true;
        return false;
    }

    @Override
    public List<LinkedHashMap<String, String>> getSurveyUnits(String authToken, String campaignId) {
        if (nullCurrentSurveyUnit) {
            return null;
        }
        LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
        map1.put("campaign", CURRENT_SU_CAMPAIGN1_ID);
        map1.put("id", "survey-unit1");
        LinkedHashMap<String, String> map2 = new LinkedHashMap<>();
        map2.put("campaign", "su-campaign2");
        map2.put("id", "survey-unit2");
        LinkedHashMap<String, String> map3 = new LinkedHashMap<>();
        map3.put("campaign", CURRENT_SU_CAMPAIGN1_ID);
        map3.put("id", "survey-unit3");
        return List.of(map1, map2, map3);
    }

    @Override
    public List<PilotageCampaign> getInterviewerCampaigns(String authToken) {
        if (nullInterviewerCampaigns) {
            return null;
        }
        return List.of(
                new PilotageCampaign(INTERVIEWER_CAMPAIGN1_ID, List.of("questionnaire-id")),
                new PilotageCampaign("interviewer-campaign2", List.of("questionnaire-id"))
        );
    }

    @Override
    public boolean hasHabilitation(SurveyUnitSummary surveyUnit, PilotageRole role, String idep, String authToken) {
        this.wentThroughHasHabilitation = true;
        return true;
    }

}