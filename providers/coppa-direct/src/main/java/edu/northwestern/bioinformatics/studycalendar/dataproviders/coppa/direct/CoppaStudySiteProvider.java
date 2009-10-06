package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;

import java.util.ArrayList;
import java.util.List;

public class CoppaStudySiteProvider implements edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider{
    private StudySiteServiceClient client;

    public List<List<StudySite>> getAssociatedSites(List<Study> studies) {
        List<List<StudySite>> results = new ArrayList<List<StudySite>>();
        return results;
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
