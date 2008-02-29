package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John Dzak
 */
public class TemplateXmlSerializerPostProcessor {
    private TemplateService templateService;
    private ActivityDao activityDao;
    private SourceDao sourceDao;
    private StudyDao studyDao;
    private AmendmentService amendmentService;
    private StudyService studyService;
    private DaoFinder daoFinder;

    public void resolveExistingActivitiesAndSources(Study study) {
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
                        PlanTreeNode<?> child = ((Add) change).getChild();
                        // Need this if change is already persisted in the database, then it won't have a child
                        // and we need to find from the child id
                        if (child == null) {
                            child = findChangeChild(change);
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

    public void resolveChangeChildrenFromPlanTreeNodeTree(Study study) {
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

        /**
         * When a change is persisted to the database, when retreived the child element is null.  We must retrieve it
         * using the child id.
         */
        private PlanTreeNode<?> findChangeChild(Change change) {
            Integer childId = ((ChildrenChange)change).getChildId();
            Class<? extends PlanTreeNode> childClass = ((PlanTreeInnerNode) change.getDelta().getNode()).childClass();

            DomainObjectDao dao = daoFinder.findDao(childClass);
            PlanTreeNode<?> child = (PlanTreeNode<?>) dao.getById(childId);
            if (child == null) {
                throw new StudyCalendarSystemException("Problem importing template. Child with class %s and id %s could not be found",
                        childClass.getName(), childId.toString());
            }
            return child;
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


    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
