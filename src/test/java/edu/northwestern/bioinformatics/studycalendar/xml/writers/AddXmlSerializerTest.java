package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.easymock.EasyMock.expect;

import java.util.Collections;

public class AddXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AddXmlSerializer serializer;
    private Add add;
    private Element element;
    private ChangeDao changeDao;
    private AbstractChangeXmlSerializer.PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    private AbstractPlanTreeNodeXmlSerializer planTreeNodeSerializer;
    private Epoch epoch;
    private Element eEpoch;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        changeDao = registerMockFor(ChangeDao.class);
        planTreeNodeSerializer = registerMockFor(AbstractPlanTreeNodeXmlSerializer.class);
        planTreeNodeSerializerFactory = registerMockFor(AbstractChangeXmlSerializer.PlanTreeNodeXmlSerializerFactory.class);

        serializer = new AddXmlSerializer(new Study()) {
            protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeSerializerFactory() {
                return planTreeNodeSerializerFactory;
            }
        };
        serializer.setChangeDao(changeDao);

        epoch = setGridId("grid1", new Epoch());
        add = setGridId("grid0", Add.create(epoch, 0));

        eEpoch = DocumentHelper.createElement("epoch");
    }

    public void testCreateElement() {
        expect(planTreeNodeSerializerFactory.createPlanTreeNodeXmlSerializer(epoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.createElement(epoch)).andReturn(eEpoch);
        replayMocks();

        Element element = serializer.createElement(add);
        verifyMocks();
        
        assertEquals("Wrong grid id", "grid0", element.attributeValue("id"));
        assertEquals("Wrong index", "0", element.attributeValue("index"));
        assertFalse("Wrong child id", element.elements("epoch").isEmpty());
    }

    public void testReadElementWhenAddExists() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(changeDao.getByGridId("grid0")).andReturn(add);
        replayMocks();

        Add actual = (Add) serializer.readElement(element);
        verifyMocks();
        
        assertSame("Change objects should be the same", add, actual);
    }

    public void testReadElementWhenAddIsNew() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(changeDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("index")).andReturn("0");
        expect(element.elements()).andReturn(Collections.singletonList(element));
        expect(planTreeNodeSerializerFactory.createPlanTreeNodeXmlSerializer(element)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.readElement(element)).andReturn((PlanTreeNode) epoch);
        replayMocks();

        Add actual = (Add) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong grid index", 0, (int) actual.getIndex());
    }
}
