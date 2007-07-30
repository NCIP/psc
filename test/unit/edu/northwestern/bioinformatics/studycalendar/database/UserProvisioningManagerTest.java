package edu.northwestern.bioinformatics.studycalendar.database;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.authorization.domainobjects.Application;
import gov.nih.nci.security.authorization.domainobjects.Group;


import java.util.Set;
import java.util.List;
import java.io.FileInputStream;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

public class UserProvisioningManagerTest extends DaoTestCase {
    String dataFileName = "uptdata_studycal.xml";
    //String path="/Users/johndzak/studycalendar/trunk/test/unit/edu/northwestern/bioinformatics/studycalendar/database/";

    // Can set database and ApplicationSecurityConfig through runtime params too
    // -Dconfig.database=studycalendar_test
    // -Dgov.nih.nci.security.configFile=/Users/johndzak/studycalendar/trunk/build/csm/ApplicationSecurityConfig.xml

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
       // System.setProperty("gov.nih.nci.security.configFile", "/Users/johndzak/studycalendar/trunk/build/csm/ApplicationSecurityConfig.xml");
    }

    protected String getTestDataFileName() {
        return dataFileName;
    }


   /* protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSet(new FileInputStream(getTestDataFileName()));
    }*/

    public void testGetCsmGroups() throws Exception {
    /*    UserProvisioningManager up = (UserProvisioningManager) getApplicationContext().getBean("userProvisioningManager");
        assertNotNull("User Provisioning Manager Null", up);

        Application ap = up.getApplicationById("2");
        assertNotNull("Appliction is null", ap );

        Set groups = ap.getGroups();
        assertNull("Groups are null" , groups);  // Assert Csm is broken
     */
    }

    public void testGetCsmGroups_2() throws Exception {
    /*    UserProvisioningManager up = (UserProvisioningManager) getApplicationContext().getBean("userProvisioningManager");
        Group group = new Group();

        SearchCriteria searchCriteria = new GroupSearchCriteria(group);
        List<Group> groups = up.getObjects(searchCriteria);

        assertNotNull("Groups are null" , groups);
        assertTrue(groups.size() > 0);
     */
    }
}
