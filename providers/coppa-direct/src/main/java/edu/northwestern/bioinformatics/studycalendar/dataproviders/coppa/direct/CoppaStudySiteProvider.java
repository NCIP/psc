package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.coppa.po.ResearchOrganization;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import gov.nih.nci.coppa.services.structuralroles.researchorganization.client.ResearchOrganizationClient;
import org.iso._21090.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.axis.types.URI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider{
    private static final String STUDY_SITE_ENDPOINT =
        "http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudySiteService";
    private static final String RESEARCH_ORG_ENDPOINT =
        "http://ctms-services-po-2-2-integration.nci.nih.gov/wsrf/services/cagrid/ResearchOrganization";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudySiteServiceClient client;
    private ResearchOrganizationClient researchOrgClient;

    CoppaStudySiteProvider() {
        try {
            client = new StudySiteServiceClient(STUDY_SITE_ENDPOINT);
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
            String extension = study.getSecondaryIdentifierValue("extension");
            gov.nih.nci.coppa.services.pa.StudySite[] studySites = getByStudyProtocol(extension);
            if (studySites == null || studySites.length == 0) {
                results.add(null);
            } else {
                gov.nih.nci.coppa.po.Id[] researchOrgIds = getResearchOrgIds(studySites);
                ResearchOrganization[] researchOrgs = getResearchOrgsByIds(researchOrgIds);
//                getOrgPlayerIds(researchOrgs[0].get);
//                CoppaProviderHelper.pscSites();
            }
        }
        return results;
    }

    private ResearchOrganization[] getResearchOrgsByIds(gov.nih.nci.coppa.po.Id[] researchOrgIds) {
        try {
            return researchOrgClient.getByIds(researchOrgIds);
        } catch (RemoteException e) {
            log.error("COPPA research organization search failed", e);
            return new ResearchOrganization[0];
        }
    }

    private gov.nih.nci.coppa.po.Id[] getResearchOrgIds(gov.nih.nci.coppa.services.pa.StudySite[] studySites) {
        List<gov.nih.nci.coppa.po.Id> ids = new ArrayList<gov.nih.nci.coppa.po.Id>();
        for(gov.nih.nci.coppa.services.pa.StudySite studySite : studySites) {
            II ii = studySite.getResearchOrganization();
            ids.add((gov.nih.nci.coppa.po.Id) ii);
        }
        return ids.toArray(new gov.nih.nci.coppa.po.Id[0]);
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

    public List<List<StudySite>> getAssociatedStudies(List<Site> sites) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String providerToken() {
        return "coppa-direct";
    }

    public void setClient(StudySiteServiceClient client) {
        this.client = client;
    }
}
