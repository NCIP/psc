package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.Date;

public class AmendmentCommand {
    private Study study;
    private Date date;
    private String name;
    private Integer previousAmendment;

    private AmendmentDao amendmentDao;
    private StudyService studyService;
    private String action;


    public AmendmentCommand(StudyService studyService, AmendmentDao amendmentDao) {
        this.studyService = studyService;
        this.amendmentDao = amendmentDao;
    }

    public void apply() throws Exception{
        // TODO: why is this condition necessary?
        if (getAction().equals("Submit")) {
            Amendment a = new Amendment();
            a.setName(getName());
            a.setDate(getDate());
            a.setPreviousAmendment(null);
            amendmentDao.save(a);
            study.setDevelopmentAmendment(a);
            studyService.save(study);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPreviousAmendment() {
        return previousAmendment;
    }

    public void setPreviousAmendment(Integer previousAmendment) {
        this.previousAmendment = previousAmendment;
    }

    public Date getDate() {
        return date;
    }
                                                                       
    public void setDate(Date date) {
        this.date = date;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public AmendmentDao getAmendmentDao() {
        return amendmentDao;
    }

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
