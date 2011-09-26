package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.List;

public abstract class AbstractDeltaXmlSerializer
    extends AbstractStudyCalendarXmlSerializer<Delta>
    implements DeltaXmlSerializer
{
    private static final String NODE_ID = "node-id";

    protected abstract Delta deltaInstance();

    protected abstract Changeable nodeInstance();

    protected abstract String elementName();

    public Element createElement(Delta delta) {
        Element eDelta = element(elementName());
        eDelta.addAttribute(ID, delta.getGridId());
        eDelta.addAttribute(NODE_ID, delta.getNode().getGridId());

        List<Change> changes = delta.getChanges();
        for (Change change : changes) {
            ChangeXmlSerializer changeSerializer
                = getChangeXmlSerializerFactory().createXmlSerializer(change, delta.getNode());
            Element eChange = changeSerializer.createElement(change);
            if (eChange != null) {
                eDelta.add(eChange);
            }
        }

        return eDelta;
    }

    public Delta readElement(Element element) {
        String gridId = element.attributeValue(ID);
        Delta delta = deltaInstance();
        delta.setGridId(gridId);

        Changeable node = nodeInstance();
        node.setGridId(element.attributeValue(NODE_ID));
        delta.setNode(node);

        List<Element> eChanges = element.elements();
        for (Element eChange : eChanges) {
            ChangeXmlSerializer changeSerializer = getChangeXmlSerializerFactory().createXmlSerializer(eChange, node);
            Change change = changeSerializer.readElement(eChange);
            delta.addChange(change);
        }
        return delta;
    }

    public ChangeXmlSerializerFactory getChangeXmlSerializerFactory() {
        ChangeXmlSerializerFactory factory = (ChangeXmlSerializerFactory) getBeanFactory().getBean("changeXmlSerializerFactory");
        return factory;
    }

    public String validate(Amendment releasedAmendment, Element eDelta) {

        String gridId = eDelta.attributeValue(ID);
        String nodeId = eDelta.attributeValue(NODE_ID);

        StringBuffer errorMessageBuffer = new StringBuffer("");


        Delta delta = releasedAmendment.getMatchingDelta(gridId, nodeId);

        if (delta == null) {
            errorMessageBuffer.append(String.format("\n released amendment present in the system does have  " +
                    "any delta matching with provied grid id %s and node id  %s of delta.\n",
                    gridId, nodeId));

        } else {

            //now validate the changes also
            List<Element> eChanges = eDelta.elements();

            List<Change> changes = delta.getChanges();
            if ((eChanges == null && eChanges != null)
                    || (eChanges != null && eChanges == null)
                    || (changes.size() != eChanges.size())) {
                errorMessageBuffer.append(String.format("Imported document has different number of Changes for  delta (id :%s).  " +
                        "Please make sure changes are identical and they are in same order.", gridId));

            } else {
                for (int i = 0; i < eChanges.size(); i++) {
                    Element eChange = eChanges.get(i);
                    ChangeXmlSerializer changeSerializer = getChangeXmlSerializerFactory().createXmlSerializer(eChange, nodeInstance());
                    ChangeXmlSerializer abstractChangeXmlSerializer = getChangeXmlSerializerFactory().createXmlSerializer(changes.get(i), nodeInstance());

                    if (!changeSerializer.getClass().isAssignableFrom(abstractChangeXmlSerializer.getClass())) {
                        errorMessageBuffer.append(String.format("\nChange (id :%s) present in imporated document   \n are in different order " +
                                "to the change %s present in system. Please make sure changes are in same order. ", eChange.attributeValue(ID), changes.get(i).toString()));

                        break;

                    }
                    //changes must be in the same order
                    String changeError = changeSerializer.validateElement(changes.get(i), eChange).toString();
                    if (!StringUtils.isEmpty(changeError)) {
                        errorMessageBuffer.append(String.format("\nChange (id: %s) present in imporated document  is not identical to the change %s present in system. Please make sure changes are identical. " +
                                "and they are in same order. ", eChange.attributeValue(ID), changes.get(i).toString()));
                        errorMessageBuffer.append("\n The error message is : " + changeError);

                        break;

                    }


                }
            }
        }
        if (StringUtils.isEmpty(errorMessageBuffer.toString())) {
            return "";
        }
        return errorMessageBuffer.toString();
    }


}
