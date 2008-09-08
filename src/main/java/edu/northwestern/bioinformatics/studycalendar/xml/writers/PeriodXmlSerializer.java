package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.utils.StringTools;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

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

    protected PlannedActivityXmlSerializer getPlannedActivityXmlSerializer() {
        return (PlannedActivityXmlSerializer) getChildSerializer();
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

    @Override
    public String validateElement(PlanTreeNode<?> planTreeNode, Element element) {
        StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));

        Period period = (Period) planTreeNode;

        List<PlannedActivity> plannedActivities = period.getPlannedActivities();

        List childElements = element.elements();
        if ((childElements == null && plannedActivities != null)
                || (childElements != null && plannedActivities == null)
                || (plannedActivities.size() != childElements.size())) {
            errorMessageStringBuffer.append(String.format("%s present in the system and in the imported document must have identical number of planned activities.\n",
                    planTreeNode.toString()));


        } else {
            for (int i = 0; i < childElements.size(); i++) {
                Element childElement = (Element) childElements.get(i);

                PlannedActivity plannedActivity = getPlannedActivityXmlSerializer().getPlannedActivityWithMatchingAttributes(plannedActivities, childElement);
                if (plannedActivity == null) {
                    errorMessageStringBuffer.append(String.format("A planned activity %s present in imported document is not present in system. \n", childElement.asXML()));
                    errorMessageStringBuffer.append(String.format("Imported document has following planned activities for same period :\n  %s", getErrorStringForPlannedActivities(plannedActivities)));

                    break;
                }
                errorMessageStringBuffer.append(getPlannedActivityXmlSerializer().validateElement(plannedActivity, childElement));

            }
        }

        return errorMessageStringBuffer.toString();
    }

    private String getErrorStringForPlannedActivities(List<PlannedActivity> plannedActivities) {
        StringBuffer errorMessageStringBuffer = new StringBuffer("");
        for (PlannedActivity plannedActivity : plannedActivities) {
            errorMessageStringBuffer.append(String.format("%s ;grid id=%s \n", plannedActivity.toString(), plannedActivity.getGridId()));
        }

        return errorMessageStringBuffer.toString();
    }

    public Period getPeriodWithMatchingAttributes(SortedSet<Period> periods, Element childElement) {

        for (Iterator<Period> iterator = periods.iterator(); iterator.hasNext();) {
            Period period = iterator.next();
            if ((StringUtils.equals(period.getName(), childElement.attributeValue(NAME)))
                    && (StringUtils.equals(String.valueOf(period.getRepetitions()), childElement.attributeValue(REPETITIONS)))
                    && (StringUtils.equals(StringTools.valueOf(period.getStartDay()), childElement.attributeValue(START_DAY)))) {

                if (period.getDuration() != null && childElement.attributeValue(DURATION_QUANTITY) != null) {
                    Duration duration = period.getDuration();
                    if ((StringUtils.equals(StringTools.valueOf(duration.getQuantity()), childElement.attributeValue(DURATION_QUANTITY)))
                            && (StringUtils.equals(StringTools.valueOf(duration.getUnit()), childElement.attributeValue(DURATION_UNIT)))) {

                        return period;
                    }
                } else if (period.getDuration() == null && childElement.attributeValue(DURATION_QUANTITY) == null) {
                    return period;
                }

            }
        }

        return null;

    }
}
