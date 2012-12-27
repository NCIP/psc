/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
    private SourceService sourceService;
    private ActivityService activityService;

    public Source loadAndSave(InputStream sourcesXml) {
        Collection<Source> sources = readData(sourcesXml);
        List<Source> validSources = replaceCollidingSources(sources);
        if (validSources.size() > 0) {
            return validSources.iterator().next();
        } else if (sources.size() > 0) {
            return sources.iterator().next();
        } else {
            return null;
        }
    }

    public Source loadAndSaveCSVFile(InputStream inputStream) {
        Source source = sourceSerializer.readDocument(inputStream);
        List<Activity> activitiesToAddOrRemove = source.getActivities();
        String sourceName = source.getName();
        Source existingSource = sourceDao.getByName(sourceName);
        if (existingSource == null) {
            sourceDao.save(source);
            existingSource = source;
        }
        sourceService.updateSource(existingSource, activitiesToAddOrRemove);
        return existingSource;
    }

    protected Collection<Source> readData(InputStream dataFile) {
        return xmlSerializer.readCollectionOrSingleDocument(dataFile);
    }

    // TODO: this is really inefficient if there are lots of sources
    protected List<Source> replaceCollidingSources(Collection<Source> sources) {
        List<Source> validSources = new ArrayList<Source>();
        List<Source> existingSources = sourceDao.getAll();

        for (Source source : sources) {
            if (existingSources.contains(source)) {
                Source existingSource = existingSources.get(existingSources.indexOf(source));
                resolveActivityType(source.getActivities());
                sourceService.updateSource(existingSource, source.getActivities());
                validSources.add(existingSource);
            } else {
                //means new source
                resolveActivityType(source.getActivities());
                sourceService.updateSource(source, source.getActivities());
                validSources.add(source);
            }
        }
        return validSources;
    }

    protected void save(List<Source> sources) {
        for (Source source : sources) {
            sourceDao.save(source);
        }
    }

    protected void resolveActivityType(List<Activity> activities) {
        for (Activity activity : activities) {
            activityService.resolveAndSaveActivityType(activity);
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
    
    @Required
    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @Required
    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }
}
