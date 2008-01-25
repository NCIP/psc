package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.dom4j.Element;

import java.util.List;
import java.util.ArrayList;
import static java.util.Collections.singletonList;

public abstract class AbstractChangeXmlSerializer extends AbstractStudyCalendarXmlSerializer<Change> {
    private PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    protected ChangeDao changeDao;

    protected abstract Change changeInstance();
    protected abstract String elementName();
    protected void addAdditionalAttributes(final Change change, Element element) {}
    protected void setAdditionalProperties(final Element element, Change change){}

    protected AbstractChangeXmlSerializer(Study study) {
        planTreeNodeSerializerFactory = new PlanTreeNodeXmlSerializerFactory(study);
    }

    public Element createElement(Change change) {
        Element element = element(elementName());
        element.addAttribute(ID, change.getGridId());
        addAdditionalAttributes(change, element);
        return element;
    }

    public Change readElement(Element element) {
        String gridId = element.attributeValue(ID);
        Change change = changeDao.getByGridId(gridId);
        if (change == null) {
            change = changeInstance();
            change.setGridId(gridId);
            setAdditionalProperties(element, change);
        }
        return change;
    }

    protected Element getElementById(Element element, String string) {
        List<Element> nodes = getAllNodes(element.getDocument().getRootElement());

        return findNodeById(nodes, string);
    }

    protected List<Element> getAllNodes(Element element) {
        if (element.nodeCount() == 0)
            return singletonList(element);

        List<Element> master = new ArrayList<Element>();
        master.add(element);

        List<Element> children = element.elements();
        for (Element child : children) {
            master.addAll(getAllNodes(child));
        }

        return master ;
    }

    protected Element findNodeById(List<Element> nodes, String id) {
        for (Element element : nodes) {
            if (id.equals(element.attributeValue(ID))) {
                return element;
            }
        }
        return null;
    }

    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }


    protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeSerializerFactory() {
        return planTreeNodeSerializerFactory;
    }

    public class PlanTreeNodeXmlSerializerFactory {
        private Study study;

        public PlanTreeNodeXmlSerializerFactory(Study study) {
            this.study = study;
        }

        public AbstractPlanTreeNodeXmlSerializer createPlanTreeNodeXmlSerializer(final Element node) {
            if (PlannedCalendarXmlSerializer.PLANNED_CALENDAR.equals(node.getName())) {
                return new PlannedCalendarXmlSerializer(study);
            } else if (EpochXmlSerializer.EPOCH.equals(node.getName())) {
                return new EpochXmlSerializer(study);
            } else if (StudySegmentXmlSerializer.STUDY_SEGMENT.equals(node.getName())) {
                return new StudySegmentXmlSerializer(study);
            } else if (PeriodXmlSerializer.PERIOD.equals(node.getName())) {
                return new PeriodXmlSerializer(study);
            } else if(PlannedActivityXmlSerializer.PLANNED_ACTIVITY.equals(node.getName())) {
                return new PlannedActivityXmlSerializer(study);
            } else {
                throw new StudyCalendarError("Problem importing template. Could not find node type %s", node.getName());
            }
        }
    }
}
