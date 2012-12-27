/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

public class PurgeStudyCommand {
    private String studyAssignedIdentifier;

    public String getStudyAssignedIdentifier() {
        return studyAssignedIdentifier;
    }

    public void setStudyAssignedIdentifier(String studyAssignedIdentifier) {
        this.studyAssignedIdentifier = studyAssignedIdentifier;
    }
}
