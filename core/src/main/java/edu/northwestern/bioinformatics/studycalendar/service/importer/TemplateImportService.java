package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyImportException;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateInternalReferenceIndex.*;

import java.io.InputStream;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;

/**
 * @author Jalpa Patel
 */
public class TemplateImportService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private StudyXmlSerializer studyXmlSerializer;
    private StudyDao studyDao;
    private TemplateService templateService;
    private TemplateDevelopmentService templateDevelopmentService;
    private DeltaService deltaService;
    private GridIdentifierResolver gridIdentifierResolver;
    private LocalGridIdentifierCreator localGridIdentifierCreator;
    private ActivityDao activityDao;
    private StudyService studyService;
    private ActivityService activityService;
    private AmendmentService amendmentService;
    private DaoFinder daoFinder;

    /**
     * This method will be called when importing study from within the Import Template Page
     *
     * @param stream
     */
    public void readAndSaveTemplate(InputStream stream) {
        Study newStudy = loadStudyFromXml(stream);
        Study existingStudy = studyDao.getByAssignedIdentifier(newStudy.getAssignedIdentifier());
        beforeSave(newStudy, existingStudy);
        saveStudy(newStudy, existingStudy);
    }

    /**
     * This method will be called from TemplateResource
     * @param existingStudy
     * @param stream
     */
    public Study readAndSaveTemplate(Study existingStudy, InputStream stream) {
        Study newStudy = loadStudyFromXml(stream);
        beforeSave(newStudy, existingStudy);
        Study study = saveStudy(newStudy, existingStudy);
        return study;
    }

    private Study loadStudyFromXml(InputStream stream) {
        Document document = studyXmlSerializer.deserializeDocument(stream);
        Element element = document.getRootElement();
        return studyXmlSerializer.readElement(element, new Study());
    }

    public Study beforeSave(Study newStudy, Study oldStudy) {
        TemplateInternalReferenceIndex oldIndex = new TemplateInternalReferenceIndex();
        if (oldStudy != null) {
            Study fullVersion = studyService.getCompleteTemplateHistory(oldStudy);
            List<Amendment> oldVersionList = new ArrayList<Amendment>(fullVersion.getAmendmentsList());
            int newVersionSize = newStudy.getAmendmentsList().size();
            int oldVersionSize = oldVersionList.size();
             // Detect changes to released amendment
            if (newVersionSize < oldVersionSize) {
                throw new StudyImportException("Imported study doesn't have all released amendment as of existing study");
            }

            List<Amendment> newVersionList = newStudy.getAmendmentsList().subList(newVersionSize -
                                               oldVersionSize, newVersionSize);
            StringBuilder sb = new StringBuilder();
            for (Amendment amendment: oldVersionList) {
                Amendment newVersion = newVersionList.get(oldVersionList.indexOf(amendment));
                Differences differences = amendment.deepEquals(newVersion);
                if (differences.hasDifferences()) {
                    sb.append("Amendment ").append(amendment.getDisplayName()).append(" differs to ")
                            .append("Amendment ").append(newVersion.getDisplayName());
                    sb.append("[").append(differences.toString()).append("]");
                }
            }

            if (sb.length() != 0) {
                throw new StudyImportException(sb.toString());
            }
            // Build TemplateInternalReferenceIndex for released Amendments
            if (fullVersion.getDevelopmentAmendment() != null) {
                oldVersionList.add(fullVersion.getDevelopmentAmendment());
            }
            oldIndex = buildTemplateInternalReferenceIndex(oldVersionList);
            oldIndex.addPlanTreeNode(oldStudy.getPlannedCalendar());
        }

        List<Amendment> amendments = new ArrayList<Amendment>(newStudy.getAmendmentsListInReverseOrder());
        if (newStudy.getDevelopmentAmendment() != null) {
            amendments.add(newStudy.getDevelopmentAmendment());
        }

        //Build TemplateInternalReferenceIndex for xml loaded study.
        TemplateInternalReferenceIndex loadedIndex = buildTemplateInternalReferenceIndex(amendments);
        loadedIndex.addPlanTreeNode(newStudy.getPlannedCalendar());

        for (Key key : oldIndex.getIndex().keySet()) {
            if (loadedIndex.getIndex().containsKey(key)) {
               loadedIndex.getIndex().remove(key);
            }
        }

        List<Key> existingGridIdKeys = new ArrayList<Key>();
        for (Key key : loadedIndex.getIndex().keySet()) {
            Boolean existing = gridIdentifierResolver.resolveGridId(key.kind, key.id);
            if (existing) {
               existingGridIdKeys.add(key);
            }
        }

        if (oldStudy != null && !existingGridIdKeys.isEmpty()) {
           throw new StudyImportException("Study has grid id which already exists in system");
        }

        // Assign new grid ids for conflicted grid ids for new study
        if (oldStudy == null) {
            for (Key key : existingGridIdKeys) {
                String newGridId = localGridIdentifierCreator.getGridIdentifier();
                log.debug("replacing conflicted grid identifier {} with new grid identifier {}", key.id, newGridId);
                Entry entry = loadedIndex.get(key);
                if (entry.getOriginal() != null)
                    ((MutableDomainObject)entry.getOriginal()).setGridId(newGridId);
                for (Delta delta : entry.getReferringDeltas()) {
                    delta.getNode().setGridId(newGridId);
                }
                for (ChildrenChange childrenChange : entry.getReferringChanges()) {
                    childrenChange.getChild().setGridId(newGridId);
                }
            }
        }

        // Create new activities if any
        List<Changeable> cChildren = new ArrayList<Changeable>();
        for (Amendment amendment : amendments) {
            for (Delta<?> delta : amendment.getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change.getAction() == ChangeAction.ADD) {
                        Child child = deltaService.findChangeChild((Add) change);
                        if (child == null) {
                                    throw new StudyImportException(
                                        "Could not resolve child for %s", change);
                        }
                        if (!(child instanceof Population)) {
                            if (child instanceof PlannedActivity) {
                                cChildren.add(child);
                            } else {
                                cChildren.addAll(templateService.findChildren((Parent) child, PlannedActivity.class));
                            }
                        }
                    }
                }
            }
        }

        for (Changeable pa: cChildren) {
            PlannedActivity plannedActivity = (PlannedActivity)pa;
            Activity activity = plannedActivity.getActivity();

            Activity existingActivity = activityDao.getByCodeAndSourceName(activity.getCode(), activity.getSource().getName());
            if (existingActivity != null) {
                plannedActivity.setActivity(existingActivity);
            } else {
                activityService.saveActivity(activity);
                plannedActivity.setActivity(activity);
            }
        }
        return newStudy;
    }

    private TemplateInternalReferenceIndex buildTemplateInternalReferenceIndex(List<Amendment> amendments) {
        TemplateInternalReferenceIndex index = new TemplateInternalReferenceIndex();
        for (Amendment amendment : amendments) {
            for (Delta<?> delta : amendment.getDeltas()) {
                index.addDelta(delta);
                for (Change change : delta.getChanges()) {
                    if (change instanceof ChildrenChange) {
                        ChildrenChange oChange = (ChildrenChange) change;
                        index.addChildrenChange(oChange);
                        if (change.getAction() == ChangeAction.ADD) {
                            Child child = deltaService.findChangeChild(oChange);
                            if (!(child instanceof Population))  {
                                for (Child oChild : templateService.findChildren((Parent) child, Period.class)) {
                                    index.addPlanTreeNode((PlanTreeNode)oChild);
                                }
                                for (Child oChild : templateService.findChildren((Parent) child, StudySegment.class)) {
                                    index.addPlanTreeNode((PlanTreeNode)oChild);
                                }
                                for (Child oChild : templateService.findChildren((Parent) child, PlannedActivity.class)) {
                                    index.addPlanTreeNode((PlanTreeNode)oChild);
                                }
                                index.addPlanTreeNode((PlanTreeNode)child);
                            }
                        }
                    }
                }
            }
        }
        return index;
    }

    public Study saveStudy(Study newStudy, Study oldStudy) {
        Amendment newDevelopment = newStudy.getDevelopmentAmendment();
        Study study;
        List<Amendment> toApply;
        if (oldStudy == null) {
            study = newStudy;
            toApply = newStudy.getAmendmentsListInReverseOrder();
        } else {
            templateDevelopmentService.deleteDevelopmentAmendmentOnly(oldStudy);
            study = oldStudy;
            toApply = newStudy.getAmendmentsListInReverseOrder().subList(oldStudy.getAmendmentsList().size(), newStudy.getAmendmentsList().size());
        }

        study.setDevelopmentAmendment(null);
        studyDao.save(study);
        for (Amendment amendment: toApply) {
            study.setDevelopmentAmendment(amendment);
            resolveDeltaNodesAndChangeChildren(amendment);
            amendmentService.amend(study);
        }

        if (newDevelopment != null) {
            resolveDeltaNodesAndChangeChildren(newDevelopment);
        }

        study.setDevelopmentAmendment(newDevelopment);
        studyService.save(study);
        return study;
    }

    private <T extends Changeable> T findRealNode(T nodeTemplate) {
        GridIdentifiableDao<T> dao = (GridIdentifiableDao<T>) daoFinder.findDao(nodeTemplate.getClass());
        if (dao instanceof PopulationDao) {
            return (T) nodeTemplate;
        } else {
            return dao.getByGridId(nodeTemplate.getGridId());
        }
    }

    private void resolveDeltaNodesAndChangeChildren(Amendment amendment) {
        Map<String, Child> referencedChildren = new HashMap<String, Child>();
        for (Delta delta : amendment.getDeltas()) {
            // resolve node
            Changeable deltaNode = findRealNode(delta.getNode());
            if (deltaNode != null) {
                delta.setNode(deltaNode);
            } else {
                throw new StudyImportException(String.format("Delta with id %s references unknown node with id %s. Please check the node id."
                        , delta.getGridId() ,delta.getNode().getGridId()));
            }
            // resolve child nodes
            for (Object oChange : delta.getChanges()) {
                if (oChange instanceof ChildrenChange) {
                    ChildrenChange change = (ChildrenChange) oChange;
                    Child nodeTemplate = change.getChild();

                    // If nodeTemplate is not null, element has yet to be persisted (because we setChild in serializer).
                    // So since we have a template for the node, we want to find the actual node.
                    if (nodeTemplate != null) {
                        Child node = findRealNode(nodeTemplate);
                        if (node != null) {
                            change.setChild(node);
                        } else if (referencedChildren.containsKey(nodeTemplate.getGridId())) {
                            change.setChild(referencedChildren.get(nodeTemplate.getGridId()));
                        }
                    }
                    referencedChildren.put(change.getChild().getGridId(), change.getChild());
                }
            }
        }
    }

    ////// Bean Setters
    @Required
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setGridIdentifierResolver(GridIdentifierResolver gridIdentifierResolver) {
        this.gridIdentifierResolver = gridIdentifierResolver;
    }
    
    @Required
    public void setLocalGridIdentifierCreator(LocalGridIdentifierCreator localGridIdentifierCreator) {
        this.localGridIdentifierCreator = localGridIdentifierCreator;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setTemplateDevelopmentService(TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }
}

