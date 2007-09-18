package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class TemplateEditCommand extends EditCommand {
    private Mode mode;
    private DaoFinder daoFinder;

    @Override
    public Map<String, Object> getModel() {
        Map<String, Object> model = super.getModel();
        Map<String, Object> modeModel = getMode().getModel();
        model.put("developmentRevision", getStudy().getDevelopmentAmendment());
        model.put("revisionChanges",
            new RevisionChanges(daoFinder, getStudy().getDevelopmentAmendment(), getStudy()));
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

    ////// CONFIGURATION

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
