/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlParsingException;
import org.dom4j.DocumentException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;

import static org.easymock.classextension.EasyMock.notNull;

/**
 * @author Rhett Sutphin
 */
public class PscStatusServiceTest extends RestletTestCase {
    private PscStatusService service;

    private Logger mockLog;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockLog = registerNiceMockFor(Logger.class);

        service = new PscStatusService();
        service.setLogger(mockLog);
    }

    public void testGetStatusLogsClientErrorsAsInfoMessages() throws Exception {
        /* expect */ mockLog.info("Payment Required (402) - PSC got to get paid, son");

        doGetStatus(
            new ResourceException(Status.CLIENT_ERROR_PAYMENT_REQUIRED, "PSC got to get paid, son"));
    }

    public void testGetStatusLogsCauseForClientErrorsAsDebugMessages() throws Exception {
        StudyCalendarValidationException expectedException
            = new StudyCalendarValidationException("nLegs should == 8");
        /* expect */ mockLog.info("Unprocessable Entity (422) - Incorrect number of legs");
        /* expect */ mockLog.debug("Previous 422 was caused by this exception", expectedException);

        doGetStatus(new ResourceException(
            Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
            "Incorrect number of legs", expectedException));
    }

    public void testGetStatusLogsServerErrorsAsErrors() throws Exception {
        /* expect */ mockLog.error("Bad Gateway (502) - Hard drive broken");

        doGetStatus(
            new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY, "Hard drive broken"));
    }

    public void testGetStatusLogsCauseForServerErrors() throws Exception {
        IllegalStateException expectedCause
            = new IllegalStateException("Arkansas");
        /* expect */ mockLog.error("Internal Server Error (500) - Uh oh", expectedCause);

        doGetStatus(
            new ResourceException(Status.SERVER_ERROR_INTERNAL, "Uh oh", expectedCause));
    }

    public void testGetStatusLogsOtherErrorsAsWarnings() throws Exception {
        /* expect */ mockLog.warn("Communication Error (1001) - A bad connector thing happened");

        doGetStatus(new ResourceException(
            Status.CONNECTOR_ERROR_COMMUNICATION, "A bad connector thing happened"));
    }

    public void testGetStatusLogsCauseForOtherErrors() throws Exception {
        IllegalStateException expectedCause
            = new IllegalStateException("Pennsylvania");
        /* expect */ mockLog.warn(
            "Communication Error (1001) - A bad connector thing happened", expectedCause);

        doGetStatus(new ResourceException(
            Status.CONNECTOR_ERROR_COMMUNICATION, "A bad connector thing happened", expectedCause));
    }

    public void testGetStatusReturnsProvidedStatusForResourceExceptions() throws Exception {
        Status actual =
            doGetStatus(new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "Never mind"));
        assertEquals(403, actual.getCode());
        assertEquals("Never mind", actual.getDescription());
    }

    public void testGetStatusLogsXmlParseExceptionsAsInfo() throws Exception {
        /* expect */ mockLog.info(
            "Bad Request (400) - Could not parse the provided XML: Missing an angle bracket somewhere.");

        doGetStatus(new StudyCalendarXmlParsingException(
            new DocumentException("Missing an angle bracket somewhere.")));
    }

    public void testGetStatusTreatsAnyUserExceptionsAsBadRequests() throws Exception {
        Status actual = doGetStatus(new StudyCalendarUserException("Boop") { });

        assertEquals("Wrong code", 400, actual.getCode());
        assertEquals("Wrong description", "Boop", actual.getDescription());
    }

    public void testGetStatusTreatsXmlParseExceptionsAsBadRequests() throws Exception {
        Status actual =
            doGetStatus(new StudyCalendarXmlParsingException(
                new DocumentException("Missing an angle bracket somewhere.")));

        assertEquals("Wrong code", 400, actual.getCode());
        assertEquals("Wrong description",
            "Could not parse the provided XML: Missing an angle bracket somewhere.",
            actual.getDescription());
    }

    public void testGetStatusTreatsValidationExceptionsAsUnprocessable() throws Exception {
        Status actual =
            doGetStatus(new StudyCalendarValidationException("Don't like it someway."));

        assertEquals("Wrong code", 422, actual.getCode());
        assertEquals("Wrong description", "Don't like it someway.", actual.getDescription());
    }

    public void testGetStatusLogsOtherExceptionsAsErrors() throws Exception {
        IllegalStateException expectedException = new IllegalStateException("Minnesota");
        /* expect */ mockLog.error("Uncaught exception in resource handling", expectedException);

        doGetStatus(expectedException);
    }

    public void testGetStatusReturns500ForOtherExceptions() throws Exception {
        /* expect */ mockLog.error((String) notNull(), (Throwable) notNull());

        Status actual = doGetStatus(new RuntimeException());
        assertEquals(500, actual.getCode());
    }

    private Status doGetStatus(Throwable statusException) {
        replayMocks();
        Status actual = service.getStatus(statusException, request, response);
        verifyMocks();
        return actual;
    }

    ////// getRepresentation

    public void testRepresentationIsText() throws Exception {
        assertEquals(MediaType.TEXT_PLAIN,
            doGetRepresentation(Status.CLIENT_ERROR_GONE).getMediaType());
    }

    public void testRepresentationIncludesStatusCode() throws Exception {
        assertContains(doGetRepresentation(Status.CLIENT_ERROR_GONE).getText(),
            "410");
    }

    public void testRepresentationIncludesDescriptionIfProvided() throws Exception {
        assertContains(
            doGetRepresentation(new Status(Status.CLIENT_ERROR_GONE, "Fishin'")).getText(),
            "Fishin'");
    }

    public void testRepresentationIncludesDefaultName() throws Exception {
        assertContains(
            doGetRepresentation(new Status(Status.CLIENT_ERROR_GONE, "Fishin'")).getText(),
            "Gone");
    }

    public void testRepresentationIncludesClientErrorReasonIfProvided() throws Exception {
        request.getAttributes().put(
            PscStatusService.CLIENT_ERROR_REASON_KEY, "Too much to do.");
        assertContains(
            doGetRepresentation(new Status(Status.CLIENT_ERROR_GONE, "Fishin'")).getText(),
            "\nToo much to do.");
    }

    private Representation doGetRepresentation(Status status) {
        return service.getRepresentation(status, request, response);
    }
}
