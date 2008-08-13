package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.LabelDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityLabelDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Label;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.web.template.EditCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nshurupova
 */
public abstract class EditPeriodEventsCommand implements EditCommand {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Period period;
    protected PlannedActivityDao plannedActivityDao;
    protected AmendmentService amendmentService;
    protected StudyService studyService;

    private DaoFinder daoFinder;
    private Study study;

    private Activity activity;
    private List<Integer> plannedActivities = new ArrayList<Integer>();

    private String details;
    private String conditionalDetails;
    private int rowNumber;
    private int columnNumber;

    protected LabelDao labelDao;
    protected PlannedActivityLabelDao plannedActivityLabelDao;
    private String label;

    public Map<String, Object> getModel() {
        Map<String, Object> model = getLocalModel();
        model.put("developmentRevision", getStudy().getDevelopmentAmendment());
        model.put("revisionChanges",
            new RevisionChanges(daoFinder, getStudy().getDevelopmentAmendment(), getStudy(), getPeriod()));
        return model;
    }

    /**
     * Template method for providing objects to the view
     */
    public abstract Map<String, Object> getLocalModel();

    /**
     * Template method that performs the actual work of the command
     */
    protected abstract void performEdit();

    public abstract String getRelativeViewName();

    /**
     * Apply any changes in the grid to the period in the command.
     */
    public boolean apply() {
        log.debug("inside EditPeriodEventsCommand");
        performEdit();
        setStudy(studyService.saveStudyFor(getPeriod()));
        return true;
    }

    ////// BOUND PROPERTIES

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Integer> getPlannedActivities() {
        return plannedActivities;
    }

    public void setPlannedActivities(List<Integer> plannedActivities) {
        this.plannedActivities = plannedActivities;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getConditionalDetails() {
        return conditionalDetails;
    }

    public void setConditionalDetails(String conditionalDetails) {
        this.conditionalDetails = conditionalDetails;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    ////// CONFIGURATION

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }


    public LabelDao getLabelDao() {
        return labelDao;
    }

    public void setLabelDao(LabelDao labelDao) {
        this.labelDao = labelDao;
    }


    public PlannedActivityLabelDao getPlannedActivityLabelDao() {
        return plannedActivityLabelDao;
    }

    public void setPlannedActivityLabelDao(PlannedActivityLabelDao plannedActivityLabelDao) {
        this.plannedActivityLabelDao = plannedActivityLabelDao;
    }
}
