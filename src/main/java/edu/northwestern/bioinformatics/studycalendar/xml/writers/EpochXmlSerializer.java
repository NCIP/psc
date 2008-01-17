package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

public class EpochXmlSerializer extends AbstractStudyCalendarXmlSerializer<Epoch> {

    // Elements
    public static final String EPOCH = "epoch";

    private EpochDao epochDao;

    public Element createElement(Epoch epoch) {
        // Using QName is the only way to attach the namespace to the element
        QName qEpoch = DocumentHelper.createQName(EPOCH, DEFAULT_NAMESPACE);
        Element eEpoch = DocumentHelper.createElement(qEpoch)
                .addAttribute(ID, epoch.getGridId())
                .addAttribute(NAME, epoch.getName());

        return eEpoch;
    }

    public Epoch readElement(Element element) {
        String key = element.attributeValue(ID);
        Epoch epoch = epochDao.getByGridId(key);
        if (epoch == null) {
            epoch = new Epoch();
            epoch.setGridId(key);
            epoch.setName(element.attributeValue(NAME));
        }
        return epoch;
    }

    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }
}
