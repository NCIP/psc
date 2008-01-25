package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import org.dom4j.Element;

public class AddXmlSerializer extends AbstractChangeXmlSerializer {
    private static final String ADD = "add";
    private static final String INDEX = "index";

    public AddXmlSerializer(Study study) {
        super(study);
    }

    protected Change changeInstance() {
        return new Add();
    }

    protected String elementName() {
        return ADD;
    }

    protected void addAdditionalAttributes(final Change change, Element element) {
        element.addAttribute(INDEX, ((Add)change).getIndex().toString());
        PlanTreeNode<?> planTreeNode = ((Add)change).getChild();
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createPlanTreeNodeXmlSerializer(planTreeNode);
        Element ePlanTreeNode = serializer.createElement(planTreeNode);
        element.add(ePlanTreeNode);
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        ((Add)change).setIndex(new Integer(element.attributeValue(INDEX)));
        // TODO: Call PlanTreeNodeXmlSerializer
    }
}
