package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.grid.client.StudyServiceClient;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.TEMPLATE_VALIDATOR_INSTANCE;
import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.validation.BindException;
import static org.springframework.validation.ValidationUtils.invokeValidator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class PSCStudyServiceIntegrationTest extends AbstractTransactionalSpringContextTests {

    private PSCStudyService pscStudyService;
    private String coordinatingCenterIdentifier;


    private StudyDao studyDao;


    private Study study;

    private Amendment amendment;

    private AmendmentDao amendmentDao;

    private StudyService studyService;

    private String gridServiceUrl;
    private StudyServiceClient studyServiceClient;

    private String studyXmlStringForImport;

    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-grid-study-service.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {
        coordinatingCenterIdentifier = "cc";

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));

        gridServiceUrl = "http://localhost:8080/wsrf/services/cagrid/StudyImportExport";
        studyServiceClient = new StudyServiceClient(gridServiceUrl);

        //make sure no study  existis with given identifier
        deleteStudy();


    }

    protected void onTearDownAfterTransaction() throws Exception {
        //now delete the commited study

        DataAuditInfo.setLocal(null);

    }


    public void testStudyImportForValidStudyXmlLocalAndRemote() throws Exception {

        amendment = createAmendment();
        amendmentDao.save(amendment);

        study = createStudy("Study A");
        study.setAmendment(amendment);

        PlannedCalendar plannedCalendar = new PlannedCalendar();
        plannedCalendar.setStudy(study);
        study.setPlannedCalendar(plannedCalendar);
        studyDao.save(study);

        String studyXml = pscStudyService.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        validate(studyXml, true);

        pscStudyService.importStudy(studyXml);

        //now try remote also

        //studyServiceClient.createStudy(studyXml);

    }

    public void testStudyImportForNullOrEmptyStudyXmlLocalAndRemote() throws Exception {
        try {
            pscStudyService.importStudy("");
            fail("studyXml string is either empty or null");
        } catch (RemoteException e) {
            //expecting this exception
        }

        //now try remote
//
//        try {
//            //studyServiceClient.importStudy("");
//            fail("studyXml string is either empty or null");
//        } catch (RemoteException e) {
//            //expecting this exception
//        }

    }


    public void testStudyImportForInValidStudyXmlStringLocalAndRemote() throws Exception {

        study = createStudy("Study A");
        studyXmlStringForImport = pscStudyService.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        validate(studyXmlStringForImport, false);

        //now try importing this study

        try {
            pscStudyService.importStudy(studyXmlStringForImport);
            fail("studyXml string is not valid");
        } catch (RemoteException e) {
            //expecting this exception
        }

        //now try remote also

//
//        try {
//            studyServiceClient.importStudy(studyXmlStringForImport);
//            fail("studyXml string is not valid");
//        } catch (RemoteException e) {
//            //expecting this exception
//        }


    }


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
        assertTrue(htmlContent.indexOf("<wsdl:definitions name=\"StudyImportExport\" targetNamespace=\"http://grid.ccts.nci.nih.gov/StudyImportExport/service\"") >= 0);
        assertTrue(htmlContent.indexOf("xmlns:binding=\"http://grid.ccts.nci.nih.gov/StudyImportExport/bindings\"") >= 0);

    }

    public void testStudyExportLocalAndRemote() throws Exception {

        amendment = createAmendment();
        amendmentDao.save(amendment);

        study = createStudy("Study A");
        study.setAmendment(amendment);

        PlannedCalendar plannedCalendar = new PlannedCalendar();
        plannedCalendar.setStudy(study);
        study.setPlannedCalendar(plannedCalendar);
        studyDao.save(study);

        String studyXml = pscStudyService.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        validate(studyXml, true);

        //now try remote also
        //but for remote, we must commit the transaction

        commitAndStartNewTransaction();
//        studyXml = studyServiceClient.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
//        assertNotNull(studyXml);
//        validate(studyXml, true);
//        deleteStudy();

    }

    public void testStudyExportWhenStudyDoesNotExistsForIdentifierLocalAndRemote() throws Exception {
        try {
            pscStudyService.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
            fail("no study exists with given identifier");
        } catch (RemoteException e) {
            //expecting this exception
        }

        //now try remote

//        try {
//            studyServiceClient.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
//            fail("no study exists with given identifier");
//        } catch (RemoteException e) {
//            //expecting this exception
//        }

    }

    public void testStudyExportForEmptyStudyLocalAndRemote() throws Exception {

        study = createStudy("Study A");


        String studyXml = pscStudyService.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
        assertNotNull(studyXml);

        validate(studyXml, false);

        //now try remote also
        //but for remote, we must commit the transaction
        commitAndStartNewTransaction();
//        studyXml = studyServiceClient.exportStudyByCoordinatingCenterIdentifier(coordinatingCenterIdentifier);
//        assertNotNull(studyXml);
//
//
//        validate(studyXml, false);
//        deleteStudy();


    }


    public Amendment createAmendment() throws Exception {
        Amendment newAmendment = new Amendment(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        newAmendment.setDate(new Date());
        //setGridId(newAmendment);
        return newAmendment;
    }

    private void commitAndStartNewTransaction() {
        setComplete();
        endTransaction();
        startNewTransaction();

    }


    private void deleteStudy() {
        Study anotherStudy = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);

        if (anotherStudy != null) {
            studyService.delete(anotherStudy);
            commitAndStartNewTransaction();
            anotherStudy = studyService.getStudyByAssignedIdentifier(coordinatingCenterIdentifier);
            assertNull(anotherStudy);
        }
    }

    private void validate(String studyXml, Boolean validStdyXmlString) {
        assertNotNull(studyXml);
        byte[] byteOutput = studyXml.getBytes();
        BindException errors = new BindException(byteOutput, EMPTY);
        invokeValidator(TEMPLATE_VALIDATOR_INSTANCE, new ByteArrayInputStream(byteOutput), errors);

        if (validStdyXmlString) {
            assertFalse("Template xml should be error free", errors.hasErrors());
        } else {
            assertTrue("Template xml has errors", errors.hasErrors());

        }
    }


    private Study createStudy(String longTitle) {
        Study pscStudy = new Study();
        pscStudy.setLongTitle("long title");
        pscStudy.setAssignedIdentifier(coordinatingCenterIdentifier);
        studyDao.save(pscStudy);
        assertNotNull(pscStudy.getId());
        return pscStudy;
    }

    @Required
    public void setPscStudyService(PSCStudyService pscStudyService) {
        this.pscStudyService = pscStudyService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }
}
