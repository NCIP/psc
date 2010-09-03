package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderConstants.*;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaProviderConstants.COPPA_STUDY_ASSIGNED_IDENTIFIER_TYPE;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import org.iso._21090.II;
import org.iso._21090.ST;
import org.osgi.framework.BundleContext;

import java.util.*;

public class CoppaStudyProvider implements StudyProvider {
    private BundleContext bundleContext;

    public CoppaStudyProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    public List<Study> getStudies(List<Study> parameters) {
        List<Study> studies = new ArrayList<Study>(parameters.size());
        for (Study param : parameters) {
            String extension = param.getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE);
            if (extension != null) {
                Id id = studyProtocolIdentifier(extension);

                StudyProtocol raw = CoppaProviderHelper.getCoppaAccessor(bundleContext).getStudyProtocol(id);

                Study found = createStudy(raw);
                studies.add(found);
            }
        }
        return studies;
    }

    public Study detect(Study param, Collection<Study> studies) {
        String extension = param.getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE);

        if (extension != null) {
            for (Study study : studies) {
                if (extension.equals(study.getSecondaryIdentifierValue(COPPA_STUDY_IDENTIFIER_TYPE))) {
                    return study;
                }
            }
        }

        return null;
    }

    @SuppressWarnings({ "unchecked" })
    public List<Study> search(String partialName) {
        StudyProtocol base = new StudyProtocol();

        ST titleTemplate = new ST();
        titleTemplate.setValue(partialName);
        base.setOfficialTitle(titleTemplate);

        StudyProtocol[] raw_by_title = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudyProtocols(base, smallLimit());

        base = new StudyProtocol();
        II assignedIdTemplate = new II();
        assignedIdTemplate.setExtension(partialName);
        // #1172 : added for COPPA 3.2
        assignedIdTemplate.setRoot(CoppaProviderHelper.STUDY_PROTOCOL_ROOT);
        base.setAssignedIdentifier(assignedIdTemplate);

        StudyProtocol[] raw_by_assigned_id = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudyProtocols(base, smallLimit());


        List<StudyProtocol> combined = new ArrayList<StudyProtocol>();
        if(raw_by_title != null && raw_by_title.length > 0) {
            combined.addAll(Arrays.asList(raw_by_title));
        }

        if (raw_by_assigned_id != null && raw_by_assigned_id.length > 0) {
            combined.addAll(Arrays.asList(raw_by_assigned_id));
        }

        Map<String, Study> dict = new LinkedHashMap<String, Study>();
        for (StudyProtocol protocol : combined) {
            if (protocol.getIdentifier() != null && !dict.containsKey(protocol.getIdentifier().getExtension())) {
                dict.put(protocol.getIdentifier().getExtension(), createStudy(protocol));
            }
        }

        return new ArrayList<Study>(dict.values());
    }

    public String providerToken() {
        return CoppaProviderConstants.PROVIDER_TOKEN;
    }

    //////////// Search Helper Methods

    private static LimitOffset smallLimit() {
        LimitOffset lo = new LimitOffset();
        lo.setLimit(40); // Limit since autocomplete might return too many.
        lo.setOffset(0);
        return lo;
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

    private Map<String, String> extractSecondaryIdentifiers(StudyProtocol p)  {
        MapBuilder<String, String> ids =
                new MapBuilder<String, String>();

        if (p.getIdentifier() != null) {
            ids.put(COPPA_STUDY_IDENTIFIER_TYPE, p.getIdentifier().getExtension());
        }

        if (p.getAssignedIdentifier() != null) {
            ids.put(COPPA_STUDY_ASSIGNED_IDENTIFIER_TYPE, p.getAssignedIdentifier().getExtension());
        }

        if (p.getPublicTitle() != null && p.getPublicTitle().getValue() != null) {
            ids.put(COPPA_STUDY_PUBLIC_TITLE_TYPE, p.getPublicTitle().getValue());
        }

        if (p.getOfficialTitle() != null && p.getOfficialTitle().getValue() != null) {
            ids.put(COPPA_STUDY_OFFICIAL_TITLE_TYPE, p.getOfficialTitle().getValue());
        }

        if (p.getIdentifier() != null) {
            Id studyProtocolId = studyProtocolIdentifier(
                    p.getIdentifier().getExtension());

            String leadOrgIdent = findLeadOrganizationIdentifier(studyProtocolId);

            if (leadOrgIdent != null) {
                ids.put(COPPA_LEAD_ORGANIZATION_IDENTIFIER_TYPE, leadOrgIdent);
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

    // TODO: CoppaStudySiteProvider might be a better place for this
    private Id studyProtocolIdentifier(String extension) {
        Id id = new Id();
        id.setExtension(extension);
        return id;
    }

    private String findLeadOrganizationIdentifier(Id studyProtocolId) {
        StudySite[] studySite = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(studyProtocolId);
        if (studySite != null) {
            for(StudySite s: studySite) {
                // This is how we find the Organization which is leading
                // the StudyProtocol.
                if ("Lead Organization".equals(s.getFunctionalCode().getCode()) && s.getLocalStudyProtocolIdentifier() != null) {
                    return s.getLocalStudyProtocolIdentifier().getValue();
                }
            }            
        }
        return null;
    }
}
