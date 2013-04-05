/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.EpochDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.hibernate.collection.PersistentBag;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.List;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;

//import com.thoughtworks.xstream.XStream;

/**
 * Added for testing CCTS roll-back script requirement.
 * <p/>
 * Don't use StudyCalendarDbTestCase class because there  are risks of  deleting the database by mistake.
 *
 * @author Saurabh Agrawal
 */
public class PSCStudyConsumerMessgeTest extends AbstractTransactionalSpringContextTests {

    private SiteService siteService;

    public static final Log logger = LogFactory.getLog(PSCStudyConsumerMessgeTest.class);
    private String regFile;
    private PSCStudyConsumer studyConsumer;
    private String ccIdentifier = "value15";

    private StudyXmlSerializer studyXmlSerializer;

    private StudyService studyService;


    public void testCreateStudyLocal() throws Exception {
        logger.info("running test create study local method");

        gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
        Site site = siteService.getByAssignedIdentifier("DUKE");
        if (site == null) {
            site = new Site();
            site.setAssignedIdentifier("DUKE");
            site.setName("DUKE");
            siteService.createOrUpdateSite(site);
        }


        studyConsumer.createStudy(study);

        Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
        assertNotNull("must create study", pscStudy);
        assertNotNull("must create study", pscStudy.getId());
        FileOutputStream fileOutputStream = new FileOutputStream("grid/StudyConsumer.xml");
        fileOutputStream.write(studyXmlSerializer.createDocumentString(pscStudy).getBytes());

    }


    private gov.nih.nci.cabig.ccts.domain.Study populateStudyDTO() throws Exception {
        gov.nih.nci.cabig.ccts.domain.Study studyDTO = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "gov/nih/nci/ccts/grid/client/client-config.wsdd");
            Reader reader = null;
            if (regFile != null){
            	reader = new FileReader(regFile);
            }else{
            	reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "SampleStudyMessage.xml"));
            }
            studyDTO = (gov.nih.nci.cabig.ccts.domain.Study) gov.nih.nci.cagrid.common.Utils.deserializeObject(reader, gov.nih.nci.cabig.ccts.domain.Study.class, config);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
        return studyDTO;
    }


    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-studyConsumer-grid.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {

        DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));


//        regFile = System.getProperty("psc.test.sampleStudyFile",
//                "grid/study-consumer/test/resources/SampleStudyMessage.xml");
        regFile = System.getProperty("psc.test.sampleStudyFile");

    }

    protected void onTearDownAfterTransaction() throws Exception {

        DataAuditInfo.setLocal(null);

    }


    @Required
    public void setStudyConsumer(PSCStudyConsumer studyConsumer) {
        this.studyConsumer = studyConsumer;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setStudyXmlSerializer(StudyXmlSerializer studyXmlSerializer) {
        this.studyXmlSerializer = studyXmlSerializer;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }


}