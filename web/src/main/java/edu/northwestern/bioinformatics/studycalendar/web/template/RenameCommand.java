package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RenameCommand extends EditTemplateCommand {
    private String value;

    ////// BOUND PROPERTIES

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    ////// MODES

    @Override protected Mode studyMode() { return new RenameStudy(); }
    @Override protected Mode epochMode() { return new RenameEpoch(); }
    @Override protected Mode studySegmentMode()   { return new RenameStudySegment(); }

    private abstract class RenameMode extends Mode {
        public Map<String, Object> getModel() {
            return null;
        }

        public String getRelativeViewName() {
            return "rename";
        }

        protected void rename(Named target) {
            updateRevision((PlanTreeNode<?>) target,
                PropertyChange.create("name", target.getName(), getValue()));
        }
    }

    // Rename study is unique in that it does not result in a Change
    // TODO: decide if this is reasonable
    private class RenameStudy extends RenameMode {
        public void performEdit() {
            getStudy().setName(getValue());
        }

        public boolean validAction() {
            if (getStudyService().getStudyDao().getByAssignedIdentifier(getValue())!=null){
                return false;
            }
            return true;
        }
    }

    private class RenameEpoch extends RenameMode {
        public void performEdit() {
            rename(getEpoch());
            if (!getEpoch().isMultipleStudySegments()) {
                StudySegment soleStudySegment = getEpoch().getStudySegments().get(0);
                rename(soleStudySegment);
                setStudySegment(soleStudySegment);
            }
        }
    }

    private class RenameStudySegment extends RenameMode {
        public void performEdit() {
            rename(getStudySegment());
            if (getStudySegment().getEpoch() != null && !getStudySegment().getEpoch().isMultipleStudySegments()) {
                rename(getStudySegment().getEpoch());
            }
        }
    }
}
