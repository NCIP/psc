package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

// TODO: Why is this in the "xml" package?
public class SourceSerializer {

    private final static String ACTIVITY_NAME = "Name";
    private final static String ACTIVITY_TYPE = "Type";
    private final static String ACTIVITY_CODE = "Code";
    private final static String ACTIVITY_DESCRIPTION = "Description";
    private final static String ACTIVITY_SOURCE = "Source";

    private static final String[] COLUMNS = new String[] {
        ACTIVITY_NAME, ACTIVITY_TYPE, ACTIVITY_CODE, ACTIVITY_DESCRIPTION, ACTIVITY_SOURCE
    };

    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

    private SourceService sourceService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String createDocumentString(Source source, char delimiter) {
        StringWriter out = new StringWriter();
        CsvWriter writer = new CsvWriter(out, delimiter);

        try {
            writer.writeRecord(COLUMNS);
            for (Activity activity : source.getActivities()) {
                writer.write(activity.getName());
                writer.write(activity.getType().getNaturalKey());
                writer.write(activity.getCode());
                writer.write(activity.getDescription());
                writer.write(source.getNaturalKey());
                writer.endRecord();
            }
            writer.close();
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Error when building CSV in memory", e);
        }

        return out.toString();
    }

    public Source readDocument(InputStream inputStream) {
        Source source = null;
        List<Activity> activitiesToAddAndRemove = new ArrayList<Activity>();

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            CsvReader reader = new CsvReader(inputStreamReader);

            reader.readHeaders();
            //primitive check for the correct header for CSV files
            if (reader.getHeaderCount() != 5 && !reader.getHeader(0).equals(ACTIVITY_NAME)){
                throw new StudyCalendarValidationException(" The file format might be incorrect - Please verify the content of the file along with the selected extention");
            }
            while (reader.readRecord()) {
                String name = reader.get(ACTIVITY_NAME);
                String type = reader.get(ACTIVITY_TYPE);
                String code = reader.get(ACTIVITY_CODE);
                String desc = reader.get(ACTIVITY_DESCRIPTION);
                String sourceName = reader.get(ACTIVITY_SOURCE);

                Activity activity = new Activity();
                if (!StringUtils.isBlank(name)) {
                    activity.setName(name);
                } else {
                    throw new StudyCalendarValidationException("Activity name can not be empty or null for activities");
                }
                if (!StringUtils.isBlank(desc)) {
                    activity.setDescription(desc);
                }
                if (!StringUtils.isBlank(code)) {
                    activity.setCode(code);
                } else {
                    throw new StudyCalendarValidationException("Activity code can not be empty or null for activities");
                }
                if (source == null) {
                    source = new Source();
                    source.setName(sourceName);
                }
                if (!StringUtils.isBlank(type) && activityTypeDao.getByName(type) != null) {
                    activity.setType(activityTypeDao.getByName(type));
                } else {
                    throw new StudyCalendarValidationException(
                        "Activity type %s is invalid. Please choose from this list: %s.",
                        type, activityTypeDao.getAll());
                }
                if (source != null) {
                    if (!StringUtils.equals(sourceName, source.getName())) {
                        throw new StudyCalendarValidationException(
                            "All activities must belong to same source. %s and %s are not same source.",
                            source.getName(), sourceName);
                    }
                }
                if(!activitiesToAddAndRemove.isEmpty()){
                    for(Activity a:activitiesToAddAndRemove) {
                      if(activity.getName().equals(a.getName()) || activity.getCode().equals(a.getCode())) {
                              throw new StudyCalendarValidationException("Name and Code must be unique for activities within same source");
                        }
                    }
                }
                activitiesToAddAndRemove.add(activity);
            }
            reader.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new StudyCalendarSystemException("error importing csv file", e);
        }
        source.addNewActivities(activitiesToAddAndRemove);
        return source;
    }

    @Required
    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
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