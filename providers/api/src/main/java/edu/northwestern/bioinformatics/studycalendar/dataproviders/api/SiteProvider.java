package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface SiteProvider {
    Site getSite(String assignedIdentifier);
    List<Site> search(String partialName);
}
