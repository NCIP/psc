package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXMLWriter.*;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.springframework.beans.factory.annotation.Required;

public class PlanTreeNodeXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlanTreeNode<?>> {

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
        PlanTreeNode<?> node = getPlanTreeNode(key, element.getName());
        if (node == null) {
            node = createPlanTreeNode(element);

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


    private PlanTreeNode<?> createPlanTreeNode(Element element) {
        String elementName = element.getName();
        if (elementName.equals(EPOCH)) {
            return new Epoch();
        } else if (elementName.equals(STUDY_SEGMENT)) {
            return new StudySegment();
        } else if (elementName.equals(PERIOD)) {
            return new Period();
        } else if (elementName.equals(PLANNED_ACTIVITY)) {
            PlannedActivity activity = new PlannedActivity();
            activity.setGridId(element.attributeValue(ID));
            activity.setDay(new Integer(element.attributeValue(DAY)));
            activity.setDetails(element.attributeValue(DETAILS));
            activity.setCondition(element.attributeValue(CONDITION));
            return activity;
        } else {
            throw new StudyCalendarError("Cannot find Node for: %s", elementName);
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
