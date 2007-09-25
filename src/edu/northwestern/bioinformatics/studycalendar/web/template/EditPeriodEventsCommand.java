package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;

import java.util.*;

import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Sep 14, 2007
 * Time: 12:42:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EditPeriodEventsCommand implements EditCommand<PlannedEvent> {
    private static final Logger log = LoggerFactory.getLogger(EditPeriodEventsCommand.class.getName());

    protected Period period;
    protected PlannedEventDao plannedEventDao;
    protected AmendmentService amendmentService;
    protected StudyService studyService;

    protected PeriodDao periodDao;
    private DaoFinder daoFinder;
    private Study study;

    private int id;
    private Activity activity;
    private List<Integer> eventIds = new ArrayList<Integer>();

    private String details;
    private int rowNumber;
    private int columnNumber;
    private boolean addition;
    private boolean updated;

    private String conditionalDetails;
    private boolean conditionalUpdated;

    private int moveFrom;
    private int moveTo;

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
    protected abstract PlannedEvent performEdit();

    public abstract String getRelativeViewName();


    /**
     * Apply any changes in the grid to the period in the command.
     */
    public PlannedEvent apply() {
        log.info("inside EditPeriodEventsCommand ");
        PlannedEvent event = performEdit();
        setStudy(studyService.saveStudyFor(getPeriod()));
        return event;
    }


    public Period getPeriod() {
        period = getPeriodDao().getById(getId());
        return period;
    }

    public boolean isDetailsUpdated() {
        return getColumnNumber() <0;
    }

    ////// BOUND PROPERTIES

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public List<Integer> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<Integer> eventIds) {
        this.eventIds = eventIds;
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

    public boolean isAddition() {
        return addition;
    }

    public void setAddition(boolean addition) {
        this.addition = addition;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isConditionalCheckbox() {
        if (getConditionalDetails()!=null && getConditionalDetails().length()>0) {
            return true;
        }
        return false;
    }

    public String getConditionalDetails() {
        return conditionalDetails;
    }

    public void setConditionalDetails(String conditionalDetails) {
        this.conditionalDetails = conditionalDetails;
    }


    public boolean isConditionalUpdated() {
        return conditionalUpdated;
    }

    public void setConditionalUpdated(boolean conditionalUpdated) {
        this.conditionalUpdated = conditionalUpdated;
    }


    public PeriodDao getPeriodDao() {
        return periodDao;
    }

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getMoveFrom() {
        return moveFrom;
    }

    public void setMoveFrom(int moveFrom) {
        this.moveFrom = moveFrom;
    }

    public int getMoveTo() {
        return moveTo;
    }

    public void setMoveTo(int moveTo) {
        this.moveTo = moveTo;
    }

    public AmendmentService getAmendmentService() {
        return amendmentService;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }


    public StudyService getStudyService() {
        return studyService;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }


    public PlannedEventDao getPlannedEventDao() {
        return plannedEventDao;
    }

    public void setPlannedEventDao(PlannedEventDao plannedEventDao) {
        this.plannedEventDao = plannedEventDao;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

}
