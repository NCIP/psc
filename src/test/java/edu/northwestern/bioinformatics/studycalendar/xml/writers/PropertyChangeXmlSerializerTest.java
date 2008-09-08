package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

public class PropertyChangeXmlSerializerTest extends StudyCalendarXmlTestCase {
    private PropertyChangeXmlSerializer serializer;
    private PropertyChange propertyChange;
    private Element element;
    private ChangeDao changeDao;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        changeDao = registerDaoMockFor(ChangeDao.class);

        serializer = new PropertyChangeXmlSerializer();
        serializer.setChangeDao(changeDao);

        propertyChange = PropertyChange.create("name", "Epoch X", "Epoch A");
    }

    public void testCreateElement() {
        Element actual = serializer.createElement(propertyChange);

        assertEquals("Wrong property", "name", actual.attributeValue("property-name"));
        assertEquals("Wrong old value", "Epoch X", actual.attributeValue("old-value"));
        assertEquals("Wrong new value", "Epoch A", actual.attributeValue("new-value"));
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(changeDao.getByGridId("grid0")).andReturn(null);
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
