/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public class PeriodXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String PERIOD = "period";
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

    protected PlannedActivityXmlSerializer getPlannedActivityXmlSerializer() {
        return (PlannedActivityXmlSerializer) getChildXmlSerializer();
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Period) node).setName(element.attributeValue(NAME));
        ((Period) node).setRepetitions(Integer.parseInt(element.attributeValue(REPETITIONS)));
        ((Period) node).setStartDay(new Integer(element.attributeValue(START_DAY)));

        Integer durationQuantity = new Integer(element.attributeValue(DURATION_QUANTITY));
        Duration.Unit durationUnit;
        try {
            durationUnit = Duration.Unit.valueOf(element.attributeValue(DURATION_UNIT));
        } catch(Exception e) {
            throw new StudyImportException("Unknown Duration Unit %s",element.attributeValue(DURATION_UNIT));
        }
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
        if (getChildXmlSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildXmlSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    @Override
    public String validateElement(MutableDomainObject planTreeNode, Element element) {
        StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));

        Period period = (Period) planTreeNode;

        if (!StringUtils.equals(period.getName(), element.attributeValue(NAME))) {
            errorMessageStringBuffer.append(String.format("name  is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s. \n", period.getName(), element.getName()));
        } else if (!StringUtils.equals(String.valueOf(period.getRepetitions()), element.attributeValue(REPETITIONS))) {
            errorMessageStringBuffer.append(String.format("repetitions  is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s. \n", period.getRepetitions(), element.attributeValue(REPETITIONS)));
        } else if (!StringUtils.equals(StringTools.valueOf(period.getStartDay()), element.attributeValue(START_DAY))) {
            errorMessageStringBuffer.append(String.format("startDay  is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s. \n", period.getStartDay(), element.attributeValue(START_DAY)));
        }

        if (period.getDuration() != null && element.attributeValue(DURATION_QUANTITY) != null) {
            Duration duration = period.getDuration();
            if (!StringUtils.equals(StringTools.valueOf(duration.getQuantity()), element.attributeValue(DURATION_QUANTITY))) {
                errorMessageStringBuffer.append(String.format("duration quantity  is different for " + planTreeNode.getClass().getSimpleName()
                        + ". expected:%s , found (in imported document) :%s. \n", duration.getQuantity(), element.attributeValue(DURATION_QUANTITY)));
            } else if (!StringUtils.equals(StringTools.valueOf(duration.getUnit()), element.attributeValue(DURATION_UNIT))) {
                errorMessageStringBuffer.append(String.format("duration unit  is different for " + planTreeNode.getClass().getSimpleName()
                        + ". expected:%s , found (in imported document) :%s. \n", duration.getUnit(), element.attributeValue(DURATION_UNIT)));
            }

        }

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

                PlannedActivity plannedActivity = getPlannedActivityXmlSerializer().getPlannedActivityWithMatchingGridId(plannedActivities, childElement);
                if (plannedActivity == null) {
                    errorMessageStringBuffer.append(String.format("A planned activity (id:%s) present in imported document is not present in system. \n",
                            childElement.attributeValue(ID)));
                    errorMessageStringBuffer.append(String.format("Imported document has following planned activities for same period :\n  %s",
                            getErrorStringForPlannedActivities(plannedActivities)));

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
            errorMessageStringBuffer.append(String.format("%s ;id=%s \n", plannedActivity.toString(), plannedActivity.getGridId()));
        }

        return errorMessageStringBuffer.toString();
    }

    public Period getPeriodWithMatchingGridId(SortedSet<Period> periods, Element childElement) {

        for (Iterator<Period> iterator = periods.iterator(); iterator.hasNext();) {
            Period period = iterator.next();
            if (StringUtils.equals(period.getGridId(), childElement.attributeValue(ID))) {
                return period;

            }
        }


        return null;

    }
}
