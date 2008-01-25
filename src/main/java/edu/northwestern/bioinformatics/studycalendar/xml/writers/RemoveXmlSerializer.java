package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import org.dom4j.Element;

public class RemoveXmlSerializer extends AbstractChangeXmlSerializer {
    private static final String REMOVE = "remove";
    private static final String CHILD_ID = "child-id";


    public RemoveXmlSerializer(Study study) {
        super(study);
    }

    protected Change changeInstance() {
        return new Remove();
    }

    protected String elementName() {
        return REMOVE;
    }

    protected void addAdditionalAttributes(final Change change, Element element) {
        element.addAttribute(CHILD_ID, ((Remove)change).getChild().getGridId());
    }

    protected void setAdditionalProperties(final Element element, Change change) {
        String childId = element.attributeValue(CHILD_ID);
        Element child = getElementById(element, childId);
        AbstractPlanTreeNodeXmlSerializer serializer = getPlanTreeNodeSerializerFactory().createPlanTreeNodeXmlSerializer(child);
        PlanTreeNode<?> node = serializer.readElement(child);
        ((Remove)change).setChild(node);
    }
}
