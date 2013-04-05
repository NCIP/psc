/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.ConnectionSource;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class SampleActivitySourceInitializer extends RowPreservingInitializer {
    private Resource xml;
    private ImportActivitiesService importActivitiesService;

    public SampleActivitySourceInitializer() {
        super("sources");
    }

    @Override
    public void oneTimeSetup(ConnectionSource connectionSource) {
        super.oneTimeSetup(connectionSource);
        try {
            importActivitiesService.loadAndSave(xml.getInputStream());
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Could not read from %s", xml);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setXmlResource(Resource xmlResource) {
        this.xml = xmlResource;
    }

    @Required
    public void setImportActivitiesService(ImportActivitiesService importActivitiesService) {
        this.importActivitiesService = importActivitiesService;
    }
}
