package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import org.dom4j.Element;

public abstract class AbstractChildrenChangeXmlSerializer extends AbstractChangeXmlSerializer {
    private static final String CHILD_ID = "child-id";

    protected AbstractChildrenChangeXmlSerializer(Study study) {
        super(study);
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
