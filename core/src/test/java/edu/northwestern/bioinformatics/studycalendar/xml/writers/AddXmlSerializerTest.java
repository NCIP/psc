package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Collections;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.expect;

public class AddXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AddXmlSerializer serializer;
    private Add add;
    private Element element;
    private PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    private AbstractPlanTreeNodeXmlSerializer planTreeNodeSerializer;
    private StudyCalendarXmlSerializer studyCalendarXmlSerializer;
    private Epoch epoch;
    private Element eEpoch;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        planTreeNodeSerializer = registerMockFor(AbstractPlanTreeNodeXmlSerializer.class);
        studyCalendarXmlSerializer = registerMockFor(StudyCalendarXmlSerializer.class);
        planTreeNodeSerializerFactory = registerMockFor(PlanTreeNodeXmlSerializerFactory.class);

        serializer = new AddXmlSerializer();
        serializer.setPlanTreeNodeXmlSerializerFactory(planTreeNodeSerializerFactory);

        epoch = setId(1, setGridId("grid1", new Epoch()));
        add = setGridId("grid0", Add.create(epoch, 0));

        eEpoch = DocumentHelper.createElement("epoch");
    }

    public void testCreateElement() {
        add.setChildId(1);
        expect(planTreeNodeSerializerFactory.createXmlSerializer(epoch)).andReturn(studyCalendarXmlSerializer);
        expect(studyCalendarXmlSerializer.createElement(epoch)).andReturn(eEpoch);
        replayMocks();

        Element element = serializer.createElement(add);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", element.attributeValue("id"));
        assertEquals("Wrong index", "0", element.attributeValue("index"));
        assertFalse("Wrong child id", element.elements("epoch").isEmpty());
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("index")).andReturn("0");
        expect(element.elements()).andReturn(Collections.singletonList(element));
        expect(planTreeNodeSerializerFactory.createXmlSerializer(element)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.readElement(element)).andReturn((PlanTreeNode) epoch);
        replayMocks();

        Add actual = (Add) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong grid index", 0, (int) actual.getIndex());
    }

    public void testValidateElementIfEpochIsChild() throws Exception {
        Add add = createAdd();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(epoch)).andReturn(studyCalendarXmlSerializer);


        expect(studyCalendarXmlSerializer.createElement(epoch)).andReturn(eEpoch);

        replayMocks();
        Element actual = serializer.createElement(add);
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("");
        replayMocks();
        assertTrue(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        add.setIndex(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("");
        replayMocks();
        add = createAdd();
        assertTrue(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("grid id is different");
        replayMocks();

        add.setChild(epoch);
        assertFalse(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("");
        replayMocks();
        add = createAdd();
        assertTrue(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(null, eEpoch)).andReturn("child is null");
        replayMocks();

        add.setChild(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("");
        replayMocks();

        add = createAdd();
        assertTrue(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        verifyMocks();

        resetMocks();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(eEpoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.validateElement(epoch, eEpoch)).andReturn("");
        replayMocks();
        add.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(add, actual).toString()));
        
        verifyMocks();
        assertFalse(StringUtils.isBlank(serializer.validateElement(null, actual).toString()));
    }

    private Add createAdd() {
        Add add = Fixtures.createAddChange(1, 0);
        add.setChild(epoch);
        add.setGridId("cb6e3130-9d2e-44e8-80ac-170d1875db5c");

        return add;
    }
}
