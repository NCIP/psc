package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.beans.PropertyEditor;

/**
 * @author Rhett Sutphin
 */
public class ControllerToolsTest extends StudyCalendarTestCase {
    private MockHttpServletRequest request;
    private ControllerTools tools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        tools = new ControllerTools();
    }

    public void testAjaxRequestWhenTrue() throws Exception {
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertTrue(tools.isAjaxRequest(request));
    }

    public void testAjaxRequestWithoutHeader() throws Exception {
        assertFalse(tools.isAjaxRequest(request));
    }
    
    public void testAjaxRequestWithOtherValue() throws Exception {
        request.addHeader("X-Requested-With", "Firefox");
        assertFalse(tools.isAjaxRequest(request));
    }

    public void testGetCurrentUser() throws Exception {
        assertNull(tools.getCurrentUser(request));
        PscUser user = AuthorizationObjectFactory.createPscUser("jimbo");
        request.setAttribute("currentUser", user);
        assertSame(user, tools.getCurrentUser(request));
    }
    
    public void testDomainObjectEditorForPlainDomainObject() throws Exception {
        assertPropertyEditorForDao(new DODao(), DaoBasedEditor.class);
    }

    public void testDomainObjectEditorForGridIdentDomainObject() throws Exception {
        assertPropertyEditorForDao(new GridDODao(), GridIdentifiableDaoBasedEditor.class);
    }

    private void assertPropertyEditorForDao(StudyCalendarDao<?> dao, final Class<? extends PropertyEditor> expectedClass) {
        final boolean[] registered = new boolean[1];
        tools.registerDomainObjectEditor(new ServletRequestDataBinder(null) {
            @Override
            @SuppressWarnings({ "RawUseOfParameterizedType" })
            public void registerCustomEditor(Class aClass, String s, PropertyEditor propertyEditor) {
                registered[0] = true;
                assertEquals("Wrong property editor type", expectedClass, propertyEditor.getClass());
            }

            @Override
            @SuppressWarnings({ "RawUseOfParameterizedType" })
            public void registerCustomEditor(Class aClass, PropertyEditor propertyEditor) {
                registered[0] = true;
                assertEquals("Wrong property editor type", expectedClass, propertyEditor.getClass());
            }
        }, "dc", dao);
        assertTrue("No property editor registered for " + dao, registered[0]);
    }

   	public void testCustomDateEditorWithExactDateLength() {
   		int maxLength = 10;
   		String validDate = "01/01/2005";
        String alsoValidDate = "1/1/2005";
        String invalidDate = "01/01/05";

   		assertTrue(validDate.length() == maxLength);
   		assertFalse(invalidDate.length() == maxLength);

           PropertyEditor dateEditor = tools.getDateEditor(false);
           //   CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true, maxLength);

   		try {
   		dateEditor.setAsText(validDate);
   		}
   		catch (IllegalArgumentException ex) {
   			fail("Exception shouldn't be thrown because this is a valid date");
   		}

   		try {
  			dateEditor.setAsText(invalidDate);
   			fail("Exception should be thrown because this is an invalid date");
   		}
  		catch (IllegalArgumentException ex) {
   			// expected to have the error message on failing year
   			assertTrue(ex.getMessage().indexOf("05") != -1);
   		}

   		try {
   		    dateEditor.setAsText(alsoValidDate);
   		}
   		catch (IllegalArgumentException ex) {
   			fail("Exception shouldn't be thrown because this is a valid date");
   		}
    }

    private static class DO implements DomainObject {
        public Integer getId() {
            throw new UnsupportedOperationException("getId not implemented");
        }

        public void setId(Integer integer) {
            throw new UnsupportedOperationException("setId not implemented");
        }
    }

    private static class GridDO extends DO implements GridIdentifiable {
        public String getGridId() {
            throw new UnsupportedOperationException("getGridId not implemented");
        }

        public void setGridId(String s) {
            throw new UnsupportedOperationException("setGridId not implemented");
        }

        public boolean hasGridId() {
            throw new UnsupportedOperationException("hasGridId not implemented");
        }
    }

    private static class DODao extends StudyCalendarDao<DO> {
        @Override public Class<DO> domainClass() { return DO.class; }
    }

    private static class GridDODao extends StudyCalendarDao<GridDO> implements GridIdentifiableDao<GridDO> {
        public GridDO getByGridId(String s) {
            throw new UnsupportedOperationException("getByGridId not implemented");
        }

        @Override public Class<GridDO> domainClass() {
            return GridDO.class;
        }
    }
}
