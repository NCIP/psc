package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

import java.util.List;

public class PlannedActivityLabelDao extends StudyCalendarMutableDomainObjectDao<PlannedActivityLabel> implements DeletableDomainObjectDao<PlannedActivityLabel> {
    @Override
    public Class<PlannedActivityLabel> domainClass() {
        return PlannedActivityLabel.class;
    }

   /**
    * Returns a list of all the plannedActivityLabels currently available.
    *
    * @return      list of all the plannedActivityLabels currently available
    */
    public List<PlannedActivityLabel> getAll() {
        List<PlannedActivityLabel> sortedList;
        sortedList = getHibernateTemplate().find("from PlannedActivityLabel");
        return sortedList;
    }

    /**
    * Finds the plannedActivityLabels by label id.
    *
    * @param  labelId  the labelId that used to get plannedActivityLabels
    * @return      the plannedActivityLabels that corresponds to the labelId parameter
    */
    public List<PlannedActivityLabel> getByLabelId(Integer labelId) {
        List<PlannedActivityLabel> labels = getHibernateTemplate().find("from PlannedActivityLabel where label_id = ?", labelId);
        return labels;
    }

    /**
    * Finds the plannedActivityLabels by planned activity id.
    *
    * @param  plannedActivityId  the planned activity id that used to get plannedActivityLabels
    * @return      the plannedActivityLabels that corresponds to the plannedActivityId parameter
    */
    public PlannedActivityLabel getByPlannedActivityId(Integer plannedActivityId) {
        List<PlannedActivityLabel> labels = getHibernateTemplate().find("from PlannedActivityLabel where planned_activity_id = ?", plannedActivityId);
        if (labels.size() == 0) {
            return null;
        }
        return labels.get(0);
    }


    /**
    * Finds the plannedActivityLabels by planned activity id.
    *
    * @param  plannedActivityId  the planned activity id that used to get plannedActivityLabels
    * @return      the plannedActivityLabels that corresponds to the plannedActivityId parameter
    */
    public List<Object> getRepetitionsByPlannedActivityIdAndLabelId(Integer plannedActivityId, Integer labelId) {
        List<Object> repetitions = getHibernateTemplate().find("select repetitionNumber from PlannedActivityLabel where planned_activity_id = ? and label_id = ?", new Object[] { plannedActivityId, labelId });
        return repetitions;
    }


    /**
    * Finds the plannedActivityLabels by planned activity id and label id.
    *
    * @param  plannedActivityId  the planned activity id that used to get plannedActivityLabels
    * @param  labelId  the label id that used to get plannedActivityLabels
    * @return      the plannedActivityLabels that corresponds to the plannedActivityId and labelId parameters
    */
    public List<PlannedActivityLabel> getPALabelByPlannedActivityIdAndLabelId(Integer plannedActivityId, Integer labelId) {
        List<PlannedActivityLabel> labels = getHibernateTemplate().find("from PlannedActivityLabel where planned_activity_id = ? and label_id = ?", new Object[] { plannedActivityId, labelId });
        if(labels.size() == 0) {
            return null;
        }
        return labels;
    }

    /**
    * Finds the plannedActivityLabels by planned activity id, label id and repetition number.
    *
    * @param  plannedActivityId  the planned activity id that used to get plannedActivityLabels
    * @param  labelId  the label id that used to get plannedActivityLabels
    * @param  repetitionNumber the repetition number that is used to get plannedActivityLabels
    * @return      the plannedActivityLabels that corresponds to the plannedActivityId parameter
    */
    public PlannedActivityLabel getPALabelByPlannedActivityIdLabelIdRepNum(Integer plannedActivityId, Integer labelId, Integer repetitionNumber) {
        List<PlannedActivityLabel> labels =
                getHibernateTemplate().find("from PlannedActivityLabel where planned_activity_id = ? and label_id = ? and rep_num = ?", new Object[] { plannedActivityId, labelId, repetitionNumber });
        if(labels.size() == 0) {
            return null;
        }
        return labels.get(0);
    }

    /**
    * Deletes a plannedActivityLabel
    *
    * @param  plannedActivityLabel to delete
    */
    public void delete(PlannedActivityLabel plannedActivityLabel) {
        getHibernateTemplate().delete(plannedActivityLabel);
    }


    /**
     * Deletes a plannedActivityLabel by labelId
     *
     * @param  labelId to delete
     */
     public void deleteByLabelId(Integer labelId) {
        List<PlannedActivityLabel> palList = getByLabelId(labelId);
        for (int i =0; i < palList.size(); i++) {
            PlannedActivityLabel pal = palList.get(i);
            delete(pal);
        }
     }

}
