package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.util.List;
import java.util.ArrayList;

public abstract class PlanTreeNodeXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlanTreeNode<?>> {

    // Elements

    public static final String STUDY_SEGMENT = "study-segment";
    public static final String PERIOD = "period";
    public static final String PLANNED_ACTIVITY = "planned-activity";

    // Attributes
    public static final String POPULATION = "population";
//
//    private EpochDao epochDao;
//    private StudySegmentDao studySegmentDao;
//    private PeriodDao periodDao;
//    private PlannedActivityDao plannedActivityDao;

//    public Element createElement(PlanTreeNode<?> node) {
//        String elementName = findElementName(node);
//
//        Element element = element(elementName)
//                .addAttribute(ID, node.getGridId());
//
//        if (node instanceof Named) {
//            element.addAttribute(NAME, ((Named)node).getName());
//        }
//
//        if (node instanceof PlannedActivity) {
//            element.addAttribute(DAY, ((PlannedActivity)node).getDay().toString());
//            element.addAttribute(DETAILS, ((PlannedActivity) node).getDetails());
//            element.addAttribute(CONDITION, ((PlannedActivity) node).getCondition());
//            Population population = ((PlannedActivity) node).getPopulation();
//            if (population != null) {
//                element.addAttribute(POPULATION, ((PlannedActivity) node).getPopulation().getAbbreviation());
//            }
//        }
//
//        return element;
//    }
//
//    public PlanTreeNode<?> readElement(Element element) {
//        String key = element.attributeValue(ID);
//        PlanTreeNode<?> node = getPlanTreeNode(key, element.getName());
//        if (node == null) {
//            node = createPlanTreeNode(element);
//
//            node.setGridId(key);
//            if (node instanceof Named) {
//                ((Named) node).setName(element.attributeValue(NAME));
//            }
//        }
//        return node;
//    }
//
//    private String findElementName(PlanTreeNode<?> node) {
//        if (node instanceof Epoch) {
//            return EPOCH;
//        } else if (node instanceof StudySegment) {
//            return STUDY_SEGMENT;
//        } else if (node instanceof Period) {
//            return PERIOD;
//        } else if (node instanceof PlannedActivity) {
//            return PLANNED_ACTIVITY;
//        } else {
//            throw new StudyCalendarError("Cannot find Node for: %s", node.getClass().getName());
//        }
//    }
//
//    public PlanTreeNode<?> getPlanTreeNode(String gridId, String nodeName) {
//       if (nodeName.equals(EPOCH)) {
//            return epochDao.getByGridId(gridId);
//        } else if (nodeName.equals(STUDY_SEGMENT)) {
//            return studySegmentDao.getByGridId(gridId);
//        } else if (nodeName.equals(PERIOD)) {
//            return periodDao.getByGridId(gridId);
//        } else if (nodeName.equals(PLANNED_ACTIVITY)) {
//            return plannedActivityDao.getByGridId(gridId);
//        } else {
//            throw new StudyCalendarError("Cannot find Node for: %s", nodeName);
//        }
//    }
//
//
//    private PlanTreeNode<?> createPlanTreeNode(Element element) {
//        String elementName = element.getName();
//        if (elementName.equals(EPOCH)) {
//            return new Epoch();
//        } else if (elementName.equals(STUDY_SEGMENT)) {
//            return new StudySegment();
//        } else if (elementName.equals(PERIOD)) {
//            return new Period();
//        } else if (elementName.equals(PLANNED_ACTIVITY)) {
//            PlannedActivity activity = new PlannedActivity();
//            activity.setDay(new Integer(element.attributeValue(DAY)));
//            activity.setDetails(element.attributeValue(DETAILS));
//            activity.setCondition(element.attributeValue(CONDITION));
//            // TODO: Add Population
//            return activity;
//        } else {
//            throw new StudyCalendarError("Cannot find Node for: %s", elementName);
//        }
//    }

    protected abstract Class<?> nodeClass();
    protected abstract String elementName();
    protected abstract PlanTreeNode<?> getFromId(String id);
    protected abstract PlanTreeNodeXmlSerializer getChildSerializer();

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {}
    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {}


    public Element createElement(PlanTreeNode<?> object) {
        Element element = element(elementName());
        element.addAttribute(ID, object.getGridId());
        addAdditionalElementAttributes(object, element);
        return element;
    }

    public PlanTreeNode<?> readElement(Element element) {
        if (!element.getName().equals(elementName())) {
            return null;
        }
        
        String key = element.attributeValue(ID);
        PlanTreeNode<?> node = getFromId(key);
        if (node == null) {
            Class<?> clazz = nodeClass();
            node = createInstance(clazz);
            node.setGridId(key);
            addAdditionalNodeAttributes(element, node);

            List<PlanTreeNode<?>> children = readChildren(element);
            addChildren(children, node);
        }
        return node;
    }

    private List<PlanTreeNode<?>> readChildren(Element element) {
        List<PlanTreeNode<?>> nodeList = new ArrayList<PlanTreeNode<?>>();
        for (Object oChild : element.elements()) {
            Element child = (Element) oChild;
            PlanTreeNodeXmlSerializer childSerializer = getChildSerializer();
            if (childSerializer != null) {
                PlanTreeNode<?> childNode = childSerializer.readElement(child);
                nodeList.add(childNode);
            }
        }
        return nodeList;
    }

    private void addChildren(List<PlanTreeNode<?>> children, PlanTreeNode<?> parent) {
        for (PlanTreeNode<?> child : children) {
            ((PlanTreeInnerNode) parent).addChild(child);
        }
    }


    //// Helper methods
    private PlanTreeNode<?> createInstance(Class<?> clazz) {
        try {
            return (PlanTreeNode<?>) clazz.newInstance();
        } catch (IllegalAccessException iae) {
            throw new StudyCalendarSystemException("Problem importing template", iae);
        } catch (InstantiationException ie) {
            throw new StudyCalendarSystemException("Problem importing template", ie);
        }
    }
}
