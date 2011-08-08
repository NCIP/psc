package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ActivityService {
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private ActivityTypeDao activityTypeDao;
    private SourceDao sourceDao;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Deletes the activity, if it has no reference by planned activity.
     * @return boolean, corresponding to the successful or unsuccessful deletion
     * @param activity - activity we want to remove
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean deleteActivity(Activity activity) {
        Integer id = activity.getId();
        List<PlannedActivity> plannedActivities = plannedActivityDao.getPlannedActivitiesForActivity(id);
        if (plannedActivities == null || plannedActivities.size() == 0) {
            activityDao.delete(activity);
            return true;
        } else {
            return false;
        }
    }

    public List<Source> getFilteredSources(String nameOrCodeSearch, ActivityType desiredType, Source desiredSource) {
        return getFilteredSources(nameOrCodeSearch, desiredType, desiredSource, null, null);
    }

    /**
     * Searches all the activities in the system for those that match the given
     * criteria.  Returns a list of transient Source elements containing just the
     * matching activities.
     */
    public List<Source> getFilteredSources(String nameOrCodeSearch, ActivityType desiredType, Source desiredSource, Integer limit, Integer offset) {
        List<Activity> matches = activityDao.getActivitiesBySearchText(nameOrCodeSearch, desiredType, desiredSource, limit, offset);
        Map<String, Source> sources = new TreeMap<String, Source>(String.CASE_INSENSITIVE_ORDER);
        for (Activity match : matches) {
            if (match.getSource() == null) continue;
            String key = match.getSource().getNaturalKey();
            if (!sources.containsKey(key)) {
                Source newSource = match.getSource().transientClone();
                sources.put(newSource.getNaturalKey(), newSource);
            }
            sources.get(key).addActivity(match.transientClone());
        }
        for (Source source : sources.values()) {
            Collections.sort(source.getActivities());
        }
        return new ArrayList<Source>(sources.values());
    }

    /**
     * Create Activity Property Map with Key value as Index Value and put similar index property into URI List.
     * @param activity
     * @return Map - Index as key and Properties as List
     */

    public Map<String,List<String>> createActivityUriList(Activity activity) {
        List<ActivityProperty> activityProperties = activity.getProperties();
        Map<String,List<String>> uriMap = new TreeMap<String,List<String>>();
        if (activityProperties !=null) {
             int i=0;
             String[] indexValue = new String[activityProperties.size()];
             String[][] dataValue = new String[activityProperties.size()][2];
             Iterator iterator = activityProperties.iterator();

             //Get the Index of the Activity Property to be compare
              while (iterator.hasNext())   {
                    ActivityProperty activityPropertyNext = (ActivityProperty)iterator.next();
                    String[] indexValueNext = activityPropertyNext.getName().split("\\.");
                    if(indexValueNext[0]!=null) {
                        Iterator iteratorAll = activityProperties.iterator();

                        //Compare with all other properties For Activity and Put together similar Index Activity Property
                        while (iteratorAll .hasNext()) {
                            ActivityProperty activityPropertyAll = (ActivityProperty)iteratorAll.next();
                            String[] indexValueAll = activityPropertyAll.getName().split("\\.");
                            if(indexValueAll!=null && indexValueAll.length==2) {
                                if(indexValueNext[0].equals(indexValueAll[0])) {
                                    if(indexValueAll[1].equalsIgnoreCase("text")) {
                                        dataValue[i][0] = activityPropertyAll.getValue();
                                    }
                                    if(indexValueAll[1].equalsIgnoreCase("template")) {
                                        dataValue[i][1] = activityPropertyAll.getValue();
                                    }
                                    indexValue[i] = indexValueNext[0];
                                }
                            }
                        }
                    }
                    i++;
                }

                //Contruct Map with Key as Index and Value(Template & Text) as uriList
                for (int j=0;j<activityProperties.size();j=j+2) {
                    List<String> uriList = new ArrayList<String>();
                    uriList.add(0,dataValue[j][0]);
                    uriList.add(1,dataValue[j][1]);
                    if(indexValue[j]!=null)
                        uriMap.put(indexValue[j],uriList);
                }
        }
        return uriMap;
    }

    public void resolveAndSaveActivity(PlannedActivity planned) {
        Activity a = planned.getActivity();
        Activity resolved = activityDao.getByCodeAndSourceName(a.getCode(), a.getSource().getName());
        if (resolved == null) {
            saveActivity(a);
            resolved = a;
        }
        planned.setActivity(resolved);
    }

    //Creates new activity
    public void saveActivity(Activity activity) {
        resolveAndSaveSource(activity);
        resolveAndSaveActivityType(activity);
        activityDao.save(activity);
    }

    public void resolveAndSaveSource(Activity activity) {
        Source source = sourceDao.getByName(activity.getSource().getName());
        if (source == null) {
            source = new Source();
            source.setName(activity.getSource().getName());
            sourceDao.save(source);
        }
        activity.setSource(source);
    }

    public void resolveAndSaveActivityType(Activity activity) {
        ActivityType activityType = activityTypeDao.getByName(activity.getType().getName());
        if (activityType == null) {
            activityType =  new ActivityType();
            activityType.setName(activity.getType().getName());
            activityTypeDao.save(activityType);
        }
        activity.setType(activityType);
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
