package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.Date;

public class CreateAmendmentCommand {
    private Study study;
    private Date date;
    private String name;
    private boolean mandatory;

    private StudyService studyService;

    public CreateAmendmentCommand(StudyService studyService) {
        this.studyService = studyService;
        mandatory = true;
    }

    public void apply() throws Exception{
        Amendment a = new Amendment();
        a.setName(getName());
        a.setDate(getDate());
        a.setPreviousAmendment(null);
        a.setMandatory(getMandatory());
        study.setDevelopmentAmendment(a);
        studyService.save(study);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }
                                                                       
    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

}
