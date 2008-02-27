package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import static org.easymock.EasyMock.expect;

import java.util.Collections;

import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;

public class AddXmlSerializerTest extends StudyCalendarXmlTestCase {
    private AddXmlSerializer serializer;
    private Add add;
    private Element element;
    private ChangeDao changeDao;
    private PlanTreeNodeXmlSerializerFactory planTreeNodeSerializerFactory;
    private AbstractPlanTreeNodeXmlSerializer planTreeNodeSerializer;
    private Epoch epoch;
    private Element eEpoch;
    private DaoFinder daoFinder;
    private DomainObjectDao domainObjectDao;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        changeDao = registerMockFor(ChangeDao.class);
        domainObjectDao = registerMockFor(DomainObjectDao.class);
        daoFinder = registerMockFor(DaoFinder.class);
        planTreeNodeSerializer = registerMockFor(AbstractPlanTreeNodeXmlSerializer.class);
        planTreeNodeSerializerFactory = registerMockFor(PlanTreeNodeXmlSerializerFactory.class);

        serializer = new AddXmlSerializer() {
            protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeSerializerFactory() {
                return planTreeNodeSerializerFactory;
            }
        };
        serializer.setChangeDao(changeDao);
        serializer.setDaoFinder(daoFinder);
        serializer.setChildClass(PlannedCalendar.class);

        epoch = setId(1, setGridId("grid1", new Epoch()));
        add = setGridId("grid0", Add.create(epoch, 0));

        eEpoch = DocumentHelper.createElement("epoch");
    }

    public void testCreateElement() {
        expect(planTreeNodeSerializerFactory.createXmlSerializer(epoch)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.createElement(epoch)).andReturn(eEpoch);
        expect(daoFinder.findDao(PlannedCalendar.class)).andReturn(domainObjectDao);
        expect(domainObjectDao.getById(1)).andReturn(epoch);
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
        expect(planTreeNodeSerializerFactory.createXmlSerializer(element)).andReturn(planTreeNodeSerializer);
        expect(planTreeNodeSerializer.readElement(element)).andReturn((PlanTreeNode) epoch);
        replayMocks();

        Add actual = (Add) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong grid index", 0, (int) actual.getIndex());
    }
}
