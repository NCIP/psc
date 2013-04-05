/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

/**
 * @author Rhett Sutphin
 */
public interface CoppaProviderConstants {
    String PROVIDER_TOKEN = "coppa";

    /**
     * The {@link edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier#getType() type}
     * into which the COPPA <tt>StudyProtocol</tt> assigned identifier is set.
     */
    String COPPA_STUDY_IDENTIFIER_TYPE = "COPPA Identifier";
    String COPPA_STUDY_ASSIGNED_IDENTIFIER_TYPE = "COPPA Assigned Identifier";
    String COPPA_STUDY_PUBLIC_TITLE_TYPE = "COPPA Public Title";
    String COPPA_STUDY_OFFICIAL_TITLE_TYPE = "COPPA Official Title";
    String COPPA_LEAD_ORGANIZATION_IDENTIFIER_TYPE= "COPPA Lead Organization Identifier";
}
