/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.apache.commons.lang.StringUtils;
import org.easymock.classextension.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.validation.BindException;


import java.util.*;

/**
 * @author Jalpa Patel
 */
public class AdvancedEditActivityCommandTest  extends StudyCalendarTestCase {
    private Activity activity0;
    List<ActivityProperty> activityProperties = new ArrayList<ActivityProperty>();;
    private ActivityDao activityDao = new ActivityDao();
    private ActivityPropertyDao activityPropertyDao  = new ActivityPropertyDao();
    private AdvancedEditActivityCommand command;
    private HibernateTemplate hibernateTemplate = new HibernateTemplate();
    private final String namespace = "URI";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activityPropertyDao = registerDaoMockFor(ActivityPropertyDao.class);
        activityDao = registerDaoMockFor(ActivityDao.class);
        activity0 = setId(20, createActivity("Bone Scan"));
        activityPropertyDao.setHibernateTemplate(hibernateTemplate);
    }

    public void testBindNewUriValuesWithoutNullPointerException() throws Exception {
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("new0").setTemplateValue("http:whatever");
        command.getNewUri().get("new0").setTextValue("The name");
        assertEquals(1, command.getNewUri().size());
    }

    public void testCreateExistingUri() throws Exception {
        ActivityProperty activityProperty = createActivityProperty(namespace,"1.template","http://templateValue.com");
        activity0.addProperty(activityProperty);
        replayMocks();
        Map<String, AdvancedEditActivityCommand.UriPropertyList> actual = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao).getExistingUri();
        verifyMocks();
        assertEquals(actual.get("1").getTemplateValue(), "http://templateValue.com");

    }

    public void testUpdateActivityWithoutExistingAndNewUri() throws Exception {
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.updateActivity();
        assertEquals("Wrong Activity name","Bone Scan",command.getActivity().getName());
    }


    public void testUpdateActivityWithExistingUri() throws Exception {
        ActivityProperty activityProperty1 = createActivityProperty(namespace,"1.template","http://UpdatedTemplateValue.com");
        ActivityProperty activityProperty2 = createActivityProperty(namespace,"1.text","UpdatedTextValue");

        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.template")).andReturn(activityProperty1);
        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.text")).andReturn(activityProperty2);
        activity0.addProperty(activityProperty1);
        activity0.addProperty(activityProperty2);

        activityDao.save(activity0);
        activityPropertyDao.save(activityProperty1);
        activityPropertyDao.save(activityProperty2);

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.updateActivity();
        verifyMocks();

        assertEquals("Wrong Template value",command.getExistingUri().get("1").getTemplateValue(),"http://UpdatedTemplateValue.com");
        assertEquals("Wrong Text value",command.getExistingUri().get("1").getTextValue(),"UpdatedTextValue");
    }

    public void testUpdateActivityWithExistingUriWithOneValueOnly() throws Exception {
        ActivityProperty activityProperty = createActivityProperty(namespace,"1.template","http://UpdatedTemplateValueOnly.com");
        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.template")).andReturn(activityProperty);

        activity0.addProperty(activityProperty);
        activityDao.save(activity0);
        activityPropertyDao.save(activityProperty);

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.updateActivity();
        verifyMocks();

        assertEquals(command.getExistingUri().get("1").getTemplateValue(),"http://UpdatedTemplateValueOnly.com");
    }

    public void testUpdateActivityWithMultipleNewUri() throws Exception {
        ActivityProperty ap = createActivityProperty(activity0,namespace,"0.template","http://templateValue.com");
        activityProperties.add(ap);
        activityDao.save(activity0);
        ActivityProperty activityProperty1 = createActivityProperty(activity0,namespace,"1.template","TemplateValue");
        ActivityProperty activityProperty2 = createActivityProperty(activity0,namespace,"1.text","TextValue");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);
        activityPropertyDao.save(ActivityPropertyEq(activityProperty1));
        activityPropertyDao.save(ActivityPropertyEq(activityProperty2));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("1").setTemplateValue("templateValue");
        command.getNewUri().get("1").setTextValue("textName");
        command.updateActivity();
        verifyMocks();

    }

        public void testUpdateActivityWithNewUriWithOneValueOnly() throws Exception {

        activityDao.save(activity0);
        ActivityProperty activityProperty = createActivityProperty(activity0,namespace,"0.template","http://newTemplateValue.com");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(null);
        activityPropertyDao.save(ActivityPropertyEq(activityProperty));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("0").setTemplateValue("template");
        command.updateActivity();
        verifyMocks();
    }

    public void testGetKeyForExistingUri() throws Exception {
        ActivityProperty activityProperty = createActivityProperty(namespace,"12.template","http://newTemplateValue.com");
        activity0.addProperty(activityProperty);

        replayMocks();
        Map<String, AdvancedEditActivityCommand.UriPropertyList> actual = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao).getExistingUri();
        verifyMocks();

        assertEquals("Index doesn't work",actual.get("12").getTemplateValue(), "http://newTemplateValue.com");

    }

    public void testGetKeyWithStringValueForExistingUri() throws Exception {
        ActivityProperty activityProperty1 = createActivityProperty(namespace,"id.template","http://newTemplateValue.com");
        ActivityProperty activityProperty2 = createActivityProperty(namespace,"id.text","textValue");
        activity0.addProperty(activityProperty1);
        activity0.addProperty(activityProperty2);

        replayMocks();
        Map<String, AdvancedEditActivityCommand.UriPropertyList> actual = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao).getExistingUri();
        verifyMocks();

        assertEquals("Index doesn't work",actual.get("id").getTemplateValue(), "http://newTemplateValue.com");
        assertEquals("Index doesn't work",actual.get("id").getTextValue(), "textValue");

    }

    public void testGetKeyIndexFoNewUri() throws Exception {
        ActivityProperty ap= createActivityProperty(activity0,namespace,"0.template","http://templateValue.com");
        activityProperties.add(ap);
        ActivityProperty activityProperty1 = createActivityProperty(activity0,namespace,"1.template","TemplateValue");
        ActivityProperty activityProperty2 = createActivityProperty(activity0,namespace,"template","TemplateValue");
        ActivityProperty activityProperty3 = createActivityProperty(activity0,namespace,"2.template","TemplateValue");
        activityProperties.add(activityProperty1);
        activityProperties.add(activityProperty2);
        activityDao.save(activity0);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);
        activityPropertyDao.save(ActivityPropertyEq(activityProperty3));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("0").setTemplateValue("TemplateValue");
        command.updateActivity();
        verifyMocks();
    }

    public void testValidateWhenActivityNameIsEmptyString() throws Exception {
        activity0.setName("");
        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();
        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.activity.name.is.empty", errors.getFieldError().getCode());
    }

    public void testValidateWhenActivityNameIsNull() throws Exception {
        activity0.setName(null);
        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.activity.name.is.empty", errors.getFieldError().getCode());
    }

    public void testValidateWhenActivityCodeIsEmptyString() throws Exception {
        activity0.setCode("");
        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.activity.code.is.empty", errors.getFieldError().getCode());
    }

    public void testValidateWhenActivityCodeIsNull() throws Exception {
        activity0.setCode(null);
        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        BindException errors =  new BindException(command, StringUtils.EMPTY);
        command.validate(errors);
        verifyMocks();

        assertTrue(errors.hasErrors());
        assertEquals("Wrong error count", 1, errors.getErrorCount());
        assertEquals("Wrong error code", "error.activity.code.is.empty", errors.getFieldError().getCode());
    }

    private static ActivityProperty ActivityPropertyEq(ActivityProperty expectedActivityProperty) {
        EasyMock.reportMatcher(new ActivityPropertyMatcher(expectedActivityProperty));
        return null;
     }

    private static class ActivityPropertyMatcher implements IArgumentMatcher {
        private ActivityProperty expectedActivityProperty;

        public ActivityPropertyMatcher(ActivityProperty expectedActivityProperty) {
            this.expectedActivityProperty = expectedActivityProperty;
        }

        public boolean matches(Object object) {
            ActivityProperty actual = (ActivityProperty) object;
            return (expectedActivityProperty.getActivity().equals(actual.getActivity())) ||
                    (expectedActivityProperty.getName().equals(actual.getName()) &&
                            (expectedActivityProperty.getNamespace().equals(actual.getNamespace())) &&
                            (expectedActivityProperty.getValue().equals(actual.getValue())));

        }
        public void appendTo(StringBuffer sb) {
           sb.append("Activity=").append(expectedActivityProperty);
        }
    }

}
