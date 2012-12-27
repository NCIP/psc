/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
@Deprecated /* Remove in 2.10 */
public class SubjectCoordinatorSchedulesResource extends AbstractPscResource {
    @Override
    public void doInit() {
        super.doInit();

        addAuthorizationsFor(Method.GET,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR,
            DATA_READER);
    }

    @Override
    public Representation get(Variant variant) {
        redirectPermanent(
            String.format("%s/api/v1/users/%s/managed-schedules", 
                getApplicationBaseUrl(), UriTemplateParameters.USERNAME.extractFrom(getRequest())));
        return null;
    }
}
