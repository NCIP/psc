package edu.northwestern.bioinformatics.studycalendar.web;

import edu.nwu.bioinformatics.commons.spring.Validatable;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.AmendmentLogin;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.AmendmentLoginDao;

import org.springframework.validation.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmendmentLoginCommand implements Validatable {
    private static final Logger log = LoggerFactory.getLogger(AmendmentLoginCommand.class.getName());

    private Study study;
    private Integer amendmentNumber;
    private String date;
    private AmendmentLoginDao amendmentLoginDao;
    private StudyDao studyDao;
    private String action;


    public AmendmentLoginCommand(StudyDao studyDao, AmendmentLoginDao amendmentLoginDao) {
        this.studyDao = studyDao;
        this.amendmentLoginDao = amendmentLoginDao;
    }

    public void apply() throws Exception{
        if (getAction().equals("Submit")) {
            AmendmentLogin a = new AmendmentLogin();
            a.setAmendmentNumber(getAmendmentNumber());
            a.setDate(getDate());
            a.setStudyId(getStudy().getId());
    //        amendmentLoginDao.save(a);
            study.setAmended(true);
            studyDao.save(study);
        }
    }

    public void validate(Errors errors) {
    }


    public Integer getAmendmentNumber() {
        return amendmentNumber;
    }

    public void setAmendmentNumber(Integer amendmentNumber) {
        this.amendmentNumber = amendmentNumber;
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

    public AmendmentLoginDao getAmendmentDao() {
        return amendmentLoginDao;
    }

    public void setAmendmentDao(AmendmentLoginDao amendmentLoginDao) {
        this.amendmentLoginDao = amendmentLoginDao;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
