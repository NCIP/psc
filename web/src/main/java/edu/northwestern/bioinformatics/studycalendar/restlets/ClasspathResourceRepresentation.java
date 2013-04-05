/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.representation.StreamRepresentation;
import org.restlet.data.MediaType;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;

/**
 * @author Rhett Sutphin
*/
public class ClasspathResourceRepresentation extends StreamRepresentation {
    private String resourceName;

    public ClasspathResourceRepresentation(MediaType mediaType, String resourceName) {
        super(mediaType);
        this.resourceName = resourceName;
    }

    @Override
    public InputStream getStream() throws IOException {
        ClassPathResource res = new ClassPathResource(resourceName);
        if (!res.exists()) {
            throw new FileNotFoundException("Could not find " + resourceName);
        }
        return res.getInputStream();
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        IOUtils.copy(getStream(), outputStream);
    }
}
