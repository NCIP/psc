/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
public class PscMockServiceReference extends MockServiceReference {
    public PscMockServiceReference(
        Integer id, Bundle bundle, Dictionary properties, ServiceRegistration registration, String[] interfaces
    ) {
        super(bundle, properties, registration, interfaces);
        if (id != null) {
            properties.put(Constants.SERVICE_ID, id);
        }
    }
}
