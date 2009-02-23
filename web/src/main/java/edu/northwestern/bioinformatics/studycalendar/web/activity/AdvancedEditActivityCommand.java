package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.tools.ExpandingMap;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import java.util.*;

/**
 * @author Jalpa Patel
 */
public class AdvancedEditActivityCommand implements Validatable {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private Activity activity;
    private final String namespace = "URI";
    private ActivityDao activityDao;
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
               if(getExistingUri().get(entry.getKey()).getTemplateValue()!=null) {
                    String templateName = indexValue.concat(".").concat("template");
                    ActivityProperty activityProperty1 = activityPropertyDao.getByNamespaceAndName(activity.getId(),"uri",templateName);
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
               if(getExistingUri().get(entry.getKey()).getTextValue()!=null) {
                    String textName = indexValue.concat(".").concat("text");
                    ActivityProperty activityProperty2 = activityPropertyDao.getByNamespaceAndName(activity.getId(),"uri",textName);
                    if(activityProperty2 == null) {
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
            if(activityProperties!=null) {
               for(int k = activityProperties.size();k>0;k--) {
                    ActivityProperty property = activityProperties.get(k-1);
                    String[] indexValue = property.getName().split("\\.");
                    if(indexValue!=null && indexValue[0].matches("\\d+")) {
                        int value = Integer.parseInt(indexValue[0]);
                        String index = String.valueOf(value+1);
                        templateName = index.concat(".").concat("template");
                        textName = index.concat(".").concat("text");
                        break;
                    }
                }
            }
            if(getNewUri().get(entry.getKey()).getTemplateValue()!=null)  {
                ActivityProperty activityProperty1 = new ActivityProperty();
                activityProperty1.setActivity(activity);
                activityProperty1.setNamespace(namespace);
                activityProperty1.setName(templateName);
                activityProperty1.setValue(getNewUri().get(entry.getKey()).getTemplateValue());
                activityPropertyDao.save(activityProperty1);
            }
            if(getNewUri().get(entry.getKey()).getTextValue()!=null) {
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
       List<ActivityProperty> activityProperties;
       if(activity.getId()!=null){
           activityProperties = activityPropertyDao.getByActivityId(activity.getId());
           if(activityProperties !=null) {
                Iterator iterator = activityProperties.iterator();
                int i=0;
                String indexSize[] = new String[activityProperties.size()];
                String[][] data = new String[activityProperties.size()][2];

                //Get the Index of the Activity Property to be compare
                while(iterator.hasNext())   {
                    ActivityProperty activityPropertyFirst = (ActivityProperty)iterator.next();
                    String[] indexValueFirst = activityPropertyFirst.getName().split("\\.");
                    if(indexValueFirst!=null) {
                        Iterator iteratorAll = activityProperties.iterator();

                        //Compare with all other properties For Activity and Put together similar Index Activity Property
                        while(iteratorAll .hasNext()) {
                            ActivityProperty activityPropertyAll = (ActivityProperty)iteratorAll.next();
                            String[] indexValueAll = activityPropertyAll.getName().split("\\.");
                            if(indexValueAll!=null && indexValueAll.length==2) {
                                if(indexValueFirst[0].equals(indexValueAll[0])) {
                                    if(indexValueAll[1].equalsIgnoreCase("template")) {
                                        data[i][0] = activityPropertyAll.getValue();
                                    }
                                    if(indexValueAll[1].equalsIgnoreCase("text")) {
                                        data[i][1] = activityPropertyAll.getValue();
                                    }
                                    indexSize[i] = indexValueFirst[0];
                                }
                            }
                        }
                    }
                    i++;
                }

           //Contruct Map with Key as Index and Value as PropertyList
           for(int j=0;j<activityProperties.size();j=j+2) {
               UriPropertyList uriList = new UriPropertyList();
               uriList.setTemplateValue(data[j][0]);
               uriList.setTextValue(data[j][1]);
               if(indexSize[j]!=null)
               existingList.put(indexSize[j],uriList);
           }
       }
    }
    return existingList;
    }

    public Map<String, UriPropertyList> getNewUri() {
        return newUri;
    }

    private Map<String,UriPropertyList> createNewUri() {
        Map<String, UriPropertyList> newList = new ExpandingMap<String, UriPropertyList>(
                new ExpandingMap.Filler<UriPropertyList>() {
                    public UriPropertyList createNew(Object key) { return new UriPropertyList(); }
                }
         );

    return newList;
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
        if (activity.getName() != null && activity.getName().length()>0) {
            if (getActivityDao().getByNameAndSourceName(activity.getName(),"PSC - Manual Activity Creation") != null){
                errors.rejectValue("activity.name", "error.activity.name.already.exists");
            }
        }
        else {
            errors.rejectValue("activity.name", "error.activity.name.is.empty");
        }
        if(activity.getCode() !=null && activity.getCode().length()>0) {
            if(getActivityDao().getByCodeAndSourceName(activity.getCode(),"PSC - Manual Activity Creation") != null) {
                errors.rejectValue("activity.code", "error.activity.code.already.exists");
            }
        }
        else {
            errors.rejectValue("activity.code", "error.activity.code.is.empty");
        }
    }

}
