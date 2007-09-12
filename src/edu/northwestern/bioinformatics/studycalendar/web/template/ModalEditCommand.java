package edu.northwestern.bioinformatics.studycalendar.web.template;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class ModalEditCommand extends EditCommand {
    private Mode mode;

    @Override
    public Map<String, Object> getModel() {
        Map<String, Object> model = super.getModel();
        Map<String, Object> modeModel = getMode().getModel();
        if (modeModel != null) {
            model.putAll(modeModel);
        }
        return model;
    }

    @Override
    protected void performEdit() {
        getMode().performEdit();
    }

    @Override
    protected String getRelativeViewName() {
        return getMode().getRelativeViewName();
    }

    private Mode getMode() {
        if (mode == null) mode = selectMode();
        return mode;
    }

    protected Mode studyMode() { throw new UnsupportedOperationException("No study mode for " + getClass().getSimpleName()); }
    protected Mode epochMode() { throw new UnsupportedOperationException("No epoch mode for " + getClass().getSimpleName()); }
    protected Mode armMode() { throw new UnsupportedOperationException("No arm mode for " + getClass().getSimpleName()); }

    protected Mode selectMode() {
        Mode newMode;
        if (getArm() != null) {
            newMode = armMode();
        } else if (getEpoch() != null) {
            newMode = epochMode();
        } else {
            newMode = studyMode();
        }
        return newMode;
    }

    protected static interface Mode {
        String getRelativeViewName();
        Map<String, Object> getModel();
        void performEdit();
    }
}
