package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.dom4j.Element;

public class PeriodXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String PERIOD = "period";

    private PeriodDao periodDao;
    private static final String START_DAY = "start-day";
    private static final String DURATION_QUANTITY = "duration-quantity";
    private static final String DURATION_UNIT = "duration-unit";
    private static final String REPETITIONS = "repetitions";

    protected PlanTreeNode<?> nodeInstance() {
        return new Period();
    }

    protected String elementName() {
        return PERIOD;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return periodDao.getByGridId(id);
    }

    protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
        PlannedActivityXmlSerializer serializer = (PlannedActivityXmlSerializer) getBeanFactory().getBean("plannedActivityXmlSerializer");
        serializer.setStudy(study);
        return serializer;
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Period) node).setName(element.attributeValue(NAME));
        ((Period) node).setRepetitions(Integer.parseInt(element.attributeValue(REPETITIONS)));
        ((Period) node).setStartDay(new Integer(element.attributeValue(START_DAY)));

        Integer durationQuantity = new Integer(element.attributeValue(DURATION_QUANTITY));
        Duration.Unit durationUnit = (element.attributeValue(DURATION_UNIT).equals("day")) ? Duration.Unit.day : Duration.Unit.week;
        ((Period) node).setDuration(new Duration(durationQuantity, durationUnit));
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((Period) node).getName());
        element.addAttribute(REPETITIONS, String.valueOf(((Period) node).getRepetitions()));
        element.addAttribute(START_DAY, ((Period) node).getStartDay().toString());
        element.addAttribute(DURATION_QUANTITY, ((Period) node).getDuration().getQuantity().toString());
        element.addAttribute(DURATION_UNIT, ((Period) node).getDuration().getUnit().toString());
    }

    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) {
        if (getChildSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
