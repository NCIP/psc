package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Column;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "study_secondary_idents")
@GenericGenerator(name = "id-generator", strategy = "native",
    parameters = {
        @Parameter(name = "sequence", value = "seq_study_secondary_idents_id")
    }
)
public class StudySecondaryIdentifier extends AbstractMutableDomainObject implements Comparable<StudySecondaryIdentifier> {
    private Study study;
    private String type, value;

    ////// LOGIC

    public int compareTo(StudySecondaryIdentifier other) {
        int typeDiff = ComparisonTools.nullSafeCompare(this.getType(), other.getType());
        if (typeDiff != 0) return typeDiff;

        int valueDiff = ComparisonTools.nullSafeCompare(this.getValue(), other.getValue());
        if (valueDiff != 0) return valueDiff;

        Integer thisStudyId  = getStudy() == null ? null : getStudy().getId();
        Integer otherStudyId = other.getStudy() == null ? null : other.getStudy().getId();
        int studyDiff = ComparisonTools.nullSafeCompare(thisStudyId, otherStudyId);
        if (studyDiff != 0) return studyDiff;

        return 0;
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    @Column(name = "identifier_type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
