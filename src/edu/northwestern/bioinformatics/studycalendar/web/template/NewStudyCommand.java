package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private StudyDao studyDao;
    private TemplateBase base;

    public NewStudyCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public Study create() {
        Study study = getBase().create();
        studyDao.save(study);
        return study;
    }

    public TemplateBase getBase() {
        if (base == null) return TemplateBase.BASIC;
        return base;
    }

    public void setBase(TemplateBase base) {
        this.base = base;
    }

    public static interface TemplateBase {
        TemplateBase BLANK = new Blank();
        TemplateBase BASIC = new Basic();

        Study create();
    }

    protected static class Blank implements TemplateBase {
        public Study create() {
            Study study = new Study();
            study.setName("New blank study");
            study.setPlannedCalendar(new PlannedCalendar());
            study.getPlannedCalendar().addEpoch(Epoch.create("New epoch"));
            return study;
        }
    }

    protected static class Basic implements TemplateBase {
        public Study create() {
            Study study = new Study();
            study.setName("New study");
            study.setPlannedCalendar(new PlannedCalendar());
            study.getPlannedCalendar().addEpoch(Epoch.create("Screening"));
            study.getPlannedCalendar().addEpoch(Epoch.create("Treatment", "A", "B", "C"));
            study.getPlannedCalendar().addEpoch(Epoch.create("Follow up"));
            return study;
        }
    }
}
