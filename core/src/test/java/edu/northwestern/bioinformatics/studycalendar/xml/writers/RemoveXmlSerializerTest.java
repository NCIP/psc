package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
import static org.easymock.EasyMock.expect;

public class RemoveXmlSerializerTest extends StudyCalendarXmlTestCase {
    private RemoveXmlSerializer serializer;
    private Element element;
    private Epoch epoch;
    private Document document;
    private PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    private AbstractPlanTreeNodeXmlSerializer planTreeNodeSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        document = registerMockFor(Document.class);
        planTreeNodeSerializer = registerMockFor(AbstractPlanTreeNodeXmlSerializer.class);
        planTreeNodeSerializerFactory = registerMockFor(PlanTreeNodeXmlSerializerFactory.class);

        serializer = new RemoveXmlSerializer();
        serializer.setPlanTreeNodeXmlSerializerFactory(planTreeNodeSerializerFactory);
        epoch = setGridId("grid1", new Epoch());
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(element.attributeValue("child-id")).andReturn("grid1");
        expectGetElementByIdCalls();
        expect(planTreeNodeSerializerFactory.createXmlSerializer(element)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.readElement(element)).andReturn((PlanTreeNode)epoch);
        replayMocks();

        Remove actual = (Remove) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertSame("Child should be the same", epoch, actual.getChild());
    }

    public void testGetAllNodesAndFindById() throws Exception{
        StringBuffer buf = new StringBuffer();
        buf.append("<first id=\"1\"><second id=\"2\"><third id=\"3\"/></second><fourth id=\"4\"/></first>");

        Document document = DocumentHelper.createDocument();
        Element first = document.addElement("first")
                .addAttribute("id", "1");

        Element second = first.addElement("second")
                .addAttribute("id", "2");

        second.addElement("third")
                .addAttribute("id", "3");

        first.addElement("fourth")
                .addAttribute("id", "4");


        List<Element> nodes = serializer.getAllNodes(first);
        assertEquals("Wrong size", 4, nodes.size());

        Element node = serializer.findNodeById(nodes, "4");
        assertEquals("wrong node", "fourth", node.getName());
    }

     public void testValidateElement() throws Exception {
        Remove remove = createRemove();
        Element actual = serializer.createElement(remove);
        assertTrue(StringUtils.isBlank(serializer.validateElement(remove, actual).toString()));
        remove.setChild(null);
        assertFalse(StringUtils.isBlank(serializer.validateElement(remove, actual).toString()));

        remove = createRemove();
        assertTrue(StringUtils.isBlank(serializer.validateElement(remove, actual).toString()));
        remove.setGridId("wrong grid id");
        assertFalse(StringUtils.isBlank(serializer.validateElement(remove, actual).toString()));
        assertFalse(StringUtils.isBlank(serializer.validateElement(null, actual).toString()));
    }

    private Remove createRemove() {
        Remove remove= Remove.create(epoch);
        remove.setGridId("grid id");
        return remove;
    }

    private void expectGetElementByIdCalls() {
        expect(element.getDocument()).andReturn(document);
        expect(document.getRootElement()).andReturn(element);
        expect(element.nodeCount()).andReturn(0);
        expect(element.attributeValue("id")).andReturn("grid1");
    }
}
