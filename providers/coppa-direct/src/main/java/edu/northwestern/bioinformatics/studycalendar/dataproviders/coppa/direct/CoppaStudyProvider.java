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
import java.util.*;

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

        Map<String, String> ids = extractSecondaryIdentifiers(p);
        addSecondaryIdentifiers(s, ids);

        return s;
    }

    private Map<String, String> extractSecondaryIdentifiers(StudyProtocol p )  {
        MapBuilder<String, String> ids =
                new MapBuilder<String, String>();

        if (p.getAssignedIdentifier() != null) {
            ids.put("extension", p.getAssignedIdentifier().getExtension());
        }

        if (p.getPublicTitle() != null) {
            ids.put("publicTitle", p.getPublicTitle().getValue());
        }

        if (p.getOfficialTitle() != null) {
            ids.put("officialTitle", p.getOfficialTitle().getValue());
        }

//        try {
//            StudySiteServiceClient sc = new StudySiteServiceClient("http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudyProtocolService");
//            Id id = new Id();
//            gov.nih.nci.coppa.services.pa.StudySite[] sss = sc.getByStudyProtocol(id);
//            for(gov.nih.nci.coppa.services.pa.StudySite ss: sss) {
//
//                if (ss.getLocalStudyProtocolIdentifier().getValue())
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return ids.toMap();
    }

    private void addSecondaryIdentifiers(Study s, Map<String, String> ids) {
        for (Map.Entry<String, String> entry : ids.entrySet()) {
            StudySecondaryIdentifier si = new StudySecondaryIdentifier();
            si.setType(entry.getKey());
            si.setValue(entry.getValue());
            s.addSecondaryIdentifier(si);
        }
    }

    // TODO: implement this
    public Study detect(Study param, Collection<Study> studies) {
        throw new UnsupportedOperationException("find not implemented");
    }

    public String providerToken() {
        return "coppa-direct";
    }

    public void setClient(StudyProtocolServiceClient c) {
        client = c;
    }
}
