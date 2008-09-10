package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.utils.StringTools;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.List;

public class PlannedActivityXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    private ActivityXmlSerializer activityXmlSerializer;
    public static final String PLANNED_ACTIVITY = "planned-activity";

    public static final String POPULATION = "population";
    private static final String DETAILS = "details";
    private static final String DAY = "day";
    private static final String CONDITION = "condition";

    private PlannedActivityDao plannedActivityDao;

    protected PlanTreeNode<?> nodeInstance() {
        return new PlannedActivity();
    }

    protected String elementName() {
        return PLANNED_ACTIVITY;
    }

    protected PlanTreeNode<?> getFromId(String id) {
        return plannedActivityDao.getByGridId(id);
    }

    protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
        return null;
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((PlannedActivity) node).setDetails(element.attributeValue(DETAILS));
        ((PlannedActivity) node).setDay(new Integer(element.attributeValue(DAY)));
        ((PlannedActivity) node).setCondition(element.attributeValue(CONDITION));

        String populationAbbreviation = element.attributeValue(POPULATION);
        if (populationAbbreviation != null) {
            for (Population population : study.getPopulations()) {
                if (populationAbbreviation.equals(population.getAbbreviation())) {
                    ((PlannedActivity) node).setPopulation(population);
                }
            }
        }

        Activity activity = activityXmlSerializer.readElement(element.element(XsdElement.ACTIVITY.xmlName()));
        ((PlannedActivity) node).setActivity(activity);
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(DETAILS, ((PlannedActivity) node).getDetails());
        element.addAttribute(DAY, ((PlannedActivity) node).getDay().toString());
        element.addAttribute(CONDITION, ((PlannedActivity) node).getCondition());
        Population population = ((PlannedActivity) node).getPopulation();
        if (population != null) {
            element.addAttribute(POPULATION, ((PlannedActivity) node).getPopulation().getAbbreviation());
        }

        Element eActivity = activityXmlSerializer.createElement(((PlannedActivity) node).getActivity());
        element.add(eActivity);
    }

    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    public void setActivityXmlSerializer(ActivityXmlSerializer activityXmlSerializer) {
        this.activityXmlSerializer = activityXmlSerializer;
    }

    @Override
    public String validateElement(PlanTreeNode<?> planTreeNode, Element element) {

        StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));
        PlannedActivity plannedActivity = (PlannedActivity) planTreeNode;
        if (!activityXmlSerializer.validateElement(plannedActivity.getActivity(), element.element(XsdElement.ACTIVITY.xmlName()))) {
            errorMessageStringBuffer.append(String.format("activities  are different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s. \n", plannedActivity.getActivity()));
        }
        if (!StringUtils.equals(plannedActivity.getDetails(), element.attributeValue(DETAILS))) {
            errorMessageStringBuffer.append(String.format("details  are different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getDetails(),
                    element.attributeValue(DETAILS)));

        } else if (!StringUtils.equals(StringTools.valueOf(plannedActivity.getDay()), element.attributeValue(DAY))) {
            errorMessageStringBuffer.append(String.format("days  are different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getDay(),
                    element.attributeValue(DAY)));

        } else if (!StringUtils.equals(plannedActivity.getCondition(), element.attributeValue(CONDITION))) {
            errorMessageStringBuffer.append(String.format("conditions  are different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getCondition(),
                    element.attributeValue(CONDITION)));

        }

        if (plannedActivity.getPopulation() != null && element.attributeValue(POPULATION) != null) {
            Population population = plannedActivity.getPopulation();
            if (!StringUtils.equals(population.getAbbreviation(), element.attributeValue(POPULATION))) {
                errorMessageStringBuffer.append(String.format("populations  are different for " + planTreeNode.getClass().getSimpleName()
                        + ". expected:%s , found (in imported document) :%s \n", population.getAbbreviation(),
                        element.attributeValue(POPULATION)));


            }

        } else if ((plannedActivity.getPopulation() == null && element.attributeValue(POPULATION) != null) || (plannedActivity.getPopulation() != null && element.attributeValue(POPULATION) == null)) {
            errorMessageStringBuffer.append(String.format("populations  are different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", plannedActivity.getPopulation(),
                    element.attributeValue(POPULATION)));

        }
        return errorMessageStringBuffer.toString();
    }

    public PlannedActivity getPlannedActivityWithMatchingGridId(List<PlannedActivity> plannedActivities, Element element) {
        for (PlannedActivity plannedActivity : plannedActivities) {
            if (StringUtils.equals(plannedActivity.getGridId(), element.attributeValue(ID))) {

                return plannedActivity;

            }

        }

        return null;

    }
}
