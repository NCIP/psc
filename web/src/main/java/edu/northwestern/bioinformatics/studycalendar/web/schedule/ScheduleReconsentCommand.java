/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.validation.Errors;

import java.util.Date;

public class ScheduleReconsentCommand implements Validatable {

    private String details;
    private Study study;
    private NowFactory nowFactory;
    private Date startDate;
    private StudyService studyService;

    public ScheduleReconsentCommand(StudyService studyService, NowFactory nowFactory) {
        this.studyService = studyService;
        this.nowFactory = nowFactory;
    }

    public void apply() throws Exception{
        studyService.scheduleReconsent(study, startDate, details);
    }

    public void validate(Errors errors) {
        if(startDate != null) {
            if(startDate.before(nowFactory.getNow())) {
                errors.rejectValue("startDate", "error.start.date.after.current.date");
            }
        }
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }
}
