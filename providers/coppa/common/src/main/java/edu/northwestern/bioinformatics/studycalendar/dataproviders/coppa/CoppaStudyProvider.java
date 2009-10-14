package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.coppa.common.LimitOffset;
import gov.nih.nci.coppa.services.pa.Id;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import org.iso._21090.II;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoppaStudyProvider implements StudyProvider {
    private BundleContext bundleContext;

    public CoppaStudyProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public List<Study> getStudies(List<Study> parameters) {
        List<Study> studies = new ArrayList<Study>(parameters.size());
        for (Study param : parameters) {
            String extension = param.getSecondaryIdentifierValue("extension");
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
        String extension = param.getSecondaryIdentifierValue("extension");

        if (extension != null) {
            for (Study study : studies) {
                if (extension.equals(study.getSecondaryIdentifierValue("extension"))) {
                    return study;
                }
            }
        }

        return null;
    }

    public List<Study> search(String partialName) {
        StudyProtocol base = new StudyProtocol();

        II ii = new II();
        ii.setIdentifierName(partialName);
        base.setAssignedIdentifier(ii);

        StudyProtocol[] raw = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudyProtocols(base, noLimit());
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
        return "coppa";
    }

    //////////// Search Helper Methods

    private static LimitOffset noLimit() {
        LimitOffset lo = new LimitOffset();
        lo.setLimit(Integer.MAX_VALUE);
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

    // TODO: CoppaStudySiteProvider might be a better place for this
    private Id studyProtocolIdentifier(String extension) {
        Id id = new Id();
        id.setExtension(extension);
        return id;
    }

    private String searchLocalStudyProtocolIdentifier(Id id) {
        StudySite[] studySite = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchStudySitesByStudyProtocolId(id);
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
