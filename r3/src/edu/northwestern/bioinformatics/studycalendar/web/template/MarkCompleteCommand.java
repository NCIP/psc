package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;


/**
 * @author Jaron Sampson
 */
public class MarkCompleteCommand {
    private Boolean completed;
    private Study study;

    private StudyDao studyDao;

    public MarkCompleteCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    ////// LOGIC

    public void apply() {
        getStudy().getPlannedCalendar().setComplete(getCompleted());
        studyDao.save(getStudy());
    }

    ////// BOUND PROPERTIES

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
