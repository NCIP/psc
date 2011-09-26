package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.tools.StringTools;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import org.apache.commons.lang.StringUtils;
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

        Child child = add.getChild();
        if (child != null) {
            Changeable added = child;
            StudyCalendarXmlSerializer serializer = getPlanTreeNodeXmlSerializerFactory().createXmlSerializer(added);
            Element ePlanTreeNode = serializer.createElement(added);
            element.add(ePlanTreeNode);
        }
    }

    protected void setAdditionalProperties(final Element element, Change add) {
        String index = element.attributeValue(INDEX);
        if (index != null) {
            ((Add) add).setIndex(new Integer(index));
        }

        List<Element> ePlanTreeNodes = element.elements();
        Element ePlanTreeNode = ePlanTreeNodes.get(0);
        StudyCalendarXmlSerializer serializer = getPlanTreeNodeXmlSerializerFactory().createXmlSerializer(ePlanTreeNode);
        Child planTreeNode = (Child)serializer.readElement(ePlanTreeNode);
        ((Add) add).setChild(planTreeNode);
    }

    @Override
    public StringBuffer validateElement(Change change, Element eChange) {
        if (change == null && eChange == null) {
            return new StringBuffer("");
        } else if ((change == null && eChange != null) || (change != null && eChange == null)) {
            return new StringBuffer("either change or element is null");
        }

        StringBuffer errorMessageStringBuffer = super.validateElement(change, eChange);

        Add add = (Add) change;


        if (!StringUtils.equals(StringTools.valueOf(add.getIndex()), eChange.attributeValue(INDEX))) {
            errorMessageStringBuffer.append(String.format("index is different. expected:%s , found (in imported document) :%s \n", add.getIndex(), eChange.attributeValue(INDEX)));
            return errorMessageStringBuffer;
        }


        List<Element> ePlanTreeNodes = eChange.elements();
        Element ePlanTreeNode = ePlanTreeNodes.get(0);

        //fixme:Saurabh initialze the child also
        Child planTreeNode = add.getChild();

        StudyCalendarXmlSerializer serializer = getPlanTreeNodeXmlSerializerFactory().createXmlSerializer(ePlanTreeNode);
        if (serializer instanceof AbstractPlanTreeNodeXmlSerializer ) {
            errorMessageStringBuffer.append(((AbstractPlanTreeNodeXmlSerializer)serializer).validateElement(planTreeNode, ePlanTreeNode));
        }

        return errorMessageStringBuffer;
    }

}
