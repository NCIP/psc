package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEvent;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import gov.nih.nci.cabig.ctms.audit.exception.AuditSystemException;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jalpa Patel
 */
public class AuditEventHelperTest  extends StudyCalendarTestCase {
    private final String USERNAME = "testUser";
    private final String IP_ADDRESS = "10.10.10.155";
    private final String URL = "/psc/pages/newStudy";
    private DataAuditInfo info;
    private AuditEventHelper auditEventHelper;
    private AuditEventDao auditEventDao;
    private List<String> auditableEntities = new ArrayList<String>();
    public void setUp() throws Exception {
        super.setUp();
        auditEventDao = registerDaoMockFor(AuditEventDao.class);
        auditableEntities.add("edu.northwestern.bioinformatics.studycalendar.domain.StudySegment");
        auditableEntities.add("edu.northwestern.bioinformatics.studycalendar.domain.Study");

        auditEventHelper = new AuditEventHelper();
        auditEventHelper.setAuditEventDao(auditEventDao);
        auditEventHelper.setAuditableEntities(auditableEntities);
        info = new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo(USERNAME, IP_ADDRESS, new Date(), URL);
        DataAuditInfo.setLocal(info);
    }

    public void testCreateAuditEvent() throws Exception {
        Study study = new Study();
        study.setId(12);
        replayMocks();
        DataAuditEvent event = auditEventHelper.createAuditEvent(study, Operation.CREATE);
        verifyMocks();
        assertNotNull("Event is not created", event);
        assertEquals("Wrong userName", USERNAME, event.getInfo().getUsername());
        assertEquals("Wrong IP address", IP_ADDRESS, event.getInfo().getIp());
        assertEquals("Wrong URL", URL, event.getInfo().getUrl());
        assertEquals("Wrong Class Name", "edu.northwestern.bioinformatics.studycalendar.domain.Study", event.getReference().getClassName());
    }

    public void testCreateAuditEventWhenNoAuditableEntities() throws Exception {
        Activity activity = new Activity();
        activity.setId(12);
        replayMocks();
        DataAuditEvent actualEvent = auditEventHelper.createAuditEvent(activity, Operation.CREATE);
        verifyMocks();
        assertNull("Event is created", actualEvent);
    }

    public void testCreateAuditEventWhenNoLocalAuditInfo() throws Exception {
        DataAuditInfo.setLocal(null);
        replayMocks();
        try {
            auditEventHelper.createAuditEvent(new Study(), Operation.CREATE);
            fail("Exception not thrown");
        } catch (AuditSystemException ase) {
            assertEquals("Can not audit; no local audit info available", ase.getMessage());
        }
        verifyMocks();
    }

    protected AuditEventDao registerDaoMockFor(Class<AuditEventDao> forClass) {
        List<Method> methods = new LinkedList<Method>(Arrays.asList(forClass.getMethods()));
        for (Iterator<Method> iterator = methods.iterator(); iterator.hasNext();) {
            Method method = iterator.next();
            if ("domainClass".equals(method.getName())) {
                iterator.remove();
            }
        }
        return registerMockFor(forClass, methods.toArray(new Method[methods.size()]));
    }
}

