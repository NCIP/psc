package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.entities.organization.client.OrganizationClient;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
import org.apache.axis.types.URI;
import org.iso._21090.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider {
    private static final String STUDY_SITE_ENDPOINT =
        "http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudySiteService";
    private static final String ORGANIZATION_ENDPOINT =
            "http://ctms-services-po-2-2-integration.nci.nih.gov/wsrf/services/cagrid/Organization";
    private static final String RESEARCH_ORG_ENDPOINT =
        "http://ctms-services-po-2-2-integration.nci.nih.gov/wsrf/services/cagrid/ResearchOrganization";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudySiteServiceClient client;
    private OrganizationClient organizationClient;
    private ResearchOrganizationClient researchOrgClient;

    CoppaStudySiteProvider() {
        try {
            client = new StudySiteServiceClient(STUDY_SITE_ENDPOINT);
            organizationClient = new OrganizationClient(ORGANIZATION_ENDPOINT);
            researchOrgClient = new ResearchOrganizationClient(RESEARCH_ORG_ENDPOINT);
        } catch (URI.MalformedURIException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>(studies.size());

        for (Study study : studies) {
            String ext = study.getSecondaryIdentifierValue("extension");
            gov.nih.nci.coppa.services.pa.StudySite[] studySites = getByStudyProtocol(ext);

            if (studySites == null || studySites.length == 0) {
                results.add(null);
            } else {
                II[] researchOrgIds = getResearchOrganizationIds(studySites);

                ResearchOrganization[] researchOrgs = getResearchOrganizationsByIds(researchOrgIds);

                II[] orgIds = getPlayerIds(researchOrgs);

                Organization[] organizations = getOrganizationsById(orgIds);

                List<Site> sites = pscSites(organizations);

                results.add(buildStudySites(sites));
            }
        }

        return results;
    }

    private Organization[] getOrganizationsById(II[] iis) {
        gov.nih.nci.coppa.po.Id[] ids = tranformIds(gov.nih.nci.coppa.po.Id.class, iis);

        try {
            List<Organization> orgs = new ArrayList<Organization>();
            for (gov.nih.nci.coppa.po.Id id : ids) {
                Organization org = organizationClient.getById(id);
                if (org != null) {
                    orgs.add(org);
                }
            }
            return orgs.toArray(new Organization[0]);
        } catch (RemoteException e) {
            log.error("COPPA organization search failed", e);
            return new Organization[0];
        }
    }

    private ResearchOrganization[] getResearchOrganizationsByIds(II[] iis) {
        gov.nih.nci.coppa.po.Id[] ids = tranformIds(gov.nih.nci.coppa.po.Id.class, iis);
        try {
            return researchOrgClient.getByIds(ids);
        } catch (RemoteException e) {
            log.error("COPPA research organization search failed", e);
            return new ResearchOrganization[0];
        }
    }

    private II[] getResearchOrganizationIds(gov.nih.nci.coppa.services.pa.StudySite[] studySites) {
        List<II> ids = new ArrayList<II>();
        for(gov.nih.nci.coppa.services.pa.StudySite studySite : studySites) {
            II ii = studySite.getResearchOrganization();
            ids.add(ii);
        }
        return ids.toArray(new II[0]);
    }

    private gov.nih.nci.coppa.services.pa.StudySite[] getByStudyProtocol(String extension) {
        Id id = new Id();
        id.setExtension(extension);
        try {
            return client.getByStudyProtocol(id);
        } catch (RemoteException e) {
            log.error("COPPA study site search failed", e);
            return new gov.nih.nci.coppa.services.pa.StudySite[0];
        }
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
        throw new UnsupportedOperationException("Get associated studies not implemented yet.");
    }

    public String providerToken() {
        return CoppaProviderConstants.PROVIDER_TOKEN;
    }

    public void setClient(StudySiteServiceClient client) {
        this.client = client;
    }

    public void setResearchOrganizationClient(ResearchOrganizationClient researchOrgClient) {
        this.researchOrgClient = researchOrgClient;
    }

    public void setOrganizationClient(OrganizationClient organizationClient) {
        this.organizationClient = organizationClient;
    }
}
