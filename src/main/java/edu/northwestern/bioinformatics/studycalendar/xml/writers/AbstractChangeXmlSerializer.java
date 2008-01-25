package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.Element;

public abstract class AbstractChangeXmlSerializer extends AbstractStudyCalendarXmlSerializer<Change> {
    protected ChangeDao changeDao;

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
        Change change = changeDao.getByGridId(gridId);
        if (change == null) {
            change = changeInstance();
            change.setGridId(gridId);
            setAdditionalProperties(element, change);
        }
        return change;
    }

    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao;
    }
}
