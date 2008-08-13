package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.web.template.period.EditPlannedActivityCommand;

import java.util.HashMap;
import java.util.Map;


public class AddToPeriodCommand extends EditPlannedActivityCommand {
    private PlannedActivity addedActivity;
    private Population population;

    @Override
    protected void performEdit() {
        addedActivity = new PlannedActivity();
        addedActivity.setDay(getColumnNumber()+1);
        addedActivity.setActivity(getActivity());
        addedActivity.setDetails(getDetails());
        addedActivity.setCondition(getConditionalDetails());
        addedActivity.setPopulation(getPopulation());
        amendmentService.updateDevelopmentAmendment(getPeriod(), Add.create(addedActivity));
    }

    @Override
    public String getRelativeViewName() {
        return "addPlannedActivity";
    }

    @Override
    public Map<String, Object> getLocalModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("addedActivity", addedActivity);
        map.put("rowNumber", getRowNumber());
        map.put("columnNumber", getColumnNumber());
        return map;
    }

    public PlannedActivity getAddedActivity() {
        return addedActivity;
    }

    ////// BOUND PROPERTIES

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }
}
