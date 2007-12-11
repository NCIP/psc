package edu.northwestern.bioinformatics.studycalendar.utils.dataloaders;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Dzak
 */
public class MultipartFileActivityLoader {

    private SourceDao sourceDao;
    private ActivityXmlReader activityXmlReader;

    public void loadData(MultipartFile dataFile) throws Exception {
        List<Source> sources = readData(dataFile.getInputStream());
        List<Source> validSources = replaceCollidingSources(sources);
        save(validSources);
    }

    protected List<Source> readData(InputStream dataFile) throws Exception{
        return activityXmlReader.read(dataFile);
    }

    protected List<Source> replaceCollidingSources(List<Source> sources) throws Exception {
        List<Source> validSources = new ArrayList<Source>();
        List<Source> existingSources = sourceDao.getAll();

        for (Source source : sources ) {

            if (existingSources.contains(source)) {

                Source existingSource = existingSources.get(existingSources.indexOf(source));

                for (Activity activity : source.getActivities()) {
                    activity.setSource(existingSource);
                }

                existingSource.getActivities().addAll(source.getActivities());
                source = existingSource;
            }

            validSources.add(source);
        }

        return validSources;
    }

    protected void save(List<Source> sources) {
        for (Source source : sources)  {
            sourceDao.save(source);
        }
    }

    //// Field Setters
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setActivityXmlReader(ActivityXmlReader activityXmlReader) {
        this.activityXmlReader = activityXmlReader;
    }
}
