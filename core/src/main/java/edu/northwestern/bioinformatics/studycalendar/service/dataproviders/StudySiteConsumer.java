package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudySiteConsumer extends AbstractConsumer<StudySite, StudySiteProvider> {
    @Override protected Class<StudySiteProvider> providerType() { return StudySiteProvider.class; }

    public StudySite refresh(StudySite in) {
        return refresh(Arrays.asList(in)).get(0);
    }

    public List<StudySite> refresh(List<StudySite> in) {
        return new Refresh().execute(in);
    }

    private class Refresh extends AbstractRefresh {
        @Override
        protected List<StudySite> loadNewVersions(StudySiteProvider provider, List<StudySite> targetStudySites) {
            List<StudySite> results = new ArrayList<StudySite>(targetStudySites.size());

            List<Study> targetStudies = new ArrayList <Study>();
            for (StudySite targetStudySite : targetStudySites) {
                targetStudies.add(targetStudySite.getStudy());
            }

            // Get StudySites from the Provider
            List<List<StudySite>> provided = provider.getAssociatedSites(targetStudies);

            // Lets validate each Study/Site association still exists.... Ready... Go!!
            for (StudySite targetStudySite : targetStudySites) {
                int i = targetStudySites.indexOf(targetStudySite);

                // Both the target StudySites and the provided StudySites should correspond by index (see StudySiteProvider Contract)
                for (StudySite providedStudySite : provided.get(i)) {

                    // Since we already searched the provider by Study, to make sure
                    // the StudySite still exists, all we have to do is check that the site
                    // has the correct site.
                    if (providedStudySite.getSite().equals(targetStudySite.getSite())) {
                        results.add(i, targetStudySite);        // StudySite still exists!!!
                        break;
                    }
                }
            }
            return results;
        }

        @Override
        protected void updateInstanceInPlace(StudySite current, StudySite newVersion) {
            if (newVersion == null) {
                current = null;
            } else {
                current.setLastRefresh(newVersion.getLastRefresh());
            }
        }
    }
}