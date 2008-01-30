package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

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
}
