package edu.northwestern.bioinformatics.studycalendar.test;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.delta.MemoryOnlyMutatorFactory;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Fixture creation methods which depend on logic from the service layer,
 * rather than merely that expressed in the domain objects themselves.
 *
 * @author Rhett Sutphin
 */
public class ServicedFixtures extends Fixtures {
    private static final Logger log = LoggerFactory.getLogger(ServicedFixtures.class);
    private static final DeltaService deltaService = new DeltaService();
    private static final AmendmentService amendmentService = new AmendmentService();

    static {
        deltaService.setMutatorFactory(new MemoryOnlyMutatorFactory());
        deltaService.setTemplateService(new TestingTemplateService());
        StaticNowFactory nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(DateTools.createTimestamp(2000, Calendar.MARCH, 9));
        deltaService.setNowFactory(nowFactory);

        amendmentService.setDeltaService(deltaService);
        amendmentService.setStudyService(new StudyService() {
            @Override public void save(Study study) { /* No-op */ }
        });
    }

    public static Study createBlankTemplate() {
        return createApprovedTemplate(TemplateSkeletonCreator.BLANK);
    }

    public static Study createBasicTemplate() {
        return createBasicTemplate(null);
    }

    public static Study createBasicTemplate(String name) {
        Study study = createApprovedTemplate(TemplateSkeletonCreator.BASIC);
        study.setAssignedIdentifier(name);
        return study;
    }

    public static Study createInDevelopmentBasicTemplate(String name) {
        return TemplateSkeletonCreator.BASIC.create(name);
    }

    private static Study createApprovedTemplate(TemplateSkeletonCreator skeletonCreator) {
        log.debug("Creating concrete template from skeleton");
        Study dev = skeletonCreator.create(null);
        amendmentService.amend(dev);
        return dev;
    }

    /**
     * A fixture-compatible version of AmendmentService#amend
     */
    public static void amend(Study study) {
        amendmentService.amend(study);
    }

    public static Study revise(Study study, Revision revision) {
        return deltaService.revise(study, revision);
    }

    public static DeltaService getTestingDeltaService() {
        return deltaService;
    }

    // static class
    protected ServicedFixtures() { super(); }
}
