package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import org.dom4j.Element;

import java.util.List;

public class AddXmlSerializer extends AbstractChildrenChangeXmlSerializer {
    public static final String ADD = "add";
    public static final String INDEX = "index";

    protected Change changeInstance() {
        return new Add();
    }

    protected String elementName() {
        return ADD;
    }

    protected void addAdditionalAttributes(final Change change, Element element) {
        Add add = (Add) change;
        if (add.getIndex() != null) {
            element.addAttribute(INDEX, add.getIndex().toString());
        }

        PlanTreeNode<?> child = (PlanTreeNode<?>) getChild((ChildrenChange) change, childClass);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(child);
        Element ePlanTreeNode = serializer.createElement(child);
        element.add(ePlanTreeNode);
    }

    protected void setAdditionalProperties(final Element element, Change add) {
        String index = element.attributeValue(INDEX);
        if (index != null){
            ((Add)add).setIndex(new Integer(index));
        }
        
        List<Element> ePlanTreeNodes = element.elements();
        Element ePlanTreeNode = ePlanTreeNodes.get(0);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createXmlSerializer(ePlanTreeNode);
        PlanTreeNode<?> planTreeNode = serializer.readElement(ePlanTreeNode);
        ((Add)add).setChild(planTreeNode);
    }
}
