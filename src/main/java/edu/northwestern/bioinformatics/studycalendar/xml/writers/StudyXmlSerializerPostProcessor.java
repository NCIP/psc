package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John Dzak
 */
public class StudyXmlSerializerPostProcessor {
    private TemplateService templateService;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private StudyService studyService;
    private DaoFinder daoFinder;
    private DeltaService deltaService;

    public void process(Study study) {
        resolveExistingActivitiesAndSources(study);
        resolveChangeChildrenFromPlanTreeNodeTree(study);
    }
    
    protected void resolveExistingActivitiesAndSources(Study study) {
        List<PlannedActivity> all = new LinkedList<PlannedActivity>();


        List<Amendment> reverse = new LinkedList<Amendment>(study.getAmendmentsList());
        Collections.reverse(reverse);
        if (study.getDevelopmentAmendment() != null) {
            reverse.add(study.getDevelopmentAmendment());
        }
        for (Amendment amendment : reverse) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change.getAction() == ChangeAction.ADD) {
                        PlanTreeNode<?> child = deltaService.findChangeChild((Add) change);
                        if (child == null) {
                            throw new StudyCalendarSystemException(
                                "Could not resolve child for %s", change);
                        }

                        if (child instanceof PlannedActivity) {
                            all.add((PlannedActivity) child);
                        } else {
                            all.addAll(templateService.findChildren((PlanTreeInnerNode) child, PlannedActivity.class));
                        }
                    }
                }
            }
        }
        Amendment dev = study.getDevelopmentAmendment();
        Amendment cur = study.getAmendment();
        study.setAmendment(null);
        study.setDevelopmentAmendment(null);
        for (PlannedActivity plannedActivity : all) {
            Activity activity = plannedActivity.getActivity();

            Activity existingActivity = activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName());
            if (existingActivity != null) {
                plannedActivity.setActivity(existingActivity);
            } else {
                Source existingSource = sourceDao.getByName(activity.getSource().getName());
                if (existingSource != null) {
                    activity.setSource(existingSource);
                }
            }

            sourceDao.save(plannedActivity.getActivity().getSource());
            activityDao.save(plannedActivity.getActivity());
        }
        study.setAmendment(cur);
        study.setDevelopmentAmendment(dev);
    }

    protected void resolveChangeChildrenFromPlanTreeNodeTree(Study study) {
        List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
        Amendment development = study.getDevelopmentAmendment();
        Collections.reverse(amendments);
        study.setAmendment(null);
        study.setDevelopmentAmendment(null);

        // StudyDao is being used instead of StudyService because we don't want to cascade to the amendments yet
        studyDao.save(study);

        for (Amendment amendment : amendments) {
            // If amendment already exists, we don't want to amend the study with it twice.
            if (amendment.getId() == null) {
                resolveDeltaNodesAndChangeChildren(amendment);

                study.setDevelopmentAmendment(amendment);
                study.setAmendment(amendment.getPreviousAmendment());
                amendmentService.amend(study);
            }
        }

        // Resolve delta nodes and child nodes for development amendment
        if (development != null) {
            resolveDeltaNodesAndChangeChildren(development);
        }
        study.setDevelopmentAmendment(development);
        studyService.save(study);

    }

    /**
     * When the study gets de-serialized, references to new PlanTreeNode(s) cannot be found since the whole
     * template isn't saved until the end. Therefore, after the template is de-serialized, we have to go
     * and resolve the PlanTreeNode(s) to their correct references.
     *
     * e.g. A Template that has two amendments is being imported.  The first amendment adds a new Epoch. The second
     *      amendment changes the name of that Epoch.  When the second amendment is de-serialized, the Delta node will have
     *      a template Epoch (an epoch with the same grid id as the actual one) as a place holder for the actual Epoch
     *      until it is resolved.
     */
    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }

    private void resolveDeltaNodesAndChangeChildren(Amendment amendment) {
        for (Delta delta : amendment.getDeltas()) {
            // resolve node
            PlanTreeNode<?> deltaNode = findRealNode(delta.getNode());
            if (deltaNode != null) {
                delta.setNode(deltaNode);
            } else {
                throw new IllegalStateException("Delta " + delta.getGridId() + " references unknown node " + delta.getNode().getGridId());
            }

            // resolve child nodes
            for (Object oChange : delta.getChanges()) {
                if (oChange instanceof ChildrenChange) {
                    ChildrenChange change = (ChildrenChange) oChange;
                    PlanTreeNode<?> nodeTemplate = change.getChild();

                    // If nodeTemplate is not null, element has yet to be persisted (because we setChild in serializer).
                    // So since we have a template for the node, we want to find the actual node.
                    if (nodeTemplate != null) {
                        PlanTreeNode<?> node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        }
                    }
                }
            }
        }
    }

    ////// Bean Setters
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
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
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
