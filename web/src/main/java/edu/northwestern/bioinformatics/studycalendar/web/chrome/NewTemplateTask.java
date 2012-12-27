/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class NewTemplateTask extends ConditionalTask {
    private Configuration configuration;

    @Override
    public boolean isEnabled() {
        return configuration.get(Configuration.ENABLE_CREATING_TEMPLATE);
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
