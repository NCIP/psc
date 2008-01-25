package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public abstract class AbstractDeltaXmlSerializer extends AbstractStudyCalendarXmlSerializer<Delta> {
    private Study study;

    private static final String PERIOD_DELTA = "period-delta";
    private static final String PLANNED_ACTIVITY_DELTA = "planned-activity-delta";
    private static final String NODE_ID = "node-id";
    private DeltaDao deltaDao;
    private TemplateService templateService;


    public AbstractDeltaXmlSerializer(Study study) {
        this.study = study;
    }

    protected abstract Delta deltaInstance();
    protected abstract PlanTreeNode<?> nodeInstance();
    protected abstract String elementName();

    public Element createElement(Delta delta) {
         Element element = element(elementName());
//            if (delta instanceof PlannedCalendarDelta) {
//                element = element(PLANNED_CALENDAR_DELTA);
//            } else if (delta instanceof EpochDelta) {
//                element = element(EPOCH_DELTA);
//            } else if (delta instanceof StudySegmentDelta) {
//                element = element(STUDY_SEGMENT_DELTA);
//            } else if (delta instanceof PeriodDelta) {
//                element = element(PERIOD_DELTA);
//            } else if (delta instanceof PlannedActivityDelta) {
//                element = element(PLANNED_ACTIVITY_DELTA);
//            } else {
//                throw new StudyCalendarError("Delta is not recognized: %s", delta.getClass());
//            }

            element.addAttribute(ID, delta.getGridId());
            element.addAttribute(NODE_ID, delta.getNode().getGridId());

        // TODO: Add Change Nodes
        return element;
    }

    public Delta readElement(Element element) {
        String gridId = element.attributeValue(ID);
        Delta delta = deltaDao.getByGridId(gridId);
        if (delta == null) {
            delta = deltaInstance();
            delta.setGridId(gridId);

            PlanTreeNode<?> node = nodeInstance();
            node.setGridId(element.attributeValue(NODE_ID));
            node = templateService.findEquivalentChild(study, node);
            if (node == null) {
                throw new StudyCalendarError("Problem importing template. Cannot find Node for grid id: %s", gridId);
            }

            delta.setNode(node);
        }
        return delta;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
