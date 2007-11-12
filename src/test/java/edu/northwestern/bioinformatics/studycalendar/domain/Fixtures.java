package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

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
        int epochId = 10, armId = 100, periodId = 1000, plannedActivityId = 10000;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(++epochId);
            for (Arm arm : epoch.getArms()) {
                arm.setId(++armId);
                for (Period period : arm.getPeriods()) {
                    period.setId(++periodId);
                    for (PlannedActivity event : period.getPlannedActivities()) {
                        event.setId(++plannedActivityId);
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

    public static Study createSingleEpochStudy(String studyName, String epochName, String... armNames) {
        Study study = createNamedInstance(studyName, Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().addEpoch(Epoch.create(epochName, armNames));
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

    public static Participant createParticipant(String firstName, String lastName) {
        Participant p = new Participant();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    public static StudyParticipantAssignment createAssignment(Study study, Site site, Participant participant) {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        StudySite ss = createStudySite(study, site);
        assignment.setStudySite(ss);
        assignment.setParticipant(participant);
        assignment.setScheduledCalendar(new ScheduledCalendar());
        return assignment;
    }

    public static ScheduledArm createScheduledArm(Arm arm) {
        ScheduledArm scheduledArm = new ScheduledArm();
        scheduledArm.setArm(arm);
        scheduledArm.setStartDay(1);
        scheduledArm.setStartDate(DateTools.createDate(1997, Calendar.JANUARY, 12));
        return scheduledArm;
    }

    public static ScheduledActivity createScheduledEvent(
        String activityName, int year, int month, int day, ScheduledEventState state
    ) {
        ScheduledActivity event = createScheduledEvent(activityName, year, month, day);
        event.changeState(state);
        return event;
    }

    public static ScheduledActivity createScheduledEvent(String activityName, int year, int month, int day) {
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


    public static void addEvents(ScheduledArm scheduledArm, ScheduledActivity... events) {
        for (ScheduledActivity event : events) {
            scheduledArm.addEvent(event);
        }
    }

    public static User createUser(Integer id, String name, Long csmUserId, boolean activeFlag, Role... roles) throws Exception {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setCsmUserId(csmUserId);
        user.setActiveFlag(activeFlag);
        if (roles != null) {
            user.setUserRoles(new HashSet<UserRole>());
            for(Role role : roles) {
                UserRole userRole = new UserRole(user, role);
                user.getUserRoles().add(userRole);
            }
        }
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

    /**
     * Creates a chain of amendments with the given names, returning the one at the end of the
     * chain (the most recent one).
     */
    public static Amendment createAmendments(String... nameHistory) {
        Amendment current = new Amendment();
        for (int i = 0; i < nameHistory.length - 1; i++) {
            String name = nameHistory[i];
            current.setName(name);
            Amendment next = new Amendment();
            next.setPreviousAmendment(current);
            current = next;
        }
        current.setName(nameHistory[nameHistory.length - 1]);
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
