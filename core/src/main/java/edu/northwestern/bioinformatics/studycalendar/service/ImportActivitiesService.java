package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.SourceSerializer;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class ImportActivitiesService {
    private SourceDao sourceDao;
    private ActivitySourceXmlSerializer xmlSerializer;
    private SourceSerializer sourceSerializer;

    public Source loadAndSave(InputStream sourcesXml) {
        Collection<Source> sources = readData(sourcesXml);
        List<Source> validSources = replaceCollidingSources(sources);

        save(validSources);
        return validSources.iterator().next();
    }

    public Source loadAndSaveCSVFile(InputStream inputStream) {
        return sourceSerializer.readDocument(inputStream);
    }

    protected Collection<Source> readData(InputStream dataFile) {
        return xmlSerializer.readCollectionOrSingleDocument(dataFile);
    }

    protected List<Source> replaceCollidingSources(Collection<Source> sources) {
        List<Source> validSources = new ArrayList<Source>();
        List<Source> existingSources = sourceDao.getAll();

        for (Source source : sources) {

            if (existingSources.contains(source)) {

                Source existingSource = existingSources.get(existingSources.indexOf(source));

                for (Activity activity : source.getActivities()) {
                    activity.setSource(existingSource);
                }

                for (Activity activity : source.getActivities()) {
                    if (!existingSource.getActivities().contains(activity)) {
                        existingSource.getActivities().add(activity);
                    }
                }

                source = existingSource;
            }

            validSources.add(source);
        }

        return validSources;
    }

    protected void save(List<Source> sources) {
        for (Source source : sources) {
            sourceDao.save(source);
        }
    }

    //// Field Setters

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setXmlSerializer(ActivitySourceXmlSerializer serializer) {
        xmlSerializer = serializer;
    }

    @Required
    public void setSourceSerializer(SourceSerializer sourceSerializer) {
        this.sourceSerializer = sourceSerializer;
    }
}
