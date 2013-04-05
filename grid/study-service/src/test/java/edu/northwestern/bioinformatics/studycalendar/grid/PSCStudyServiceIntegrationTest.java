/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.grid.client.StudyServiceClient;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyCreationException;
import edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyServiceIntegrationTest extends AbstractTransactionalSpringContextTests {

    private PSCStudyService studyGridService;
    private String ASSIGNED_IDENTIFIER;


    private Study study;


    private StudyService studyService;

    private String gridServiceUrl;
    private StudyServiceClient studyServiceClient;

    private String studyXmlStringForImport;
    private Epoch epoch;

    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-grid-study-service.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {
        ASSIGNED_IDENTIFIER = "cc";

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));

        gridServiceUrl = "http://localhost:8080/wsrf/services/cagrid/StudyService";
        studyServiceClient = new StudyServiceClient(gridServiceUrl);

        //make sure no study  existis with given identifier
        deleteStudy();


    }

    protected void onTearDownAfterTransaction() throws Exception {
        //now delete the commited study

        DataAuditInfo.setLocal(null);

    }


    public void testStudyImportForValidStudyXmlLocalAndRemote() throws Exception {

        study = createStudy("Study A");
        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = studyGridService.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);

        commitAndStartNewTransaction();

        validateStudy(gridStudy, study);

        //now try importing this study


        studyGridService.createStudy(gridStudy);

        //now try remote also

        studyServiceClient.createStudy(gridStudy);

    }

    public void testStudyImportForNullOrEmptyStudyXmlLocalAndRemote() throws Exception {
        try {
            studyGridService.createStudy(null);
            fail("method parameter  is null");
        } catch (StudyCreationException e) {
            //expecting this exception
        }

        //now try remote
        try {
            studyServiceClient.createStudy(null);
            fail("method parameter  is null");
        } catch (StudyCreationException e) {
            //expecting this exception
        }
    }


//    public void testStudyImportForInValidStudyXmlStringLocalAndRemote() throws Exception {
//
//        study = createStudy("Study A");
//        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = studyGridService.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);
//
//        commitAndStartNewTransaction();
//
//        validateStudy(gridStudy, study);
//
//        //now try importing this study
//// study xml:<ns1:study assigned-identifier="cc" id="58d98daa-3e7e-44fa-b687-e85e95845a6b" xmlns:ns1="http://bioinformatics.northwestern.edu/ns/psc">
//// <ns1:planned-calendar id="3062ad9b-d0e2-4cb3-bfec-e33dbf2bea4b"/>
////</ns1:study>
//
//
//        studyGridService.createStudy(gridStudy);
//
//
//
//    }


    public void testGridServiceWsdlRemote() throws Exception {
        URL url = new URL(gridServiceUrl + "?wsdl");
        URLConnection urlConnection = url.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        byte[] buffer = new byte[1024];
        int numRead;
        long numWritten = 0;
        DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));
        String s = "";
        String htmlContent = "";

        while ((s = dis.readLine()) != null) {
            htmlContent = htmlContent + s;

        }
        logger.debug("wsdl file content is:" + htmlContent);
        assertTrue(htmlContent.indexOf("<wsdl:definitions name=\"StudyService\" targetNamespace=\"http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService/service\"") >= 0);
        assertTrue(htmlContent.indexOf("xmlns:binding=\"http://grid.studycalendar.bioinformatics.northwestern.edu/StudyService/bindings\"") >= 0);

    }

    public void testStudyExportLocalAndRemote() throws Exception {

        study = createStudy("Study A");

        edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy = studyGridService.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);
        commitAndStartNewTransaction();

        validateStudy(gridStudy, study);

        //now try remote also
        //but for remote, we must commit the transaction

        gridStudy = studyServiceClient.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);

        validateStudy(gridStudy, study);

    }


    public void testStudyExportWhenStudyDoesNotExistsForIdentifierLocalAndRemote() throws Exception {
        try {
            studyGridService.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);
            fail("Exception while exporting study..no study found with given identifier:cc");

        } catch (StudyDoesNotExistsException e) {

            //expecting this exception
        }

        //  now try remote

        try {
            studyServiceClient.retrieveStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);
            fail("Exception while exporting study..no study found with given identifier:cc");
        } catch (StudyDoesNotExistsException e) {
            //expecting this exception
        }

    }


    private void validateStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study gridStudy, Study study) {

        assertNotNull(gridStudy);
        assertNotNull(study);

        //assertEquals(study.getGridId(), gridStudy.getId());

        assertEquals(study.getAssignedIdentifier(), gridStudy.getAssignedIdentifier());

        edu.northwestern.bioinformatics.studycalendar.grid.Amendment[] gridAmendments = gridStudy.getAmendment();

        List<Amendment> amendments = study.getAmendmentsList();
        if (amendments != null && !amendments.isEmpty()) {
            assertNotNull(gridAmendments);
            assertEquals(amendments.size(), gridAmendments.length);

        }

        edu.northwestern.bioinformatics.studycalendar.grid.PlannedCalendar gridPlannedCalendar = gridStudy.getPlannedCalendar();

        PlannedCalendar plannedCalendar = study.getPlannedCalendar();

        if (plannedCalendar != null) {
            assertNotNull(gridStudy.getPlannedCalendar());
            assertEquals(plannedCalendar.getGridId(), gridPlannedCalendar.getId());
        }

    }


    private void commitAndStartNewTransaction() {
        setComplete();
        endTransaction();
        startNewTransaction();

    }


    private void deleteStudy() {
        Study anotherStudy = studyService.getStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);

        if (anotherStudy != null) {
            studyService.delete(anotherStudy);
            commitAndStartNewTransaction();
            anotherStudy = studyService.getStudyByAssignedIdentifier(ASSIGNED_IDENTIFIER);
            assertNull(anotherStudy);
        }
    }

    private Study createStudy(String shortTitle) {
        Study newStudy = TemplateSkeletonCreatorImpl.createBase(shortTitle);
        newStudy.setAssignedIdentifier(ASSIGNED_IDENTIFIER);
        newStudy.setLongTitle("long title");

        // now add 2 epochs..one with no arm and one with arms to the planned calendar of study

        TemplateSkeletonCreatorImpl.addEpoch(newStudy, 0, Epoch.create("epoch with no arms"));

        Epoch epochWithArms = Epoch.create("epoch with 2 arm", new String[]{"Arm A", "Arm B"});
        TemplateSkeletonCreatorImpl.addEpoch(newStudy, 1, epochWithArms);


        studyService.save(newStudy);
        assertNotNull(newStudy.getId());
        return newStudy;
    }

    @Required
    public void setStudyGridService(PSCStudyService studyGridService) {
        this.studyGridService = studyGridService;
    }


    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
