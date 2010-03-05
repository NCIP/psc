package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.iso._21090.II;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import static java.util.Collections.EMPTY_LIST;
import java.util.List;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider, RefreshableProvider {
    private BundleContext bundleContext;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CoppaStudySiteProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>(studies.size());

        for (Study study : studies) {
            String ext = study.getSecondaryIdentifierValue(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
            if (ext != null) {
                Id id = new Id();
                id.setExtension(ext);
                gov.nih.nci.coppa.services.pa.StudySite[] studySites = getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(id);

                if (studySites == null || studySites.length == 0) {
                    results.add(EMPTY_LIST);
                } else {
                    II[] researchOrgIIs = getResearchOrganizationIds(studySites);

                    gov.nih.nci.coppa.po.Id[] researchOrgIds = tranformIds(gov.nih.nci.coppa.po.Id.class, researchOrgIIs);
                    ResearchOrganization[] researchOrgs = getCoppaAccessor(bundleContext).getResearchOrganizations(researchOrgIds);

                    II[] orgIIs = getPlayerIds(researchOrgs);

                    gov.nih.nci.coppa.po.Id[] orgIDs = tranformIds(gov.nih.nci.coppa.po.Id.class, orgIIs);
                    Organization[] organizations = getOrganizationsById(orgIDs);

                    List<Site> sites = pscSites(organizations);

                    results.add(buildStudySites(sites));
                }
            } else {
                results.add(EMPTY_LIST);
            }
        }

        return results;
    }

    private Organization[] getOrganizationsById(gov.nih.nci.coppa.po.Id[]  ids) {
        List<Organization> orgs = new ArrayList<Organization>();
        for (gov.nih.nci.coppa.po.Id id : ids) {
            Organization org = getCoppaAccessor(bundleContext).getOrganization(id);
            if (org != null) {
                orgs.add(org);
            }
        }
        return orgs.toArray(new Organization[0]);
    }


    private II[] getResearchOrganizationIds(gov.nih.nci.coppa.services.pa.StudySite[] studySites) {
        List<II> ids = new ArrayList<II>();
        for(gov.nih.nci.coppa.services.pa.StudySite studySite : studySites) {
            II ii = studySite.getResearchOrganization();
            ids.add(ii);
        }
        return ids.toArray(new II[0]);
    }

    private List<StudySite> buildStudySites(List<Site> sites) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        for (Site site : sites) {
            StudySite ss = new StudySite();
            ss.setSite(site);
            studySites.add(ss);
        }
        return studySites;
    }

    public List<List<StudySite>> getAssociatedStudies(List<Site> sites) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>(sites.size());

        for (Site s : sites) {

            List<StudySite> provided = null;

            if (isNotBlank(s.getAssignedIdentifier())) {

                gov.nih.nci.coppa.po.Id orgId = new gov.nih.nci.coppa.po.Id();
                orgId.setExtension(s.getAssignedIdentifier());
                orgId.setRoot("2.16.840.1.113883.3.26.4.2");

                ResearchOrganization[] ros = getCoppaAccessor(bundleContext).getResearchOrganizationsByPlayerIds(new gov.nih.nci.coppa.po.Id[]{orgId});

                if (ros != null && ros[0] != null) {

                    provided = new ArrayList<StudySite>();

                    for (II roII : ros[0].getIdentifier().getItem()) {
                        if (!roII.getIdentifierName().equals("NCI Research Organization identifier")) {
                            break;
                        }

                        gov.nih.nci.coppa.services.pa.StudySite param = new gov.nih.nci.coppa.services.pa.StudySite();
                        param.setResearchOrganization(roII);

                        LimitOffset l = new LimitOffset();
                        l.setLimit(250);

                        gov.nih.nci.coppa.services.pa.StudySite[] raw = getCoppaAccessor(bundleContext).searchStudySitesByStudySite(param, l);

                        if (raw != null && raw.length > 0) {

                            for (gov.nih.nci.coppa.services.pa.StudySite coppaStudySite : raw) {

                                String coppaProtocolId = coppaStudySite.getStudyProtocolIdentifier().getExtension();

                                StudySecondaryIdentifier ssid = new StudySecondaryIdentifier();
                                ssid.setType(CoppaProviderConstants.COPPA_STUDY_IDENTIFIER_TYPE);
                                ssid.setValue(coppaProtocolId);

                                Study study = new Study();
                                study.addSecondaryIdentifier(ssid);

                                StudySite transformed = new StudySite(study, null);
                                provided.add(transformed);
                            }
                        }
                    }
                }
            }
            results.add(provided);
        }
        return results;
    }

    public String providerToken() {
        return CoppaProviderConstants.PROVIDER_TOKEN;
    }

    public Integer getRefreshInterval() {
        return 15;
    }
}
