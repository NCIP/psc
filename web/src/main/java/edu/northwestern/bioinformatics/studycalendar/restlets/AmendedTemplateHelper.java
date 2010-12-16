package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateHelper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Map<Class<?>, UriTemplateParameters> PARAMS_FOR_NODE_TYPES
        = new HashMap<Class<?>, UriTemplateParameters>();
    static {
        PARAMS_FOR_NODE_TYPES.put(Epoch.class, UriTemplateParameters.EPOCH_NAME);
        PARAMS_FOR_NODE_TYPES.put(StudySegment.class, UriTemplateParameters.STUDY_SEGMENT_NAME);
        PARAMS_FOR_NODE_TYPES.put(Period.class, UriTemplateParameters.PERIOD_IDENTIFIER);
        PARAMS_FOR_NODE_TYPES.put(PlannedActivity.class, UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER);
    }

    public static final String CURRENT = "current";
    public static final String DEVELOPMENT = "development";

    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private AmendmentDao amendmentDao;

    private Request request;
    private Study originalStudy, amendedStudy;

    public Study getAmendedTemplate() throws NotFound {
        if (amendedStudy != null) return amendedStudy;

        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(getRequest());
        if (StringUtils.isBlank(studyIdentifier)) {
            throw new NotFound("No study specified");
        }

        originalStudy = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (originalStudy == null) {
            throw new NotFound("No study matching %s", studyIdentifier);
        }

        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(getRequest());
        if (StringUtils.isBlank(amendmentIdentifier)) {
            throw new NotFound("No amendment specified");
        } else if (AmendedTemplateHelper.DEVELOPMENT.equals(amendmentIdentifier)) {
            amendedStudy = applyDevelopmentAmendment();
        } else {
            amendedStudy = applyReleasedAmendment(amendmentIdentifier);
        }
        return amendedStudy;
    }

    public Study getAmendedTemplateOrNull() {
        try {
            return getAmendedTemplate();
        } catch (NotFound e) {
            return null;
        }
    }

    public Study getRealStudy() {
        if (originalStudy == null) getAmendedTemplate(); // for side effects
        return originalStudy;
    }

    public boolean isDevelopmentRequest() {
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(getRequest());
        return AmendedTemplateHelper.DEVELOPMENT.equals(amendmentIdentifier);
    }

    public Collection<ResourceAuthorization> getReadAuthorizations() {
        Study study;
        try {
            study = getAmendedTemplate();
        } catch (NotFound nf) {
            study = null;
        }
        if (isDevelopmentRequest()) {
            return ResourceAuthorization.createTemplateManagementAuthorizations(study);
        } else {
            return ResourceAuthorization.createAllStudyAuthorizations(study);
        }
    }

    private Study applyDevelopmentAmendment() {
        log.debug("Applying dev amendment to {}", originalStudy.getAssignedIdentifier());
        if (originalStudy.getDevelopmentAmendment() == null) {
            log.debug(" - No dev amendment");
            throw new NotFound("Study template %s is not in development",
                originalStudy.getAssignedIdentifier());
        }
        return deltaService.revise(originalStudy, originalStudy.getDevelopmentAmendment());
    }

    private Study applyReleasedAmendment(String amendmentIdentifier) {
        Amendment amendment;
        if (AmendedTemplateHelper.CURRENT.equals(amendmentIdentifier)) {
            log.debug("Using current version of {}", originalStudy.getAssignedIdentifier());
            amendment = originalStudy.getAmendment();
            if (amendment == null) {
                throw new NotFound(
                    "%s has never been released, so it has no current version.  Try 'development' instead of 'current'.",
                    originalStudy.getAssignedIdentifier());
            }
        } else {
            log.debug("Looking for {} in {}", amendmentIdentifier, originalStudy.getAssignedIdentifier());
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier, originalStudy);
            if (amendment != null && !amendment.equals(originalStudy.getAmendment()) && !originalStudy.getAmendment().hasPreviousAmendment(amendment)) {
                throw new NotFound("The amendment %s is not part of %s",
                    amendmentIdentifier, originalStudy.getAssignedIdentifier());
            } else if (amendment == null) {
                throw new NotFound("No released amendment matching %s for %s",
                    amendmentIdentifier, originalStudy.getAssignedIdentifier());
            }
        }
        log.debug("Amending {} with {}", originalStudy.getAssignedIdentifier(), amendment.getNaturalKey());
        return amendmentService.getAmendedStudy(originalStudy, amendment);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public <N extends PlanTreeNode> N drillDown(Class<N> targetClass) {
        return drillDown(getAmendedTemplate().getPlannedCalendar(), targetClass);
    }

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    private <N extends PlanTreeNode> N drillDown(PlanTreeNode from, Class<N> targetClass) {
        if (targetClass.isAssignableFrom(from.getClass())) return (N) from;
        if (!PlanTreeInnerNode.class.isAssignableFrom(from.getClass())) {
            log.debug("Reached tree leaves without finding desired node");
            return null;
        }
        PlanTreeInnerNode innerNode = (PlanTreeInnerNode) from;
        UriTemplateParameters param = PARAMS_FOR_NODE_TYPES.get(innerNode.childClass());
        String key = param.extractFrom(getRequest());
        PlanTreeNode next = innerNode.findNaturallyMatchingChild(key);
        if (next == null) {
            throw new NotFound("No %s identified by '%s' in %s",
                StringTools.humanizeClassName(innerNode.childClass().getSimpleName()),
                key,
                StringTools.humanizeClassName(from.getClass().getSimpleName()));
        } else {
            return drillDown(next, targetClass);
        }
    }

    private Request getRequest() {
        if (request == null) throw new IllegalStateException("Please set request before invoking any helper methods");
        return request;
    }

    ////// CONFIGURATION

    // Should not be wired by spring
    public void setRequest(Request request) {
        this.request = request;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    //////

    public static class NotFound extends StudyCalendarUserException {
        public NotFound(String message, Object... messageParameters) {
            super(message, messageParameters);
        }
    }
}