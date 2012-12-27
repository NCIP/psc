/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.presenter;

public enum TemplateAction {
    RELEASE_REVISION;

    public String getToken() {
        return toString();
    }
}
