package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public abstract class AbstractChangeXmlSerializer
    extends AbstractStudyCalendarXmlSerializer<Change>
    implements ChangeXmlSerializer
{
    private PlanTreeNodeXmlSerializerFactory planTreeNodeXmlSerializerFactory;

    protected abstract Change changeInstance();
    protected abstract String elementName();
    protected void addAdditionalAttributes(final Change change, Element element) {}
    protected void setAdditionalProperties(final Element element, Change change){}

    public Element createElement(Change change) {
        Element element = element(elementName());
        element.addAttribute(ID, change.getGridId());
        addAdditionalAttributes(change, element);
        return element;
    }

    public Change readElement(Element element) {
        String gridId = element.attributeValue(ID);
        Change change = changeInstance();
        change.setGridId(gridId);
        setAdditionalProperties(element, change);
        return change;
    }

    protected Element getElementById(Element element, String string) {
        List<Element> nodes = getAllNodes(element.getDocument().getRootElement());

        return findNodeById(nodes, string);
    }

    protected List<Element> getAllNodes(Element element) {
        if (element.nodeCount() == 0)
            return singletonList(element);

        List<Element> master = new ArrayList<Element>();
        master.add(element);

        List<Element> children = element.elements();
        for (Element child : children) {
            master.addAll(getAllNodes(child));
        }

        return master ;
    }

    protected Element findNodeById(List<Element> nodes, String id) {
        for (Element element : nodes) {
            if (id.equals(element.attributeValue(ID))) {
                return element;
            }
        }
        return null;
    }

    protected PlanTreeNodeXmlSerializerFactory getPlanTreeNodeXmlSerializerFactory() {
        return planTreeNodeXmlSerializerFactory;
    }

    @Required
    public void setPlanTreeNodeXmlSerializerFactory(PlanTreeNodeXmlSerializerFactory factory) {
        this.planTreeNodeXmlSerializerFactory = factory;
    }

    public StringBuffer validateElement(Change change, Element eChange) {

        String gridId = eChange.attributeValue(ID);
        if (!change.getGridId().equals(gridId))
            return new StringBuffer(String.format("grid id is different. expected:%s , found (in imported document) :%s \n", change.getGridId(), gridId));

        return new StringBuffer("");


    }
}
