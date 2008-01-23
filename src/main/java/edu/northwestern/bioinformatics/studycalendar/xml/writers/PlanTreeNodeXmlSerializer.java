package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;
import org.dom4j.Document;

import java.util.List;
import java.util.ArrayList;
import java.io.Reader;

public abstract class PlanTreeNodeXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlanTreeNode<?>> {
    Study study;

    protected PlanTreeNodeXmlSerializer(Study study) {
        this.study = study;
    }

    protected abstract Class<?> nodeClass();
    protected abstract String elementName();
    protected abstract PlanTreeNode<?> getFromId(String id);
    protected abstract PlanTreeNodeXmlSerializer getChildSerializer();

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {}
    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {}

    public Element createElement(PlanTreeNode<?> object) {
        Element element = element(elementName());
        element.addAttribute(ID, object.getGridId());
        addAdditionalElementAttributes(object, element);
        return element;
    }

    public PlanTreeNode<?> readElement(Element element) {
        if (!element.getName().equals(elementName())) {
            return null;
        }
        
        String key = element.attributeValue(ID);
        PlanTreeNode<?> node = getFromId(key);
        if (node == null) {
            Class<?> clazz = nodeClass();
            node = createInstance(clazz);
            node.setGridId(key);
            addAdditionalNodeAttributes(element, node);

            if (getChildSerializer() != null) {
                List<PlanTreeNode<?>> children = readChildren(element);
                addChildren(children, node);
            }
        }
        return node;
    }

    private List<PlanTreeNode<?>> readChildren(Element element) {
        List<PlanTreeNode<?>> nodeList = new ArrayList<PlanTreeNode<?>>();
        for (Object oChild : element.elements()) {
            Element child = (Element) oChild;
            PlanTreeNode<?> childNode = getChildSerializer().readElement(child);
            nodeList.add(childNode);
        }
        return nodeList;
    }

    private void addChildren(List<PlanTreeNode<?>> children, PlanTreeNode<?> parent) {
        for (PlanTreeNode<?> child : children) {
            ((PlanTreeInnerNode) parent).addChild(child);
        }
    }

    public Document createDocument(PlanTreeNode<?> root) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    public String createDocumentString(PlanTreeNode<?> root) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    public PlanTreeNode<?> readDocument(Document document) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    public PlanTreeNode<?> readDocument(Reader reader) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }


    //// Helper methods
    private PlanTreeNode<?> createInstance(Class<?> clazz) {
        try {
            return (PlanTreeNode<?>) clazz.newInstance();
        } catch (IllegalAccessException iae) {
            throw new StudyCalendarSystemException("Problem importing template", iae);
        } catch (InstantiationException ie) {
            throw new StudyCalendarSystemException("Problem importing template", ie);
        }
    }
}
