package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Rhett Sutphin
 */
public class MockStudyProvider implements StudyProvider {
    private Map<String, Study> studies;

    public List<Study> getStudies(List<Study> parameters) {
        List<Study> results = new ArrayList<Study>(parameters.size());
        for (Study parameter : parameters) {
            String nctId = parameter.getSecondaryIdentifierValue(MockDataProviderTools.KEY_STUDY_IDENTIFIER_TYPE);
            Study found = studies.get(nctId);
            results.add(found == null ? null : found.clone());
        }
        return results;
    }

    public List<Study> search(String partialName) {
        List<Study> results = new ArrayList<Study>();
        for (Study study : studies.values()) {
            if (matches(study, partialName)) {
                results.add(study.clone());
            }
        }
        return results;
    }

    private boolean matches(Study study, String partialName) {
        Pattern pattern = Pattern.compile(partialName.replace('\\', ' '), Pattern.CASE_INSENSITIVE);
        if (pattern.matcher(study.getLongTitle()).find()) {
            return true;
        }
        for (StudySecondaryIdentifier si : study.getSecondaryIdentifiers()) {
            if (pattern.matcher(si.getValue()).find()) {
                return true;
            }
        }
        return false;
    }

    public String providerToken() {
        return MockDataProviderTools.PROVIDER_TOKEN;
    }

    public void setStudies(Map<String, Study> studies) {
        this.studies = studies;
    }
}
