package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import org.apache.axis.types.URI;
import org.iso._21090.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;

public class CoppaStudyProvider implements StudyProvider {
    private static final String TEST_ENDPOINT =
        "http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudyProtocolService";
    private static final String STUDY_SITE_ENDPOINT =
        "http://ctms-services-pa-integration.nci.nih.gov/wsrf/services/cagrid/StudySiteService";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyProtocolServiceClient client;
    private StudySiteServiceClient studySiteClient;

    public CoppaStudyProvider() {
        try {
            // Temporary
            setClient(new StudyProtocolServiceClient(TEST_ENDPOINT));
            setStudySiteClient(new StudySiteServiceClient(STUDY_SITE_ENDPOINT));
        } catch (URI.MalformedURIException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Study> getStudies(List<Study> parameters) {
        List<Study> studies = new ArrayList<Study>(parameters.size());
        for (Study param : parameters) {
            String extension = param.getSecondaryIdentifierValue("extension");
            if (extension != null) {
                Id id = studyProtocolIdentifier(extension);

                StudyProtocol raw = getStudyProtocol(id);

                Study found = createStudy(raw);
                studies.add(found);
            }
        }
        return studies;
    }

    // TODO: implement this
    public Study detect(Study param, Collection<Study> studies) {
        throw new UnsupportedOperationException("find not implemented");
    }

    public List<Study> search(String partialName) {
        StudyProtocol base = new StudyProtocol();

        II ii = new II();
        ii.setIdentifierName(partialName);
        base.setAssignedIdentifier(ii);

        StudyProtocol[] raw = searchStudyProtocols(base);
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

    public String providerToken() {
        return "coppa-direct";
    }

    //////////// Search Helper Methods

    private StudyProtocol getStudyProtocol(Id id) {
        try {
            return client.getStudyProtocol(id);
        } catch(RemoteException e) {
            log.error("COPPA study protocol search failed", e);
            return null;
        }
    }
    private StudyProtocol[] searchStudyProtocols(StudyProtocol protocol) {
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

    //////////// Object creation helpers

    Study createStudy(StudyProtocol p) {
        Study s = null;
        
        if (p != null) {
            s = new Study();

            s.setAssignedIdentifier(
                p.getAssignedIdentifier().getExtension());

            s.setLongTitle(
                p.getOfficialTitle().getValue());

            Map<String, String> ids = extractSecondaryIdentifiers(p);
            addSecondaryIdentifiers(s, ids);
        }

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

        if (p.getAssignedIdentifier() != null) {
            Id studyProtocolId = studyProtocolIdentifier(
                    p.getAssignedIdentifier().getExtension());

            String localStudyProtocolIdentifier = searchLocalStudyProtocolIdentifier(studyProtocolId);

            if (localStudyProtocolIdentifier != null) {
                ids.put("localStudyProtocolIdentifier", localStudyProtocolIdentifier);
            }
        }

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

    // TODO: StudySiteProvider might be a better place for this
    private Id studyProtocolIdentifier(String extension) {
        Id id = new Id();
        id.setExtension(extension);
        return id;
    }

    private String searchLocalStudyProtocolIdentifier(Id id) {
        StudySite[] studySite = searchStudySiteByStudyProtocolId(id);
        if (studySite != null) {
            for(StudySite s: studySite) {
                // This is how we find the Organization which is leading
                // the StudyProtocol.
                if (s.getFunctionalCode().getCode() == "Lead Organization" && s.getLocalStudyProtocolIdentifier() != null) {
                    return s.getLocalStudyProtocolIdentifier().getValue();
                }
            }            
        }
        return null;
    }

    private StudySite[] searchStudySiteByStudyProtocolId(Id id) {
        try {
            return studySiteClient.getByStudyProtocol(id);
        } catch (Exception e) {
            log.error("COPPA study site search failed", e);
            return new StudySite[0];
        }
    }

    //////////// Setters and Getters

    public void setClient(StudyProtocolServiceClient c) {
        client = c;
    }

    public void setStudySiteClient(StudySiteServiceClient s) {
        this.studySiteClient = s;
    }
}
