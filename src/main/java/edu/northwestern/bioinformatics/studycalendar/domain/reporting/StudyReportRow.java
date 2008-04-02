package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author John Dzak
 */
public class StudyReportRow implements DomainObject {
    Integer id;
    private Study study;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }


}
