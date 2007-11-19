package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbContext {
    private Study study;
    private PlannedCalendar plannedCalendar;
    private Epoch epoch;
    private Arm arm;
    private Period period;
    private PlannedActivity plannedActivity;

    private Subject subject;
    private StudySubjectAssignment studySubjectAssignment;
    private ScheduledCalendar scheduledCalendar;
    private ScheduledArm scheduledArm;
    private ScheduledActivity scheduledActivity;

    private Site site;
    private StudySite studySite;
    private Activity activity;
    private User user;
    private Amendment amendment;

    private TemplateService templateService;

    public BreadcrumbContext(TemplateService templateService) {
        this.templateService = templateService;
    }

    public static BreadcrumbContext create(DomainObject basis, TemplateService templateService) {
        BreadcrumbContext context = new BreadcrumbContext(templateService);
        if (basis != null) {
            StringBuilder propertyName = new StringBuilder(basis.getClass().getSimpleName());
            propertyName.setCharAt(0, Character.toLowerCase(propertyName.charAt(0)));
            try {
                PropertyUtils.setProperty(context, propertyName.toString(), basis);
            } catch (NoSuchMethodException e) {
                throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
            } catch (IllegalAccessException e) {
                throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
            } catch (InvocationTargetException e) {
                throw new StudyCalendarSystemException("No setter for " + propertyName.toString() + " of type " + basis.getClass().getName(), e);
            }
        }
        return context;
    }

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

    public void setArm(Arm arm) {
        if (arm == null) return;
        setEpoch(templateService.findParent(arm));
        this.arm = arm;
    }

    public void setPeriod(Period period) {
        if (period == null) return;
        setArm(templateService.findParent(period));
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
        setPlannedCalendar(studySubjectAssignment.getStudySite().getStudy().getPlannedCalendar());
        this.studySubjectAssignment = studySubjectAssignment;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        if (scheduledCalendar == null) return;
        setStudySubjectAssignment(scheduledCalendar.getAssignment());
        this.scheduledCalendar = scheduledCalendar;
    }

    public void setScheduledArm(ScheduledArm scheduledArm) {
        if (scheduledArm == null) return;
        setScheduledCalendar(scheduledArm.getScheduledCalendar());
        setArm(scheduledArm.getArm());
        this.scheduledArm = scheduledArm;
    }

    public void setScheduledActivity(ScheduledActivity scheduledActivity) {
        if (scheduledActivity == null) return;
        setScheduledArm(scheduledActivity.getScheduledArm());
        setPlannedActivity(scheduledActivity.getPlannedActivity());
        this.scheduledActivity = scheduledActivity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
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

    public Arm getArm() {
        return arm;
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

    public ScheduledArm getScheduledArm() {
        return scheduledArm;
    }

    public ScheduledActivity getScheduledActivity() {
        return scheduledActivity;
    }

    public Activity getActivity() {
        return activity;
    }

    public User getUser() {
        return user;
    }

    public Amendment getAmendment() {
        return amendment;
    }
}
