package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Deprecated
public class PscUserServiceIntegratedTest extends DaoTestCase {
    private PscUserService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = (PscUserService) getApplicationContext().getBean("pscUserService");
    }

    public void testUserLoading() throws Exception {
        PscUser loaded = service.loadUserByUsername("Joey");
        assertNotNull(loaded);
    }
}
