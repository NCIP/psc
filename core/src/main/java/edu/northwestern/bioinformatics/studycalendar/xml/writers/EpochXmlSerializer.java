package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.List;

public class EpochXmlSerializer extends AbstractPlanTreeNodeXmlSerializer {
    public static final String EPOCH = "epoch";

    protected PlanTreeNode<?> nodeInstance() {
        return new Epoch();
    }

    protected String elementName() {
        return EPOCH;
    }

    protected void addAdditionalNodeAttributes(final Element element, PlanTreeNode<?> node) {
        ((Epoch) node).setName(element.attributeValue(NAME));
    }

    @Override
    protected List<PlanTreeNode<?>> readChildElements(Element element) {
        List<PlanTreeNode<?>> children = super.readChildElements(element);
        if (children.size() == 0) {
            String name = XsdAttribute.PLAN_TREE_NODE_NAME.from(element);
            StudySegment eponymous = new StudySegment();
            eponymous.setName(name);
            children.add(eponymous);
        }
        return children;
    }

    protected void addAdditionalElementAttributes(final PlanTreeNode<?> node, Element element) {
        element.addAttribute(NAME, ((Epoch) node).getName());
    }

    protected void addChildrenElements(PlanTreeInnerNode<?, PlanTreeNode<?>, ?> node, Element eStudySegment) {
        if (getChildXmlSerializer() != null) {
            for (PlanTreeNode<?> oChildNode : node.getChildren()) {
                Element childElement = getChildXmlSerializer().createElement(oChildNode);
                eStudySegment.add(childElement);
            }
        }
    }

    //@Override
    public String validateChildrenElements(StudySegment studySegment, Element eStudySegment) {
        StringBuffer errorMessageStringBuffer = new StringBuffer("");

        errorMessageStringBuffer.append(getChildXmlSerializer().validateElement(studySegment, eStudySegment));

        if (!StringUtils.isBlank(errorMessageStringBuffer.toString())) {
            errorMessageStringBuffer.append("Study segments must be identical and they must appear in the same order as they are in system. \n");
        }

        return errorMessageStringBuffer.toString();
    }

    @Override
    public String validateElement(MutableDomainObject planTreeNode, Element element) {

        StringBuffer errorMessageStringBuffer = new StringBuffer(super.validateElement(planTreeNode, element));

        Epoch epoch = (Epoch) planTreeNode;
        String name = epoch.getName();
        if (!name.equals(element.attributeValue(NAME))) {
            errorMessageStringBuffer.append(String.format("name  is different for " + planTreeNode.getClass().getSimpleName()
                    + ". expected:%s , found (in imported document) :%s \n", name, element.attributeValue(NAME)));

        }

        List childElements = element.elements();
        List<StudySegment> studySegments = epoch.getStudySegments();

        if ((childElements == null && studySegments != null)
                || (childElements != null && studySegments == null)
                || (studySegments.size() != childElements.size())) {
            errorMessageStringBuffer.append("Epoch present in the system and in the imported document must have identical number of study segments\n");


        } else {
            for (int i = 0; i < childElements.size(); i++) {
                Object childElement = childElements.get(i);

                errorMessageStringBuffer.append(validateChildrenElements(studySegments.get(i), (Element) childElement));
            }
        }

        return errorMessageStringBuffer.toString();
    }
}