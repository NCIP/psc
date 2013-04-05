/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.osgi;

import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.ObjectClassDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PscMockMetaTypeInformation implements MetaTypeInformation {
    private Bundle bundle;
    private Map<String, ObjectClassDefinition> ocds =
        new LinkedHashMap<String, ObjectClassDefinition>();

    public PscMockMetaTypeInformation(Bundle bundle) {
        this.bundle = bundle;
    }

    public PscMockMetaTypeInformation addObjectClassDefinition(String pid, ObjectClassDefinition def) {
        ocds.put(pid, def);
        return this;
    }

    public String[] getPids() {
        if (ocds.isEmpty()) {
            return null;
        } else {
            return ocds.keySet().toArray(new String[ocds.size()]);
        }
    }

    public String[] getFactoryPids() {
        throw new UnsupportedOperationException("getFactoryPids not implemented");
    }

    public ObjectClassDefinition getObjectClassDefinition(String pid, String locale) {
        ObjectClassDefinition definition = ocds.get(pid);
        if (definition == null) {
            throw new IllegalArgumentException("No such pid: " + pid);
        } else {
            return definition;
        }
    }

    public String[] getLocales() {
        throw new UnsupportedOperationException("getLocales not implemented");
    }

    public Bundle getBundle() {
        return bundle;
    }
}
