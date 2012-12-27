/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.springframework.core.io.ClassPathResource;

import java.util.Collections;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.RawDataConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;

/**
 * @author Rhett Sutphin
 */
public class DictionaryConfigurationTest extends TestCase {
    private static final DefaultConfigurationProperties PROPERTIES =
        new DefaultConfigurationProperties(new ClassPathResource(
            "dictionary-configuration-test.properties", DictionaryConfigurationTest.class));
    private static final ConfigurationProperty<Integer> ANSWER =
        PROPERTIES.add(new DefaultConfigurationProperty.Int("answer"));
    private static final ConfigurationProperty<Integer> FINE_STRUCTURE_INV =
        PROPERTIES.add(new DefaultConfigurationProperty.Int("fineStructure"));

    public void testFromDictionaryDeserializesStrings() throws Exception {
        Configuration fromDict = create(mapBuilder().put("answer", "42"));
        assertEquals((Integer) 42, fromDict.get(ANSWER));
    }

    public void testSetSerializesStrings() throws Exception {
        DictionaryConfiguration conf = create();
        conf.set(ANSWER, 96);
        assertEquals("96", conf.getDictionary().get("answer"));
    }

    public void testDefaultsAvailable() throws Exception {
        assertEquals((Integer) 137, create().get(FINE_STRUCTURE_INV));
    }

    public void testResetMakesDefaultAvailable() throws Exception {
        Configuration conf = create(mapBuilder().put("fineStructure", "28"));
        assertEquals("Value not initially correct", (Integer) 28, conf.get(FINE_STRUCTURE_INV));
        conf.reset(FINE_STRUCTURE_INV);
        assertEquals("Default not exposed", (Integer) 137, conf.get(FINE_STRUCTURE_INV));
    }
    
    public void testFromConfigHasAllElements() throws Exception {
        Configuration src = new TransientConfiguration(PROPERTIES);
        src.set(ANSWER, 66);
        src.set(FINE_STRUCTURE_INV, 138);

        DictionaryConfiguration actual = new DictionaryConfiguration(src);
        assertSame("Wrong props", PROPERTIES, actual.getProperties());
        assertEquals("Answer not available", "66", actual.getDictionary().get("answer"));
        assertEquals("Fine structure not available", "138", actual.getDictionary().get("fineStructure"));
    }

    public void testFromConfigCopiesOnlySetProperties() throws Exception {
        Configuration src = new TransientConfiguration(PROPERTIES);
        src.set(ANSWER, 66);

        DictionaryConfiguration actual = new DictionaryConfiguration(src);
        assertFalse("Should not be set", actual.isSet(FINE_STRUCTURE_INV));
    }
    
    public void testFromConfigUsesRawDataIfAvailable() throws Exception {
        RawDataConfiguration src = EasyMock.createMock(RawDataConfiguration.class);
        expect(src.getRawData()).andReturn(Collections.singletonMap("answer", "24"));

        replay(src);
        DictionaryConfiguration actual = new DictionaryConfiguration(src, PROPERTIES);
        assertEquals("Raw data not used", (Integer) 24, actual.get(ANSWER));
        verify(src);
    }

    public void testFromConfigFromRawDataHandlesNullValues() throws Exception {
        RawDataConfiguration src = EasyMock.createMock(RawDataConfiguration.class);
        expect(src.getRawData()).andReturn(Collections.<String, String>singletonMap("answer", null));

        replay(src);
        DictionaryConfiguration actual = new DictionaryConfiguration(src, PROPERTIES);
        assertEquals(1, actual.getDictionary().size());
        verify(src);
    }

    public void testRawDataIsAvailable() throws Exception {
        DictionaryConfiguration conf = create(mapBuilder().put("answer", "42").put("e", "2.72"));
        Map<String, String> raw = conf.getRawData();
        assertEquals("Wrong amount of raw data", 2, raw.size());
        assertEquals("Missing known element", "42", raw.get("answer"));
        assertEquals("Missing unknown element", "2.72", raw.get("e"));
    }

    private DictionaryConfiguration create() {
        return new DictionaryConfiguration(PROPERTIES);
    }

    private DictionaryConfiguration create(MapBuilder<String, String> mapBuilder) {
        return new DictionaryConfiguration(PROPERTIES,  mapBuilder.toDictionary());
    }

    private MapBuilder<String, String> mapBuilder() {
        return new MapBuilder<String, String>();
    }
}
