package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Facade for accessing all configured {@link StudyProvider}s.
 *
 * @author Rhett Sutphin
 */

public class StudyConsumer {
    private OsgiLayerTools osgiLayerTools;

    // TODO: this is just a stub implementation
    public List<Study> search(String partialName) {
        StudyProvider service = osgiLayerTools.getRequiredService(StudyProvider.class);
        return service.search(partialName);
    }

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }
}
