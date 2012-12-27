/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;

public class RemoveXmlSerializer extends AbstractChildrenChangeXmlSerializer {
    public static final String REMOVE = "remove";

    protected Change changeInstance() {
        return new Remove();
    }

    protected String elementName() {
        return REMOVE;
    }
}
