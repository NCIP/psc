package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.StudyMapping;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscStudyMapping implements StudyMapping<Study> {
    public static PscStudyMapping INSTANCE = new PscStudyMapping.Static();

    private StudyDao studyDao;

    public String getSharedIdentity(Study study) {
        return study.getAssignedIdentifier();
    }

    public List<Study> getApplicationInstances(List<String> sharedIdentifiers) {
        return studyDao.getByAssignedIdentifiers(sharedIdentifiers);
    }

    public boolean isInstance(Object o) {
        return o instanceof Study;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    private static class Static extends PscStudyMapping {
        @Override
        public List<Study> getApplicationInstances(List<String> identifiers) {
            throw new UnsupportedOperationException("Use a spring-configured instance if you need to use #getApplicationInstances");
        }

        @Override
        public void setStudyDao(StudyDao studyDao) {
            throw new UnsupportedOperationException("The static instance must not have a studyDao");
        }
    }
}
