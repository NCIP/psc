package edu.northwestern.bioinformatics.studycalendar.database;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.authorization.domainobjects.Application;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.UserProvisioningManager;


import java.util.Set;
import java.util.List;
import java.io.FileInputStream;
import java.net.URL;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserProvisioningManagerTest extends DaoTestCase {
    String dataFileName = "uptdata_studycal.xml";    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URL url = getClass().getClassLoader().getResource("ApplicationSecurityConfig.xml");
        System.setProperty("gov.nih.nci.security.configFile", url.getFile());
    }

    protected String getTestDataFileName() {
        return dataFileName;
    }

    public void testGetCsmGroups() throws Exception {
        UserProvisioningManager up = (UserProvisioningManager) getApplicationContext().getBean("userProvisioningManager");
        assertNotNull("User Provisioning Manager Null", up);

        Application ap = up.getApplicationById("2");
        assertNotNull("Appliction is null", ap );

        Set groups = ap.getGroups();
        assertNull("Groups are null" , groups);  // Assert Csm is broken
    }

    public void testGetCsmGroups_2() throws Exception {
        UserProvisioningManager mgr = (UserProvisioningManager) getApplicationContext().getBean("userProvisioningManager");
        Application appProt = new Application();
		appProt.setApplicationName("study_calendar");
		Group grpProt = new Group();
		grpProt.setApplication(appProt);
		GroupSearchCriteria sc = new GroupSearchCriteria(grpProt);
		List groups = mgr.getObjects(sc);

        assertNotNull("Groups are null" , groups);
        assertTrue(groups.size() > 0);

    }
}
