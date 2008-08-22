package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.data.Status;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityForm extends ValidatingForm {
    private Study study;
    private ActivityDao activityDao;
    private PopulationDao populationDao;

    public PlannedActivityForm(Representation entity, Study study, ActivityDao activityDao, PopulationDao populationDao) {
        super(entity);
        this.study = study;
        this.activityDao = activityDao;
        this.populationDao = populationDao;

        validatePresenceOf(FormParameters.DAY);
        validateIntegralityOf(FormParameters.DAY);
        validatePresenceOf(FormParameters.ACTIVITY_CODE);
        validatePresenceOf(FormParameters.ACTIVITY_SOURCE);
    }

    public PlannedActivity createDescribedPlannedActivity() throws ResourceException {
        throwForValidationFailureIfNecessary();

        Integer day = FormParameters.DAY.extractFirstAsIntegerFrom(this);
        Activity activity = findDescribedActivity();
        Population population = findDescribedPopulation();

        PlannedActivity newPlannedActivity = new PlannedActivity();
        newPlannedActivity.setDay(day);
        newPlannedActivity.setActivity(activity);
        newPlannedActivity.setCondition(
            StringEscapeUtils.unescapeHtml(FormParameters.CONDITION.extractFirstFrom(this)));
        newPlannedActivity.setDetails(
            StringEscapeUtils.unescapeHtml(FormParameters.DETAILS.extractFirstFrom(this)));
        newPlannedActivity.setPopulation(population);
        return newPlannedActivity;
    }

    private Activity findDescribedActivity() throws ResourceException {
        String activitySource = FormParameters.ACTIVITY_SOURCE.extractFirstFrom(this);
        String activityCode = FormParameters.ACTIVITY_CODE.extractFirstFrom(this);

        Activity activity = activityDao.getByCodeAndSourceName(activityCode, activitySource);
        if (activity == null) {
            throw new ResourceException(
                Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Activity not found");
        }
        return activity;
    }

    private Population findDescribedPopulation() throws ResourceException {
        Population population = null;
        String populationAbbrev = FormParameters.POPULATION.extractFirstFrom(this);
        if (populationAbbrev != null) {
            population = populationDao.getByAbbreviation(study, populationAbbrev);
            if (population == null) {
                throw new ResourceException(
                    Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "Population not found");
            }
        }
        return population;
    }
}
