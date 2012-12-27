/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

public class PropertyChangeXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PropertyChangeXmlSerializer serializer;
    private PropertyChange propertyChange;
    private Element element;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        serializer = new PropertyChangeXmlSerializer();
        propertyChange = PropertyChange.create("name", "Epoch X", "Epoch A");
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(propertyChange);

        assertEquals("Wrong property", "name", actual.attributeValue("property-name"));
        assertEquals("Wrong old value", "Epoch X", actual.attributeValue("old-value"));
        assertEquals("Wrong new value", "Epoch A", actual.attributeValue("new-value"));
    }

    public void testCreateElementWhenPropertyValueIsNull() throws Exception {
        propertyChange = PropertyChange.create("name", null, "Epoch A");
        Element actual = serializer.createElement(propertyChange);
        assertEquals("Wrong property", "name", actual.attributeValue("property-name"));
        assertEquals("Old value should be empty string instead of null", "", actual.attributeValue("old-value"));
        assertEquals("Wrong new value", "Epoch A", actual.attributeValue("new-value"));
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("property-name")).andReturn("name");
        expect(element.attributeValue("old-value")).andReturn("Epoch X");
        expect(element.attributeValue("new-value")).andReturn("Epoch A");
        replayMocks();

        PropertyChange actual = (PropertyChange) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong property", "name", actual.getPropertyName());
        assertEquals("Wrong old value", "Epoch X", actual.getOldValue());
        assertEquals("Wrong new value", "Epoch A", actual.getNewValue());
    }

    public void testReadElementWhenPropertyValueIsEmptyString() throws Exception {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("property-name")).andReturn("name");
        expect(element.attributeValue("old-value")).andReturn("");
        expect(element.attributeValue("new-value")).andReturn("Epoch A");
        replayMocks();

        PropertyChange actual = (PropertyChange) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong property", "name", actual.getPropertyName());
        assertEquals("Old value should be null instead of empty string", null, actual.getOldValue());
        assertEquals("Wrong new value", "Epoch A", actual.getNewValue());
    }

    public void testValidateElement() throws Exception {
        PropertyChange propertyChange = createPropertyChange();
        Element actual = serializer.createElement(propertyChange);
        assertTrue(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));
        propertyChange.setOldValue("wrong");
        assertFalse(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));

        propertyChange = createPropertyChange();
        assertTrue(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));
        propertyChange.setNewValue(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));

        propertyChange = createPropertyChange();
        assertTrue(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));
        propertyChange.setPropertyName(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));

        propertyChange = createPropertyChange();
        assertTrue(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));
        propertyChange.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(propertyChange, actual).toString()));
    }

    private PropertyChange createPropertyChange() {
        PropertyChange propertyChange = PropertyChange.create("name", "old name", "new name");
        propertyChange.setGridId("grid id");

        return propertyChange;
    }
}
