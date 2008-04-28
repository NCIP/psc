package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateSkeletonCreatorImpl {

//   private StudyDao studyDao;

   public static Study createBase(String name) {

       Study study = new Study();
//       studyDao.
       study.setName(name);
       study.setPlannedCalendar(new PlannedCalendar());
       Amendment start = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
       start.setDate(new Date()); // TODO: might want to use NowFactory
       start.addDelta(new PlannedCalendarDelta(study.getPlannedCalendar()));
       study.setDevelopmentAmendment(start);
       return study;
   }

   public static void addEpoch(Study study, Integer index, Epoch epoch) {
       Delta<?> delta = study.getDevelopmentAmendment().getDeltas().get(0);
       Add add = new Add();
       add.setChild(epoch);
       add.setIndex(index);
       delta.addChange(add);
   }

   static class Blank implements TemplateSkeletonCreator {
       public Study create(String studyName) {
           String newStudyName;
            if (studyName == null || studyName.length()==0) {
                newStudyName = "[Unnamed blank study]";
            } else {
                newStudyName = studyName;
            }
           Study study = createBase(newStudyName);
           addEpoch(study, 0, Epoch.create("[Unnamed epoch]"));
           return study;
       }
   }

    static class Basic implements TemplateSkeletonCreator {
        public Study create(String studyName) {
        String newStudyName;
            if (studyName == null || studyName.length()==0) {
                newStudyName = "[ABC 1234]";
            } else {
                newStudyName = studyName;
            }
            Study study = createBase(newStudyName);
            addEpoch(study, 0, Epoch.create("Screening"));
            addEpoch(study, 1, Epoch.create("Treatment", "A", "B", "C"));
            addEpoch(study, 2, Epoch.create("Follow up"));
            return study;
        }
   }
}
