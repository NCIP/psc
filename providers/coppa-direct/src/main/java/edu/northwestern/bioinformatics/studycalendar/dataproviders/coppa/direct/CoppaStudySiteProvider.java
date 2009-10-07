package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudySiteServiceClient client;

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>(studies.size());

        for (Study study : studies) {
            String extension = study.getSecondaryIdentifierValue("extension");
            gov.nih.nci.coppa.services.pa.StudySite[] studySites = getByStudyProtocol(extension);
            if (studySites == null || studySites.length == 0) {
                results.add(null);
            }
        }
        return results;
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
