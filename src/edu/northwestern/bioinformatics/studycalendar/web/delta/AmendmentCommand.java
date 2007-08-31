package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.nwu.bioinformatics.commons.spring.Validatable;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;

import org.springframework.validation.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmendmentCommand implements Validatable {
    private static final Logger log = LoggerFactory.getLogger(AmendmentCommand.class.getName());

    private Study study;
    private String name;
    private String date;
    private Integer previousAmendment;

    private AmendmentDao amendmentDao;
    private StudyDao studyDao;
    private String action;


    public AmendmentCommand(StudyDao studyDao, AmendmentDao amendmentDao) {
        this.studyDao = studyDao;
        this.amendmentDao = amendmentDao;
    }

    public void apply() throws Exception{
        if (getAction().equals("Submit")) {
            Amendment a = new Amendment();
            a.setName(getName());
            a.setDate(getDate());
            a.setStudyId(getStudy().getId());
            a.setPreviousAmendment(null);
            amendmentDao.save(a);
            study.setAmended(true);
            studyDao.save(study);
        }
    }

    public void validate(Errors errors) {
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

    public String getDate() {
        return date;
    }
                                                                       
    public void setDate(String date) {
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
