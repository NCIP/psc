package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.LinkedList;

import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;

@Transactional
public class ImportTemplateService {
    private StudyXmlSerializer studyXmlSerializer;
    private ActivityDao activityDao;
    private StudyService studyService;
    private SourceDao sourceDao;
    private DeltaService deltaService;
    private StudyDao studyDao;
    private DaoFinder daoFinder;
    private AmendmentService amendmentService;
    private TemplateService templateService;

    public void importTemplate (InputStream stream) {
        Study study = studyXmlSerializer.readDocument(stream);

        resolveExistingActivitiesAndSources(study);
        List<Amendment> amendments = new ArrayList<Amendment>(study.getAmendmentsList());
        Collections.reverse(amendments);
        study.setAmendment(null);

        // StudyDao is being used instead of StudyService because we don't want to cascade to the amendments yet
        studyDao.save(study);

        for (Amendment amendment : amendments) {
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
                        PlanTreeNode<?> node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        }
                    }
                }
            }
            study.setDevelopmentAmendment(amendment);
            amendmentService.amend(study);
        }
    }

    private PlanTreeNode<?> findRealNode(PlanTreeNode<?> nodeTemplate) {
        GridIdentifiableDao dao = (GridIdentifiableDao) daoFinder.findDao(nodeTemplate.getClass());
        return (PlanTreeNode<?>) dao.getByGridId(nodeTemplate.getGridId());
    }

    private void resolveExistingActivitiesAndSources(Study study) {
        List<PlannedActivity> all = new LinkedList<PlannedActivity>();

        for (Amendment amendment : study.getAmendmentsList()) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change.getAction() == ChangeAction.ADD) {
                        PlanTreeNode<?> child = ((Add) change).getChild();
                        if (child instanceof PlannedActivity) {
                            all.add((PlannedActivity) child);
                        } else {
                            all.addAll(templateService.findChildren((PlanTreeInnerNode) child, PlannedActivity.class));
                        }
                    }
                }
            }
        }

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
    }



    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
