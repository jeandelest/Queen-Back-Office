package fr.insee.queen.api.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name="personalization")
@Getter
@Setter
@AllArgsConstructor
public class PersonalizationDB {

    /**
     * The id
     */
    @Id
    @org.springframework.data.annotation.Id
    private UUID id;

    /**
     * The value of data (jsonb format)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String value;

    /**
     * The SurveyUnit associated
     */
    @OneToOne
    @JoinColumn(name = "survey_unit_id", referencedColumnName = "id")
    private SurveyUnitDB surveyUnit;


    public PersonalizationDB() {
        super();
        this.id = UUID.randomUUID();
    }
}