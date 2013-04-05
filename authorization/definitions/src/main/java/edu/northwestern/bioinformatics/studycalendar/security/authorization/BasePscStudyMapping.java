/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.StudyMapping;

import java.util.List;

/**
 * Implements the pieces of {@link StudyMapping} which can be implemented without
 * depending on the core module.
 *
 * @author Rhett Sutphin
 */
public abstract class BasePscStudyMapping implements StudyMapping<Study> {
    public String getSharedIdentity(Study study) {
        return study.getAssignedIdentifier();
    }

    public abstract List<Study> getApplicationInstances(List<String> sharedIdentifiers);

    public boolean isInstance(Object o) {
        return o instanceof Study;
    }
}
