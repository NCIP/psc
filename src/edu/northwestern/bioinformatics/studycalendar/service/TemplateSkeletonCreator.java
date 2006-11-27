package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import javax.persistence.Basic;

/**
 * @author Rhett Sutphin
*/
public interface TemplateSkeletonCreator {
    TemplateSkeletonCreator BLANK = new TemplateSkeletonCreatorImpl.Blank();
    TemplateSkeletonCreator BASIC = new TemplateSkeletonCreatorImpl.Basic();

    Study create();
}

class TemplateSkeletonCreatorImpl {
    static class Blank implements TemplateSkeletonCreator {
        public Study create() {
            Study study = new Study();
            study.setName("[Unnamed blank study]");
            study.setPlannedCalendar(new PlannedCalendar());
            study.getPlannedCalendar().addEpoch(Epoch.create("[Unnamed epoch]"));
            return study;
        }
    }

    static class Basic implements TemplateSkeletonCreator {
        public Study create() {
            Study study = new Study();
            study.setName("[Unnamed study]");
            study.setPlannedCalendar(new PlannedCalendar());
            study.getPlannedCalendar().addEpoch(Epoch.create("Screening"));
            study.getPlannedCalendar().addEpoch(Epoch.create("Treatment", "A", "B", "C"));
            study.getPlannedCalendar().addEpoch(Epoch.create("Follow up"));
            return study;
        }
    }
}
