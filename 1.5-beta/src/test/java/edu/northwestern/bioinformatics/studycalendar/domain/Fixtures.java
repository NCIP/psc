package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MemoryOnlyMutatorFactory;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class Fixtures {
    private static final Logger log = LoggerFactory.getLogger(Fixtures.class);
    private static final DeltaService deltaService = new DeltaService();
    static {
        deltaService.setMutatorFactory(new MemoryOnlyMutatorFactory());
        deltaService.setTemplateService(new TestingTemplateService());
    }

    public static <T extends DomainObject> T setId(Integer id, T target) {
        target.setId(id);
        return target;
    }

    public static void assignIds(Study study) {
        study.getPlannedCalendar().setId(1);
        study.getPlannedCalendar().setGridId("GRID-1");
        int epochId = 10, studySegmentId = 100, periodId = 1000, plannedActivityId = 10000;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(++epochId);
            epoch.setGridId("GRID-" + epochId);
            for (StudySegment studySegment : epoch.getStudySegments()) {
                studySegment.setId(++studySegmentId);
                studySegment.setGridId("GRID-" + studySegmentId);
                for (Period period : studySegment.getPeriods()) {
                    period.setId(++periodId);
                    period.setGridId("GRID-" + periodId);
                    for (PlannedActivity event : period.getPlannedActivities()) {
                        event.setId(++plannedActivityId);
                        event.setGridId("GRID-" + plannedActivityId);
                    }
                }
            }
        }
    }

    public static <T extends GridIdentifiable> T setGridId(String gridId, T target) {
        target.setGridId(gridId);
        return target;
    }

    public static Period createPeriod(String name, int startDay, int dayCount, int repetitions) {
        return createPeriod(name, startDay, Duration.Unit.day, dayCount, repetitions);
    }

    public static Period createPeriod(String name, int startDay, Duration.Unit dUnit, int dQuantity, int repetitions) {
        Period p = new Period();
        p.setName(name);
        p.setStartDay(startDay);
        p.getDuration().setUnit(dUnit);
        p.getDuration().setQuantity(dQuantity);
        p.setRepetitions(repetitions);
        return p;
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day) {
        PlannedActivity event = new PlannedActivity();
        Activity activity = createNamedInstance(activityName, Activity.class);
        activity.setType(ActivityType.LAB_TEST);
        event.setActivity(activity);
        event.setDay(day);
        return event;
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day, String details) {
        PlannedActivity event = createPlannedActivity(activityName, day);
        event.setDetails(details);
        return event;
    }

    public static PlannedActivity createPlannedActivity(String activityName, int day, String details,String condition) {
        PlannedActivity event = createPlannedActivity(activityName, day, details);
        event.setCondition(condition);
        return event;
    }

    public static Study createSingleEpochStudy(String studyName, String epochName, String... studySegmentNames) {
        Study study = createNamedInstance(studyName, Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().addEpoch(Epoch.create(epochName, studySegmentNames));
        return study;
    }

    public static Study createBlankTemplate() {
        return createApprovedTemplate(TemplateSkeletonCreator.BLANK);
    }

    public static Study createBasicTemplate() {
        return createApprovedTemplate(TemplateSkeletonCreator.BASIC);
    }

    private static Study createApprovedTemplate(TemplateSkeletonCreator skeletonCreator) {
        log.debug("Creating concrete template from skeleton");
        Study dev = skeletonCreator.create();
        deltaService.setMutatorFactory(new MemoryOnlyMutatorFactory());
        // This is a partial implementation of DeltaService#amend 
        deltaService.apply(dev, dev.getDevelopmentAmendment());
        dev.setAmendment(dev.getDevelopmentAmendment());
        dev.setDevelopmentAmendment(null);
        return dev;
    }

    public static Study revise(Study study, Revision revision) {
        return deltaService.revise(study, revision);
    }

    public static DeltaService getTestingDeltaService() {
        return deltaService;
    }

    public static StudySite createStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) study.addStudySite(studySite);
        if (site != null) site.addStudySite(studySite);
        return studySite;
    }

    public static Subject createSubject(String firstName, String lastName) {
        Subject p = new Subject();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    public static StudySubjectAssignment createAssignment(Study study, Site site, Subject subject) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        StudySite ss = createStudySite(study, site);
        assignment.setStudySite(ss);
        assignment.setSubject(subject);
        assignment.setScheduledCalendar(new ScheduledCalendar());
        return assignment;
    }

    public static ScheduledStudySegment createScheduledStudySegment(StudySegment studySegment) {
        ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
        scheduledStudySegment.setStudySegment(studySegment);
        scheduledStudySegment.setStartDay(1);
        scheduledStudySegment.setStartDate(DateTools.createDate(1997, Calendar.JANUARY, 12));
        return scheduledStudySegment;
    }

    public static ScheduledActivity createScheduledActivity(
        String activityName, int year, int month, int day, ScheduledActivityState state
    ) {
        ScheduledActivity event = createScheduledActivity(activityName, year, month, day);
        event.changeState(state);
        return event;
    }

    public static ScheduledActivity createScheduledActivity(String activityName, int year, int month, int day) {
        PlannedActivity baseEvent = createPlannedActivity(activityName, 0);
        ScheduledActivity event = new ScheduledActivity();
        event.setPlannedActivity(baseEvent);
        event.setActivity(createActivity(activityName));
        event.setIdealDate(DateUtils.createDate(year, month, day - 2));
        event.changeState(new Scheduled(null, DateUtils.createDate(year, month, day)));
        return event;
    }

    public static ScheduledActivity createConditionalEvent(String activityName, int year, int month, int day) {
        PlannedActivity baseEvent = createPlannedActivity(activityName, 0);
        baseEvent.setCondition("Details");
        ScheduledActivity event = new ScheduledActivity();
        event.setPlannedActivity(baseEvent);
        event.setActivity(createActivity(activityName));
        event.setIdealDate(DateUtils.createDate(year, month, day - 2));
        event.changeState(new Conditional(null, DateUtils.createDate(year, month, day)));
        return event;
    }

    public static Activity createActivity(String name) {
        Activity activity = createNamedInstance(name, Activity.class);
        activity.setType(ActivityType.LAB_TEST);
        return activity;
    }

    public static Activity createActivity(String name, String code, Source source, ActivityType type) {
        Activity activity = createActivity(name);
        activity.setCode(code);
        activity.setType(type);
        if (source != null) {
            source.addActivity(activity);
        }
        return activity;
    }

    public static Activity createActivity(String name, String code, Source source, ActivityType type, String description) {
        Activity activity = createActivity(name, code, source, type);
        activity.setDescription(description);
        return activity;
    }


    public static void addEvents(ScheduledStudySegment scheduledStudySegment, ScheduledActivity... events) {
        for (ScheduledActivity event : events) {
            scheduledStudySegment.addEvent(event);
        }
    }

    public static void setUserRoles(User user, Role... roles) {
        user.clearUserRoles();
        for (Role role : roles) {
            UserRole userRole = new UserRole(user, role);
            user.getUserRoles().add(userRole);
        }
    }

    public static User createUser(String name, Role... roles) {
        return createUser(null, name, null, true, roles);
    }

    public static User createUser(Integer id, String name, Long csmUserId, boolean activeFlag, Role... roles) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCsmUserId(csmUserId);
        user.setActiveFlag(activeFlag);
        setUserRoles(user, roles);
        return user;
    }

    public static UserRole createUserRole(User user, Role role, Site... sites) {
        UserRole userRole = new UserRole(user, role, sites);
        user.addUserRole(userRole);
        return userRole;
    }

    public static ProtectionGroup createProtectionGroup(Long aId, String aName){
        ProtectionGroup myProtectionGroup = new ProtectionGroup();
        myProtectionGroup.setProtectionGroupName(aName);
        myProtectionGroup.setProtectionGroupId(aId);
        return myProtectionGroup;
    }

    public static <T extends Named> T createNamedInstance(String name, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            instance.setName(name);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        }
    }

    public static Population createPopulation(String abbreviation, String name) {
        Population population = new Population();
        population.setAbbreviation(abbreviation);
        population.setName(name);
        return population;
    }

    public static Amendment createAmendment(String name, Date date, boolean mandatory) {
        Amendment amendment =  new Amendment();
        amendment.setMandatory(mandatory);
        amendment.setName(name);
        amendment.setDate(date);
        return amendment;
    }

    /**
     * Creates a chain of amendments with the given names, returning the one at the end of the
     * chain (the most recent one).
     */
    public static Amendment createAmendments(String... nameHistory) {
        Amendment current = new Amendment();
        Calendar cal = Calendar.getInstance();
        cal.set(2001, Calendar.JANUARY, 1);
        for (int i = 0; i < nameHistory.length - 1; i++) {
            String name = nameHistory[i];
            current.setName(name);
            current.setDate(cal.getTime());
            Amendment next = new Amendment();
            next.setPreviousAmendment(current);
            current = next;
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        current.setName(nameHistory[nameHistory.length - 1]);
        return current;
    }

    /**
     * Creates a chain of amendments with the given dates, returning the one at the end of the
     * chain (the most recent one).
     */
    public static Amendment createAmendments(Date... dateHistory) {
        Amendment current = new Amendment();
        for (int i = 0; i < dateHistory.length - 1; i++) {
            Date date = dateHistory[i];
            current.setDate(date);
            Amendment next = new Amendment();
            next.setPreviousAmendment(current);
            current = next;
        }
        current.setDate(dateHistory[dateHistory.length - 1]);
        return current;
    }

    public static Add createAddChange(Integer newChildId, Integer index) {
        Add add = new Add();
        add.setChildId(newChildId);
        add.setIndex(index);
        return add;
    }

    // static class
    private Fixtures() { }
}
