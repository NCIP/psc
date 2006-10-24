package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public abstract class AddToCommand {
    private StudyDao studyDao;

    public AddToCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void apply() {
        createAndAddNewChild();
        studyDao.save(toSave());
    }

    public Map<String, Object> getModel() {
        return new HashMap<String, Object>();
    }

    protected abstract String whatAdded();

    protected abstract void createAndAddNewChild();

    protected abstract Study toSave();
}
