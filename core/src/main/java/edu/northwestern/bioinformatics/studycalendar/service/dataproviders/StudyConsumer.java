/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Facade for accessing all configured {@link StudyProvider}s.
 *
 * @author Rhett Sutphin
 */
public class StudyConsumer extends AbstractConsumer<Study, StudyProvider> {
    @Override protected Class<StudyProvider> providerType() { return StudyProvider.class; }

    public List<Study> search(String partialName) {
        return super.doSearch(partialName);
    }

    public Study refresh(Study in) {
        return refresh(Arrays.asList(in)).get(0);
    }

    public List<Study> refresh(List<Study> in) {
        return new Refresh().execute(in);
    }

    private class Refresh extends AbstractRefresh {
        @Override
        protected List<Study> loadNewVersions(StudyProvider provider, List<Study> targetSites) {
            return provider.getStudies(targetSites);
        }

        @Override
        protected void updateInstanceInPlace(Study current, Study newVersion) {
            current.setLastRefresh(newVersion.getLastRefresh());
            current.setLongTitle(newVersion.getLongTitle());

            // Remove dereferenced idents
            for (Iterator<StudySecondaryIdentifier> it = current.getSecondaryIdentifiers().iterator(); it.hasNext();) {
                StudySecondaryIdentifier currentIdent = it.next();
                if (!newVersion.getSecondaryIdentifiers().contains(currentIdent)) {
                    it.remove();
                }
            }

            // Add new idents
            for (StudySecondaryIdentifier newIdent : newVersion.getSecondaryIdentifiers()) {
                if (!current.getSecondaryIdentifiers().contains(newIdent)) {
                    current.addSecondaryIdentifier(newIdent.clone());
                }
            }
        }
    }
}
