package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

import java.util.List;

public abstract class AbstractDeltaXmlSerializer extends AbstractStudyCalendarXmlSerializer<Delta> {
    protected Study study;
    
    private static final String NODE_ID = "node-id";
    private DeltaDao deltaDao;
    private TemplateService templateService;

    protected abstract Delta deltaInstance();
    protected abstract PlanTreeNode<?> nodeInstance();
    protected abstract String elementName();

    public Element createElement(Delta delta) {
        Element eDelta = element(elementName());
        eDelta.addAttribute(ID, delta.getGridId());
        eDelta.addAttribute(NODE_ID, delta.getNode().getGridId());

        List<Change> changes = delta.getChanges();
        for (Change change : changes) {
            AbstractChangeXmlSerializer changeSerializer = getChangeXmlSerializerFactory().createXmlSerializer(change, delta.getNode());
            Element eChange = changeSerializer.createElement(change);
            eDelta.add(eChange);
        }

        return eDelta;
    }

    public Delta readElement(Element element) {
        String gridId = element.attributeValue(ID);
        Delta delta = deltaDao.getByGridId(gridId);
        if (delta == null) {
            delta = deltaInstance();
            delta.setGridId(gridId);

            PlanTreeNode<?> node = nodeInstance();
            node.setGridId(element.attributeValue(NODE_ID));
            delta.setNode(node);

            List<Element> eChanges = element.elements();
            for (Element eChange : eChanges) {
                AbstractChangeXmlSerializer changeSerializer = getChangeXmlSerializerFactory().createXmlSerializer(eChange,  node);
                Change change = changeSerializer.readElement(eChange);
                delta.addChange(change);
            }
        }
        return delta;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public ChangeXmlSerializerFactory getChangeXmlSerializerFactory() {
        ChangeXmlSerializerFactory factory = (ChangeXmlSerializerFactory) getBeanFactory().getBean("changeXmlSerializerFactory");
        factory.setStudy(study);
        return factory;
    }
}
