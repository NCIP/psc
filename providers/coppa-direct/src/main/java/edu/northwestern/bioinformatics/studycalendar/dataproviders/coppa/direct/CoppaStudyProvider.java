package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
import org.apache.axis.types.URI;
import org.iso._21090.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoppaStudyProvider implements StudyProvider {
    private static final String TEST_ENDPOINT =
        "http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudyProtocolService";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyProtocolServiceClient client;

    public CoppaStudyProvider() {
        try {
            // Temporary
            setClient(new StudyProtocolServiceClient(TEST_ENDPOINT));
        } catch (URI.MalformedURIException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Study> getStudies(List<Study> parameters) {
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public List<Study> search(String partialName) {
        StudyProtocol base = new StudyProtocol();

        II ii = new II();
        ii.setIdentifierName(partialName);
        base.setAssignedIdentifier(ii);

        StudyProtocol[] raw = searchByStudyProtocol(base);
        if (raw == null) {
            return Collections.emptyList();
        } else {
            List<Study> results = new ArrayList<Study>(raw.length);
            for (StudyProtocol protocol : raw) {
                results.add(createStudy(protocol));
            }
            return results;
        }
    }

    private StudyProtocol[] searchByStudyProtocol(StudyProtocol protocol) {
        LimitOffset lo = new LimitOffset();
        lo.setLimit(Integer.MAX_VALUE);
        lo.setOffset(0);

        try {
            return client.search(protocol, lo);
        } catch(RemoteException e) {
            log.error("COPPA study protocol search failed", e);
            return new StudyProtocol[0];
        }
    }

    // Package level for testing
    Study createStudy(StudyProtocol p) {
        Study s = new Study();

        s.setAssignedIdentifier(
            p.getAssignedIdentifier().getExtension());

        s.setLongTitle(
            p.getOfficialTitle().getValue());

        addSecondaryIdentifiers(p, s);

        return s;
    }

    private void addSecondaryIdentifiers(StudyProtocol p, Study s) {
        MapBuilder<String, String> secondaryTitles =
                new MapBuilder<String, String>();

        if (p.getAssignedIdentifier() != null) {
            secondaryTitles
                .put("extension", p.getAssignedIdentifier().getExtension())
                .put("root", p.getAssignedIdentifier().getRoot());
        }

        if (p.getPublicTitle() != null) {
            secondaryTitles.put("publicTitle", p.getPublicTitle().getValue());
        }

        if (p.getOfficialTitle() != null) {
            secondaryTitles.put("officialTitle", p.getOfficialTitle().getValue());
        }


        for (Map.Entry<String, String> entry : secondaryTitles.toMap().entrySet()) {
            StudySecondaryIdentifier si = new StudySecondaryIdentifier();
            si.setType(entry.getKey());
            si.setValue(entry.getValue());
            s.addSecondaryIdentifier(si);
        }
    }

    public String providerToken() {
        return "coppa-direct";
    }

    public void setClient(StudyProtocolServiceClient c) {
        client = c;
    }
}
