/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.osgi;

import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class PscMockObjectClassDefinition implements ObjectClassDefinition {
    private String id, name, description;
    private List<AttributeDefinition> definitions = new ArrayList<AttributeDefinition>();

    public PscMockObjectClassDefinition(String id) {
        this(id, null, null);
    }

    public PscMockObjectClassDefinition(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.definitions = new ArrayList<AttributeDefinition>();
    }

    public String getID() {
        return id;
    }

    public PscMockObjectClassDefinition setId(String i) {
        this.id = i;
        return this;
    }

    public String getName() {
        return name;
    }

    public PscMockObjectClassDefinition setName(String n) {
        this.name = n;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PscMockObjectClassDefinition setDescription(String desc) {
        this.description = desc;
        return this;
    }

    public PscMockObjectClassDefinition addAttributeDefinition(AttributeDefinition def) {
        definitions.add(def);
        return this;
    }

    public AttributeDefinition[] getAttributeDefinitions(int i) {
        return definitions.toArray(new AttributeDefinition[definitions.size()]);
    }

    public InputStream getIcon(int i) throws IOException {
        return null;
    }
}
