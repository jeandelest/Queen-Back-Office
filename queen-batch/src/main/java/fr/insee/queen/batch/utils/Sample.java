package fr.insee.queen.batch.utils;

import fr.insee.queen.domain.campaign.model.CampaignSummary;
import fr.insee.queen.domain.surveyunit.model.SurveyUnit;
import lombok.Data;

import java.util.List;

/**
* Object XmlSample : represent the sample XML file
* 
* @author Claudel Benjamin
* 
*/
@Data
public class Sample {
	/**
	* The fileName of sample
	*/
	private final String fileName;
	/**
	* The campaign of sample
	*/
	private final CampaignSummary campaign;

	private final List<SurveyUnit> surveyUnits;

	public Sample(String fileName, CampaignSummary campaign, List<SurveyUnit> surveyUnits) {
		super();
		this.fileName = fileName;
		this.campaign = campaign;
		this.surveyUnits = surveyUnits;
	}
}
