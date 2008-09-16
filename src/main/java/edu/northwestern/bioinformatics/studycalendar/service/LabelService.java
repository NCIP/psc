package edu.northwestern.bioinformatics.studycalendar.service;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.LabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Label;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

import java.util.List;

/**
 * @author Nataliya Shurupova
 */

public class LabelService {
    private LabelDao labelDao;
    private PlannedActivityDao plannedActivityDao;
    private PlannedActivityLabelDao plannedActivityLabelDao;
    protected final Logger log = LoggerFactory.getLogger(getClass());


    public boolean deleteLabel(Label label, PlannedActivity plannedActivity) {
        List<PlannedActivityLabel> plannedActivityLabels = plannedActivityLabelDao.getPALabelByPlannedActivityIdAndLabelId(plannedActivity.getId(), label.getId());
        if (plannedActivityLabels != null) {
            for (PlannedActivityLabel palabel : plannedActivityLabels) {
                plannedActivityLabelDao.delete(palabel);
            }
        }
        labelDao.delete(label);
        return true;
    }

    public Label getOrCreateLabel(String labelName) {
        Label label = labelDao.getByName(labelName);
        if (label == null) {
            label = new Label();
            label.setName(labelName);
            try {
                labelDao.save(label);
            } catch(DataIntegrityViolationException ex) {
                log.debug("Throwing exception due to constraint violation. Label with this name was added by other activity" + ex.getMessage());
                label = labelDao.getByName(labelName);
            }
        }
        return label;
    }


    public PlannedActivityLabel getOrCreatePlannedActivityLabel(Label label, PlannedActivity plannedActivity) {
        PlannedActivityLabel plannedActivityLabel = plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity.getId(), label.getId(), plannedActivity.getPeriod().getRepetitions());
        if (plannedActivityLabel == null) {
            plannedActivityLabel = new PlannedActivityLabel();
            plannedActivityLabel.setPlannedActivity(plannedActivity);
            plannedActivityLabel.setLabel(label);
            plannedActivityLabel.setRepetitionNumber(plannedActivity.getPeriod().getRepetitions());
            try {
                plannedActivityLabelDao.save(plannedActivityLabel);
            } catch (DataIntegrityViolationException ex) {
                plannedActivityLabel = plannedActivityLabelDao.getPALabelByPlannedActivityIdLabelIdRepNum(plannedActivity.getId(), label.getId(), plannedActivity.getPeriod().getRepetitions());
            }
        }
        return plannedActivityLabel;
    }

    
    //need to see if the space-delimiters exist in labelName
    public String[] getLabelsFromStringParameter (String labelName) {
        return labelName.split(" ");
    }


    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setLabelDao(LabelDao labelDao) {
        this.labelDao = labelDao;
    }

    @Required
    public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
        this.plannedActivityLabelDao = plannedActivityLabelDao;
    }
}
