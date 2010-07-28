package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AddToCommand extends EditTemplateCommand {
    @Override
    protected Mode studyMode() {
        return new AddEpoch();
    }

    @Override
    protected Mode epochMode() {
        return new AddStudySegment();
    }

    private class AddStudySegment extends Mode {
        @Override
        public String getRelativeViewName() {
            return "addStudySegment";
        }

        @Override
        public void performEdit() {
            updateRevision(getEpoch(),
                Add.create(new StudySegment(), getRevisedEpoch().getStudySegments().size()));
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public Map<String, Object> getModel() {
            List<StudySegment> studySegments = getRevisedEpoch().getStudySegments();
            return new ModelMap("studySegment", studySegments.get(studySegments.size() - 1));
        }
    }

    private class AddEpoch extends Mode {
        @Override
        public String getRelativeViewName() {
            return "addEpoch";
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public Map<String, Object> getModel() {
            List<Epoch> epochs = getRevisedStudy().getPlannedCalendar().getEpochs();
            return new ModelMap("epoch", epochs.get(epochs.size() - 1));
        }

        @Override
        public void performEdit() {
            updateRevision(getStudy().getPlannedCalendar(),
                Add.create(Epoch.create(), getRevisedStudy().getPlannedCalendar().getEpochs().size()));
        }
    }
}
