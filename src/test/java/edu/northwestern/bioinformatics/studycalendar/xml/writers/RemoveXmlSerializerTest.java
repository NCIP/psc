package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import java.util.List;

public class RemoveXmlSerializerTest extends StudyCalendarXmlTestCase {
    private RemoveXmlSerializer serializer;
    private Element element;
    private ChangeDao changeDao;
    private Epoch epoch;
    private Document document;
    private AbstractChangeXmlSerializer.PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    private AbstractPlanTreeNodeXmlSerializer planTreeNodeSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        document = registerMockFor(Document.class);
        changeDao = registerDaoMockFor(ChangeDao.class);
        planTreeNodeSerializer = registerMockFor(AbstractPlanTreeNodeXmlSerializer.class);
        planTreeNodeSerializerFactory = registerMockFor(AbstractChangeXmlSerializer.PlanTreeNodeXmlSerializerFactory.class);

        serializer = new RemoveXmlSerializer(new Study()){
            protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeSerializerFactory() {
                return planTreeNodeSerializerFactory;
            }
        };
        serializer.setChangeDao(changeDao);

        epoch = setGridId("grid1", new Epoch());
    }

    public void testReadElement() {
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(changeDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("child-id")).andReturn("grid1");
        expectGetElementByIdCalls();
        expect(planTreeNodeSerializerFactory.createPlanTreeNodeXmlSerializer(element)).andReturn(planTreeNodeSerializer);
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

    private void expectGetElementByIdCalls() {
        expect(element.getDocument()).andReturn(document);
        expect(document.getRootElement()).andReturn(element);
        expect(element.nodeCount()).andReturn(0);
        expect(element.attributeValue("id")).andReturn("grid1");
    }
}
