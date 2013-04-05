/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class CreateAmendmentCommand implements Validatable {
    private Study study;
    private Date date;
    private String name;
    private boolean mandatory;

    private StudyService studyService;
    private AmendmentDao amendmentDao;

        protected final Logger log = LoggerFactory.getLogger(getClass());

    public CreateAmendmentCommand(StudyService studyService, AmendmentDao amendmentDao) {
        this.studyService = studyService;
        this.amendmentDao = amendmentDao;
        mandatory = true;
    }

    public void apply() throws Exception {
        Amendment a = new Amendment();
        a.setName(getName());
        a.setDate(getDate());
        a.setPreviousAmendment(null);
        a.setMandatory(getMandatory());
        study.setDevelopmentAmendment(a);
        studyService.save(study);
    }

    public void validate(Errors errors) {
        if (getDate() == null) {
            errors.rejectValue("date", "error.empty.amendment.date");
        } else {
            String keyString = new Amendment.Key(getDate(), getName()).toString();
            if (amendmentDao.getByNaturalKey(keyString, getStudy()) !=null ) {
                errors.rejectValue("date", "error.unique.amendment.date.name");
            }
        }
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
