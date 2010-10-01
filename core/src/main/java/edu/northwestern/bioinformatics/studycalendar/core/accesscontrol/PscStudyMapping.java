package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.BasePscStudyMapping;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscStudyMapping extends BasePscStudyMapping {

    private StudyDao studyDao;

    @Override
    public List<Study> getApplicationInstances(List<String> sharedIdentifiers) {
        return studyDao.getByAssignedIdentifiers(sharedIdentifiers);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
