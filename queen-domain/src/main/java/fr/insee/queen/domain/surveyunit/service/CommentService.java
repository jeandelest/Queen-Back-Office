package fr.insee.queen.domain.surveyunit.service;

public interface CommentService {
    String getComment(String surveyUnitId);

    void updateComment(String surveyUnitId, String commentValue);
}
