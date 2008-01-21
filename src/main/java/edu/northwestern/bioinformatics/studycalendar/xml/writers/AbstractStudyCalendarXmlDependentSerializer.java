package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import org.dom4j.Element;
import edu.northwestern.bioinformatics.studycalendar.xml.BaseAbstractStudyCalendarXmlSerializer;

public abstract class AbstractStudyCalendarXmlDependentSerializer<P, C> extends BaseAbstractStudyCalendarXmlSerializer {
    public abstract Element createElement(C child);

    public abstract C readElement(P parent, Element element);
}
