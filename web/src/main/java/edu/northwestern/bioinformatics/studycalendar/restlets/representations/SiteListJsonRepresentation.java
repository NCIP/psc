/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerationException;

/**
 * @author Nataliya Shurupova
 */
public class SiteListJsonRepresentation extends StreamingJsonRepresentation {
    private List<Site> sites;

    public SiteListJsonRepresentation(List<Site> sites) {
        this.sites = sites;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
        generator.writeStartObject();
        generator.writeFieldName("sites");
        generator.writeStartArray();

        for (Site site : getSites()) {
            writeSiteObject(generator, site);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void writeSiteObject(JsonGenerator generator, Site site) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "assigned_identifier", site.getAssignedIdentifier());
        JacksonTools.nullSafeWriteStringField(generator, "provider", site.getProvider());
        JacksonTools.nullSafeWriteStringField(generator, "site_name", site.getName());
        generator.writeEndObject();
    }

    public List<Site> getSites() {
        return sites;
    }
}

