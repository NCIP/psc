/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlanTreeNodeXmlSerializer extends AbstractStudyCalendarXmlSerializer<PlanTreeNode<?>> {
    private AbstractPlanTreeNodeXmlSerializer childXmlSerializer;

    protected abstract PlanTreeNode<?> nodeInstance();

    protected abstract String elementName();

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) { }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) { }

    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) { }

    @Override
    public Element createElement(PlanTreeNode<?> node) {
        Element element = element(elementName());
        element.addAttribute(ID, node.getGridId());
        addAdditionalElementAttributes(node, element);

        if (getChildXmlSerializer() != null) {
            addChildrenElements((PlanTreeInnerNode<?, PlanTreeNode<?>, ?>) node, element);
        }

        return element;
    }

    @Override
    public PlanTreeNode<?> readElement(Element element) {
        if (!element.getName().equals(elementName())) {
            return null;
        }

        String key = element.attributeValue(ID);
        PlanTreeNode<?> node = nodeInstance();
        node.setGridId(key);
        addAdditionalNodeAttributes(element, node);

        if (getChildXmlSerializer() != null) {
            List<PlanTreeNode<?>> children = readChildElements(element);
            addChildren(children, node);
        }
        return node;
    }

    protected List<PlanTreeNode<?>> readChildElements(Element element) {
        List<PlanTreeNode<?>> nodeList = new ArrayList<PlanTreeNode<?>>();
        for (Object oChild : element.elements()) {
            Element child = (Element) oChild;
            PlanTreeNode<?> childNode = getChildXmlSerializer().readElement(child);
            nodeList.add(childNode);
        }
        return nodeList;
    }

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    private void addChildren(List<PlanTreeNode<?>> children, PlanTreeNode<?> parent) {
        for (PlanTreeNode<?> child : children) {
            ((PlanTreeInnerNode) parent).addChild(child);
        }
    }

    @Override
    public Document createDocument(PlanTreeNode<?> root) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    @Override
    public String createDocumentString(PlanTreeNode<?> root) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    @Override
    public PlanTreeNode<?> readDocument(Document document) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    @Override
    public PlanTreeNode<?> readDocument(InputStream in) {
        throw new UnsupportedOperationException("PlanTreeNodes aren't root nodes");
    }

    public String validateElement(MutableDomainObject planTreeNode, Element ePlanTreeNode) {
        StringBuffer errorMessageStringBuffer = new StringBuffer("");


        if (!ePlanTreeNode.getName().equals(elementName())) {
            errorMessageStringBuffer.append(String.format("name  is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s. \n", ePlanTreeNode.getName(), elementName()));
        }

        String key = ePlanTreeNode.attributeValue(ID);
        if (!planTreeNode.getGridId().equals(key)) {
            errorMessageStringBuffer.append(String.format("id is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s. \n", planTreeNode.getGridId(), key));
        }

        return errorMessageStringBuffer.toString();
    }

    ////// CONFIGURATION

    protected AbstractPlanTreeNodeXmlSerializer getChildXmlSerializer() {
        return childXmlSerializer;
    }

    // not required -- some serializers don't have children, or handle their children a different way
    public void setChildXmlSerializer(AbstractPlanTreeNodeXmlSerializer childXmlSerializer) {
        this.childXmlSerializer = childXmlSerializer;
    }
}
