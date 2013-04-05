/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.NOTIFICATION;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class NotificationXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Notification> {

    protected XsdElement rootElement() {
        return NOTIFICATION;
    }

    protected XsdElement collectionRootElement() {
        return XsdElement.NOTIFICATIONS;
    }

    @Override
    public Element createElement(Notification notification, boolean inCollection) {
        Element elt = NOTIFICATION.create();
        NOTIFICATION_MESSAGE.addTo(elt, notification.getMessage());
        NOTIFICATION_TITLE.addTo(elt, notification.getTitle());
        NOTIFICATION_ACTION_REQUIRED.addTo(elt, notification.isActionRequired());
        NOTIFICATION_DISMISSED.addTo(elt, notification.isDismissed());
        NOTIFICATION_ID.addTo(elt, notification.getGridId());


        return elt;
    }

    @Override
    public Notification readElement(Element elt) {
        throw new UnsupportedOperationException("Reading notifications elements not allowed");
    }

}