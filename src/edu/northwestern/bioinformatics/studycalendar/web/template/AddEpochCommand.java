package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

import java.util.Map;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AddEpochCommand extends AddToCommand {
    private Study study;

    public AddEpochCommand(StudyDao studyDao) { super(studyDao); }

    @Override
    public Map<String, Object> getModel() {
        Map<String, Object> model = super.getModel();
        List<Epoch> epochs = getStudy().getPlannedCalendar().getEpochs();
        model.put("epoch", epochs.get(epochs.size() - 1));
        model.put("previousEpoch", epochs.get(epochs.size() - 2));
        return model;
    }

    protected void createAndAddNewChild() {
        getStudy().getPlannedCalendar().addEpoch(Epoch.create("New Epoch"));
    }

    protected String whatAdded() {
        return "epoch";
    }

    protected Study toSave() {
        return getStudy();
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
