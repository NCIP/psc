/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for potentially large JSON representations.
 *
 * @author Rhett Sutphin
 */
public abstract class StreamingJsonRepresentation extends OutputRepresentation {
    protected StreamingJsonRepresentation() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        JsonFactory jf = new JsonFactory();
        JsonGenerator gen = jf.createJsonGenerator(out, JsonEncoding.UTF8);
        generate(gen);
        gen.close();
    }

    public abstract void generate(JsonGenerator generator)
        throws IOException, JsonGenerationException;
}
