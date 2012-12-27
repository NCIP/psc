/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class MockStudySiteProvider implements StudySiteProvider, RefreshableProvider {
    private List<MockStudySiteMapping> pairs;

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> result = new ArrayList<List<StudySite>>(studies.size());
        for (Study study : studies) {
            String nct = study.getSecondaryIdentifierValue(MockDataProviderTools.KEY_STUDY_IDENTIFIER_TYPE);
            List<StudySite> associations = null;
            if (nct != null) {
                Collection<Site> idents = sitesFor(nct);
                if (!idents.isEmpty()) {
                    associations = new ArrayList<StudySite>();
                    for (Site site : idents) {
                        StudySite ss = new StudySite();
                        ss.setSite(site);
                        associations.add(ss);
                    }
                }
            }
            result.add(associations);
        }
        return result;
    }

    public List<List<StudySite>> getAssociatedStudies(List<Site> sites) {
        List<List<StudySite>> result = new ArrayList<List<StudySite>>(sites.size());
        for (Site site : sites) {
            String ident = site.getAssignedIdentifier();
            List<StudySite> associations = null;
            if (ident != null) {
                Collection<Study> studies = studiesFor(ident);
                if (!studies.isEmpty()) {
                    associations = new ArrayList<StudySite>();
                    for (Study study : studies) {
                        StudySite ss = new StudySite();
                        ss.setStudy(study);
                        associations.add(ss);
                    }
                }
            }
            result.add(associations);
        }
        return result;
    }

    public String providerToken() {
        return MockDataProviderTools.PROVIDER_TOKEN;
    }

    private Collection<Study> studiesFor(String siteIdent) {
        List<Study> studies = new LinkedList<Study>();
        for (MockStudySiteMapping pair : pairs) {
            if (pair.getSiteIdentifier().equals(siteIdent)) {
                StudySecondaryIdentifier ssi = new StudySecondaryIdentifier();
                ssi.setType(MockDataProviderTools.KEY_STUDY_IDENTIFIER_TYPE);
                ssi.setValue(pair.getStudyNct());
                Study s = new Study();
                s.addSecondaryIdentifier(ssi);
                studies.add(s);
            }
        }
        return studies;
    }

    private Collection<Site> sitesFor(String nct) {
        List<Site> sites = new LinkedList<Site>();
        for (MockStudySiteMapping pair : pairs) {
            if (pair.getStudyNct().equals(nct)) {
                Site s = new Site();
                s.setAssignedIdentifier(pair.getSiteIdentifier());
                sites.add(s);
            }
        }
        return sites;
    }

    public Integer getRefreshInterval() {
        return 15;
    }


    ////// CONFIGURATION

    public void setPairs(List<MockStudySiteMapping> pairs) {
        this.pairs = pairs;
    }
}
