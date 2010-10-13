package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
public class DomainContext {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Study study;
    private PlannedCalendar plannedCalendar;
    private Epoch epoch;
    private StudySegment studySegment;
    private Period period;
    private PlannedActivity plannedActivity;

    private Subject subject;
    private StudySubjectAssignment studySubjectAssignment;
    private ScheduledCalendar scheduledCalendar;
    private ScheduledStudySegment scheduledStudySegment;
    private ScheduledActivity scheduledActivity;

    private Site site;
    private StudySite studySite;
    private Activity activity;
    private Amendment amendment;
    private Population population;

    private TemplateService templateService;

    private BeanWrapper selfWrapper;

    public DomainContext(TemplateService templateService) {
        this.templateService = templateService;
        selfWrapper = new BeanWrapperImpl(this);
    }

    public static DomainContext create(DomainObject basis, TemplateService templateService) {
        DomainContext context = new DomainContext(templateService);
        if (basis != null) {
            Method[] methods = DomainContext.class.getMethods();
            for (Method method : methods) {
                if (method.getParameterTypes().length == 1) {
                    if (method.getParameterTypes()[0].isAssignableFrom(basis.getClass())) {
                        try {
                            method.invoke(context, basis);
                        } catch (IllegalAccessException e) {
                            throw new StudyCalendarSystemException("Can not invoke method with context " + context.toString() + " with parametere " + basis.getClass().getName(), e);
                        } catch (InvocationTargetException e) {
                            throw new StudyCalendarSystemException("Can not invoke method with context " + context.toString() + " with parametere " + basis.getClass().getName(), e);
                        }
                    }
                }
            }
        }
        return context;
    }

    public Object getProperty(String path) {
        try {
            return selfWrapper.getPropertyValue(path);
        } catch (BeansException beansException) {
            log.debug("Could not resolve " + path + " in DomainContext", beansException);
            return null;
        }
    }

    ////// SETTERS

    public void setSite(Site site) {
        this.site = site;
    }

    public void setStudySite(StudySite studySite) {
        if (studySite == null) return;
        setSite(studySite.getSite());
        setStudy(studySite.getStudy());
        this.studySite = studySite;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        if (plannedCalendar == null) return;
        setStudy(plannedCalendar.getStudy());
        this.plannedCalendar = plannedCalendar;
    }

    public void setEpoch(Epoch epoch) {
        if (epoch == null) return;
        setPlannedCalendar(templateService.findParent(epoch));
        this.epoch = epoch;
    }

    public void setStudySegment(StudySegment studySegment) {
        if (studySegment == null) return;
        setEpoch(templateService.findParent(studySegment));
        this.studySegment = studySegment;
    }

    public void setPeriod(Period period) {
        if (period == null) return;
        setStudySegment(templateService.findParent(period));
        this.period = period;
    }

    public void setPlannedActivity(PlannedActivity plannedActivity) {
        if (plannedActivity != null) {
            setPeriod(templateService.findParent(plannedActivity));
        }
        this.plannedActivity = plannedActivity;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setStudySubjectAssignment(StudySubjectAssignment studySubjectAssignment) {
        if (studySubjectAssignment == null) return;
        setSubject(studySubjectAssignment.getSubject());
        if (studySubjectAssignment.getStudySite() != null) {
            setPlannedCalendar(studySubjectAssignment.getStudySite().getStudy().getPlannedCalendar());
            setSite(studySubjectAssignment.getStudySite().getSite());
        }
        this.studySubjectAssignment = studySubjectAssignment;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        if (scheduledCalendar == null) return;
        setStudySubjectAssignment(scheduledCalendar.getAssignment());
        this.scheduledCalendar = scheduledCalendar;
    }

    public void setScheduledStudySegment(ScheduledStudySegment scheduledStudySegment) {
        if (scheduledStudySegment == null) return;
        setScheduledCalendar(scheduledStudySegment.getScheduledCalendar());
        setStudySegment(scheduledStudySegment.getStudySegment());
        this.scheduledStudySegment = scheduledStudySegment;
    }

    public void setScheduledActivity(ScheduledActivity scheduledActivity) {
        if (scheduledActivity == null) return;
        setScheduledStudySegment(scheduledActivity.getScheduledStudySegment());
        setPlannedActivity(scheduledActivity.getPlannedActivity());
        this.scheduledActivity = scheduledActivity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    // TODO: more setters, as needed

    ////// BEAN PROPERTIES

    public Site getSite() {
        return site;
    }

    public StudySite getStudySite() {
        return studySite;
    }

    public Study getStudy() {
        return study;
    }

    public PlannedCalendar getPlannedCalendar() {
        return plannedCalendar;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public Period getPeriod() {
        return period;
    }

    public PlannedActivity getPlannedActivity() {
        return plannedActivity;
    }

    public Subject getSubject() {
        return subject;
    }

    public StudySubjectAssignment getStudySubjectAssignment() {
        return studySubjectAssignment;
    }

    public ScheduledCalendar getScheduledCalendar() {
        return scheduledCalendar;
    }

    public ScheduledStudySegment getScheduledStudySegment() {
        return scheduledStudySegment;
    }

    public ScheduledActivity getScheduledActivity() {
        return scheduledActivity;
    }

    public Activity getActivity() {
        return activity;
    }

    public Amendment getAmendment() {
        return amendment;
    }

    public Population getPopulation() {
        return population;
    }
}
