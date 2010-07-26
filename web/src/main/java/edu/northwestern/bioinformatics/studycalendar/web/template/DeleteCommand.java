package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class DeleteCommand extends EditTemplateCommand {

    ////// MODES

    @Override
    protected Mode epochMode() {
        return new DeleteEpoch();
    }

    @Override
    protected Mode studySegmentMode() {
        return new DeleteStudySegment();
    }

    private abstract class DeleteMode<T extends PlanTreeNode<? extends PlanTreeInnerNode>> extends Mode {
        @Override
        public final void performEdit() {
            if (getObjectParent().getChildren().size() < 2) return;
            updateRevision(getObjectParent(), Remove.create(getObject()));
        }

        @Override
        public Map<String, Object> getModel() {
            Map<String, Object> map = new HashMap<String, Object>();
            Epoch epoch = null;
            StudySegment studySegment = null;
            if (getObjectParent() instanceof Epoch) {
                epoch = (Epoch)getObjectParent();
                List<StudySegment> studySegments = epoch.getStudySegments();
                studySegments.remove(getRevisedStudySegment());
                studySegment =  studySegments.get(studySegments.size() - 1);
            } else if (getObjectParent() instanceof PlannedCalendar) {
                PlannedCalendar calendar = (PlannedCalendar)getObjectParent();
                epoch = calendar.getEpochs().get(0);
                if (epoch.getId().equals(getObject().getId())) {
                    epoch = calendar.getEpochs().get(1);
                }
                studySegment = epoch.getStudySegments().get(0);
            }
            map.put("epoch", epoch);
            map.put("studySegment", studySegment);
            StudySegmentTemplate studySegmentTemplate = new StudySegmentTemplate(studySegment);
            map.put("template", studySegmentTemplate);
            return map;
        }


        protected abstract T getObject();
        protected abstract PlanTreeInnerNode getObjectParent();
    }

    private class DeleteEpoch extends DeleteMode<Epoch> {
        @Override
        public String getRelativeViewName() {
            return "deleteEpoch";
        }

        @Override
        protected Epoch getObject() {
            return getEpoch();
        }

        @Override
        protected PlanTreeInnerNode getObjectParent() {
            return getSafeEpochParent();
        }
    }

    private class DeleteStudySegment extends DeleteMode<StudySegment> {
        @Override
        public String getRelativeViewName() {
            return "deleteStudySegment";
        }

        @Override
        protected StudySegment getObject() {
            return getStudySegment();
        }

        @Override
        protected PlanTreeInnerNode getObjectParent() {
            return getSafeStudySegmentParent();
        }
    }
}
