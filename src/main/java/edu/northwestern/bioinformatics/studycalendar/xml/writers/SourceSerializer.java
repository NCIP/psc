package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractCsvXlsSerializer;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.service.SourceService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

import com.csvreader.CsvReader;
import org.jruby.RubyStruct;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class SourceSerializer implements AbstractCsvXlsSerializer<Source> {

    private final static String ACTIVITY_NAME = "Name";
    private final static String ACTIVITY_TYPE = "Type";
    private final static String ACTIVITY_CODE = "Code";
    private final static String ACTIVITY_DESCRIPTION = "Description";
    private final static String ACTIVITY_SOURCE = "Source";
    private final static String EMPTY_STRING = "";

    private final String NEW_STRING = "\n";
    private String[] arrayOfHeaders = new String[]{ACTIVITY_NAME, ACTIVITY_TYPE, ACTIVITY_CODE, ACTIVITY_DESCRIPTION, ACTIVITY_SOURCE};
    private SourceDao sourceDao;

    private SourceService sourceService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String createDocumentString(Source source, String delimeter) {
        StringBuffer sb = new StringBuffer();
        sb.append(constructHeader(delimeter));
        for (Activity a : source.getActivities()) {
            sb.append(a.getName());
            sb.append(delimeter);
            sb.append(a.getType().getName());
            sb.append(delimeter);
            sb.append((a.getCode() == null || (EMPTY_STRING).equals(a.getCode()) ? EMPTY_STRING : a.getCode()));
            sb.append(delimeter);
            sb.append((a.getDescription() == null || (EMPTY_STRING).equals(a.getDescription()) ? EMPTY_STRING : a.getDescription()));
            sb.append(delimeter);
            sb.append(source.getName());
            sb.append(NEW_STRING);
        }
        return sb.toString();
    }


    public String constructHeader(String delimiter) {
        StringBuffer sb = new StringBuffer();
        for (String header : arrayOfHeaders) {
            sb.append(header);
            sb.append(delimiter);
        }
        sb.append(NEW_STRING);
        return sb.toString();
    }

    public Source readDocument(InputStream inputStream) throws Exception {
        Source source = null;
        List<Activity> activitiesToAddAndRemove = new ArrayList<Activity>();


        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            CsvReader reader = new CsvReader(inputStreamReader);

            reader.readHeaders();
            while (reader.readRecord()) {
                String name = reader.get(ACTIVITY_NAME);
                String type = reader.get(ACTIVITY_TYPE);
                String code = reader.get(ACTIVITY_CODE);
                String desc = reader.get(ACTIVITY_DESCRIPTION);
                String sourceName = reader.get(ACTIVITY_SOURCE);

                Activity activity = new Activity();
                if (!StringUtils.isBlank(name)) {
                    activity.setName(name);
                }
                if (!StringUtils.isBlank(desc)) {
                    activity.setDescription(desc);
                }
                if (!StringUtils.isBlank(code)) {
                    activity.setCode(code);
                }

                if (source == null) {
                    source = sourceDao.getByName(sourceName);

                }

                if (!StringUtils.isBlank(type) && ActivityType.getByName(type) != null) {
                    activity.setType(ActivityType.getByName(type));
                } else {
                    String message = String.format("Activity type %s either does not exists or it is null. Please choose %s activity type only.", type, ActivityType.values());
                    logger.error(message);
                    throw new Exception(message);

                }
                if (source != null) {
                    if (!StringUtils.equals(sourceName, source.getName())) {
                        String message = String.format("All activities must belong to same source. %s and %s are not same source.", source.getName(), sourceName);
                        logger.error(message);
                        throw new Exception(message);
                    }
                } else {
                    String s = String.format("source %s does not exists.", sourceName);
                    logger.error(s);
                    throw new Exception(s);
                }
                activitiesToAddAndRemove.add(activity);


            }
            sourceService.updateSource(source, activitiesToAddAndRemove);
            reader.close();


        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new Exception("error importing csv file", e);

        }
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
}
