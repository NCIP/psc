/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazySortedMap;
import org.springframework.validation.Errors;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Jalpa Patel
 */
public class AdvancedEditActivityCommand implements Validatable {
    private Activity activity;
    private final String namespace = "URI";
    private ActivityDao activityDao;
    private ActivityService activityService  = new  ActivityService();
    private ActivityPropertyDao activityPropertyDao;
    private Map<String, UriPropertyList> existingUri;
    private Map<String, UriPropertyList> newUri;

    public AdvancedEditActivityCommand(Activity activity, ActivityDao activityDao,ActivityPropertyDao activityPropertyDao) {
        this.activity = activity;
        this.activityDao = activityDao;
        this.activityPropertyDao = activityPropertyDao;
        this.existingUri = createExistingUri();
        this.newUri = createNewUri();
    }

    public Activity updateActivity() {
        activityDao.save(activity);
        updateExistingUri();
        saveNewUri();
        return activity;
    }

    public void updateExistingUri() {
        Map<String, UriPropertyList> updatedUri = getExistingUri();
        Set entries = updatedUri.entrySet();
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
               Map.Entry entry = (Map.Entry)iterator.next();
               String indexValue = (String)entry.getKey();
               if (getExistingUri().get(entry.getKey()).getTemplateValue()!=null) {
                    String templateName = indexValue.concat(".").concat("template");
                    ActivityProperty activityProperty1 = activityPropertyDao.getByNamespaceAndName(activity.getId(),namespace,templateName);
                    if(activityProperty1 == null) {
                        activityProperty1 = new ActivityProperty();
                        activityProperty1.setActivity(activity);
                        activityProperty1.setNamespace(namespace);
                        activityProperty1.setName(templateName);
                        activityProperty1.setValue(getExistingUri().get(entry.getKey()).getTemplateValue());
                    } else {
                        activityProperty1.setValue(getExistingUri().get(entry.getKey()).getTemplateValue());
                    }
                    activityPropertyDao.save(activityProperty1);
               }
               if (getExistingUri().get(entry.getKey()).getTextValue()!=null) {
                    String textName = indexValue.concat(".").concat("text");
                    ActivityProperty activityProperty2 = activityPropertyDao.getByNamespaceAndName(activity.getId(),namespace,textName);
                    if (activityProperty2 == null) {
                        activityProperty2 = new ActivityProperty();
                        activityProperty2.setActivity(activity);
                        activityProperty2.setNamespace(namespace);
                        activityProperty2.setName(textName);
                        activityProperty2.setValue(getExistingUri().get(entry.getKey()).getTemplateValue());
                    } else {
                        activityProperty2.setValue(getExistingUri().get(entry.getKey()).getTextValue());
                    }
                    activityPropertyDao.save(activityProperty2);
               }
        }
    }

    public void saveNewUri() {
        Map<String,UriPropertyList> addedUri = getNewUri();
        Set entries = addedUri.entrySet();
        Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            List<ActivityProperty> activityProperties;
            String templateName = "0".concat(".").concat("template");
            String textName = "0".concat(".").concat("text");
            activityProperties = activityPropertyDao.getByActivityId(activity.getId());
            if (activityProperties!=null) {
               for (int k = activityProperties.size();k>0;k--) {
                    ActivityProperty property = activityProperties.get(k-1);
                    String[] indexValue = property.getName().split("\\.");
                    if (indexValue!=null && indexValue[0].matches("\\d+")) {
                        int value = Integer.parseInt(indexValue[0]);
                        String index = String.valueOf(value+1);
                        templateName = index.concat(".").concat("template");
                        textName = index.concat(".").concat("text");
                        break;
                    }
                }
            }
            if (getNewUri().get(entry.getKey()).getTemplateValue()!=null)  {
                ActivityProperty activityProperty1 = new ActivityProperty();
                activityProperty1.setActivity(activity);
                activityProperty1.setNamespace(namespace);
                activityProperty1.setName(templateName);
                activityProperty1.setValue(getNewUri().get(entry.getKey()).getTemplateValue());
                activityPropertyDao.save(activityProperty1);
            }
            if (getNewUri().get(entry.getKey()).getTextValue()!=null) {
                ActivityProperty activityProperty2 = new ActivityProperty();
                activityProperty2.setActivity(activity);
                activityProperty2.setNamespace(namespace);
                activityProperty2.setName(textName);
                activityProperty2.setValue(getNewUri().get(entry.getKey()).getTextValue());
                activityPropertyDao.save(activityProperty2);
            }
        }
    }

    public Map<String, UriPropertyList> getExistingUri() {
        return existingUri;
    }

    private Map<String,UriPropertyList> createExistingUri() {
        Map<String,UriPropertyList> existingList = new TreeMap<String,UriPropertyList>();
        if (activity.getId()!=null){
           Map<String,List<String>> uriListMap = activityService.createActivityUriList(activity);
           Iterator iterator = uriListMap.entrySet().iterator();
           while (iterator.hasNext()) {
               Map.Entry entry = (Map.Entry)iterator.next();
               UriPropertyList uriList = new UriPropertyList();
               List<String> uriValues = (List)entry.getValue();
               uriList.setTemplateValue(uriValues.get(1));
               uriList.setTextValue(uriValues.get(0));
               existingList.put((String)entry.getKey(),uriList);
           }
        }
        return existingList;
    }

    public Map<String, UriPropertyList> getNewUri() {
        return newUri;
    }

    private Map<String,UriPropertyList> createNewUri() {
        return LazySortedMap.decorate(new TreeMap<String, UriPropertyList>(), new Factory<UriPropertyList>() {
            public UriPropertyList create() {
                return new UriPropertyList();
            }
        });
    }
    
    ////CONFIGURATION

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public ActivityDao getActivityDao() {
        return activityDao;
    }

    public void setActivityPropertyDao(ActivityPropertyDao activityPropertyDao) {
        this.activityPropertyDao = activityPropertyDao;
    }

    ////BEAN PROPERTY

    public String getNamespace() {
        return namespace;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public class UriPropertyList {
        private String templateValue;
        private String textValue;

        public String getTemplateValue() {
            return templateValue;
        }

        public void setTemplateValue(String templateValue) {
            this.templateValue = templateValue;
        }

        public String getTextValue() {
            return textValue;
        }

        public void setTextValue(String textValue) {
            this.textValue = textValue;
        }
    }

    public void validate(Errors errors) {
        if (activity.getName() == null || activity.getName().length() <= 0) {
            errors.rejectValue("activity.name", "error.activity.name.is.empty");
        }

        if(activity.getCode() == null || activity.getCode().length() <= 0) {
            errors.rejectValue("activity.code", "error.activity.code.is.empty");
        }
    }

}
