package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.springframework.beans.factory.annotation.Required;

public class EpochXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlanTreeNode<?>> {

    // Elements
    public static final String EPOCH = "epoch";
    public static final String STUDY_SEGMENT = "study-segment";
    public static final String PERIOD = "period";
    public static final String PLANNED_ACTIVITY = "planned-activity";

    private EpochDao epochDao;
    private StudySegmentDao studySegmentDao;
    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;

    public Element createElement(PlanTreeNode<?> node) {
        String elementName = findElementName(node);

        // Using QName is the only way to attach the namespace to the element
        QName qNode = DocumentHelper.createQName(elementName, DEFAULT_NAMESPACE);
        Element eNode = DocumentHelper.createElement(qNode)
                .addAttribute(ID, node.getGridId());

        if (node instanceof Named) {
            eNode.addAttribute(NAME, ((Named)node).getName());
        }

        return eNode;
    }

    public PlanTreeNode<?> readElement(Element element) {
        String key = element.attributeValue(ID);
        Class<?> nodeClass = findElementClass(element.getName());
        PlanTreeNode<?> node = getPlanTreeNode(key, element.getName());
        if (node == null) {
            node = createInstance((Class<PlanTreeNode<?>>) nodeClass);
            node.setGridId(key);
            if (node instanceof Named) {
                ((Named) node).setName(element.attributeValue(NAME));
            }
        }
        return node;
    }

    private String findElementName(PlanTreeNode<?> node) {
        if (node instanceof Epoch) {
            return EPOCH;
        } else if (node instanceof StudySegment) {
            return STUDY_SEGMENT;
        } else if (node instanceof Period) {
            return PERIOD;
        } else if (node instanceof PlannedActivity) {
            return PLANNED_ACTIVITY;
        } else {
            throw new StudyCalendarError("Cannot find Node for: %s", node.getClass().getName());
        }
    }

    private Class findElementClass(String elementName) {
        if (elementName.equals(EPOCH)) {
            return Epoch.class;
        } else if (elementName.equals(STUDY_SEGMENT)) {
            return StudySegment.class;
        } else if (elementName.equals(PERIOD)) {
            return Period.class;
        } else if (elementName.equals(PLANNED_ACTIVITY)) {
            return PlannedActivity.class;
        } else {
            throw new StudyCalendarError("Cannot find Node for: %s", elementName);
        }
    }

    private <T extends PlanTreeNode> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarError("Could not import from template XML", e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("Could not import from template XML", e);
        }
    }

    public PlanTreeNode<?> getPlanTreeNode(String gridId, String nodeName) {
       if (nodeName.equals(EPOCH)) {
            return epochDao.getByGridId(gridId);
        } else if (nodeName.equals(STUDY_SEGMENT)) {
            return studySegmentDao.getByGridId(gridId);
        } else if (nodeName.equals(PERIOD)) {
            return periodDao.getByGridId(gridId);
        } else if (nodeName.equals(PLANNED_ACTIVITY)) {
            return plannedActivityDao.getByGridId(gridId);
        } else {
            throw new StudyCalendarError("Cannot find Node for: %s", nodeName);
        }
    }

    // Dao setters
    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }
}
