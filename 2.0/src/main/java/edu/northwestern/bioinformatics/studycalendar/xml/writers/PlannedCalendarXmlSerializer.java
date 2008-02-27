package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import org.dom4j.Element;

public class PlannedCalendarXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String PLANNED_CALENDAR = "planned-calendar";
    private PlannedCalendarDao plannedCalendarDao;
    private boolean serializeEpoch;

    public PlannedCalendarXmlSerializer() {
        serializeEpoch = false;
    }

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedCalendar();
    }

    protected String elementName() {
        return PLANNED_CALENDAR;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return plannedCalendarDao.getByGridId(id);
    }

    protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
        if (serializeEpoch) {
            EpochXmlSerializer serializer = (EpochXmlSerializer) getBeanFactory().getBean("epochXmlSerializer");
            serializer.setStudy(study);
            return serializer;
        }
        return null;
    }
    
    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) {
        if (getChildSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    public void setPlannedCalendarDao(PlannedCalendarDao plannedCalendarDao) {
        this.plannedCalendarDao = plannedCalendarDao;
    }


    public void setSerializeEpoch(boolean serializeEpoch) {
        this.serializeEpoch = serializeEpoch;
    }
}
