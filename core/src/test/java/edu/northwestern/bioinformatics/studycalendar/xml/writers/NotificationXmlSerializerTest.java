/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.dom4j.Element;

/**
 * @author Saurabh Agrawal
 */
public class NotificationXmlSerializerTest extends StudyCalendarXmlTestCase {
    private Notification notification;
    private NotificationXmlSerializer serializer;
    private AdverseEvent adverseEvent;

    protected void setUp() throws Exception {
        super.setUp();


        serializer = new NotificationXmlSerializer();

        adverseEvent = new AdverseEvent();
        adverseEvent.setDescription("desc");
        adverseEvent.setDetectionDate(DateUtils.createDate(2008, 10, 12));
        notification = new Notification(adverseEvent);


        notification = setGridId("grid0", notification);
    }

    public void testCreateElementNewNotification() {

        Element actual = serializer.createElement(notification, true);

        assertEquals("Wrong element name", "notification", actual.getName());
        assertEquals("Wrong message", notification.getMessage(), actual.attributeValue("message"));
        assertEquals("Wrong title", notification.getTitle(), actual.attributeValue("title"));
        assertEquals("Wrong actionRequired", "true", actual.attributeValue("action-required"));
        assertEquals("Wrong dismissed", "false", actual.attributeValue("dismissed"));
        assertEquals("Wrong grid id", "grid0", actual.attributeValue("id"));

    }


}