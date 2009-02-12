package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.createActivity;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.createActivityProperty;
import static edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures.createSingleActivityProperty;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.easymock.classextension.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IArgumentMatcher;
import org.springframework.orm.hibernate3.HibernateTemplate;


import java.util.*;

/**
 * @author Jalpa Patel
 */
public class AdvancedEditActivityCommandTest  extends StudyCalendarTestCase {
    private Activity activity0;
    List<ActivityProperty> activityProperties,activityPropertiesAll;
    private ActivityDao activityDao = new ActivityDao();
    private ActivityPropertyDao activityPropertyDao  = new ActivityPropertyDao();
    private AdvancedEditActivityCommand command;
    private HibernateTemplate hibernateTemplate = new HibernateTemplate();
    private final String namespace = "uri";

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
        activityProperties = createActivityProperty(activity0,namespace,"1.template","http://templateValue.com","1.text","textValue");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);

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
        activityProperties = createActivityProperty(activity0,namespace,"1.template","http://UpdatedTemplateValue.com","1.text","UpdatedTextValue");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);
        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.template")).andReturn(activityProperties.get(0));
        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.text")).andReturn(activityProperties.get(1));
        activityDao.save(activity0);
        activityPropertyDao.save(activityProperties.get(0));
        activityPropertyDao.save(activityProperties.get(1));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.updateActivity();
        verifyMocks();

        assertEquals("Wrong Template value",command.getExistingUri().get("1").getTemplateValue(),"http://UpdatedTemplateValue.com");
        assertEquals("Wrong Text value",command.getExistingUri().get("1").getTextValue(),"UpdatedTextValue");
    }

    public void testUpdateActivityWithExistingUriWithOneValueOnly() throws Exception {
        activityProperties = createActivityProperty(activity0,namespace,"1.template","http://UpdatedTemplateValueOnly.com");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);
        expect(activityPropertyDao.getByNamespaceAndName(20,namespace,"1.template")).andReturn(activityProperties.get(0));
        activityDao.save(activity0);
        activityPropertyDao.save(activityProperties.get(0));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.updateActivity();
        verifyMocks();

        assertEquals(command.getExistingUri().get("1").getTemplateValue(),"http://UpdatedTemplateValueOnly.com");
    }

    public void testUpdateActivityWithMultipleNewUri() throws Exception {
        activityProperties = createActivityProperty(activity0,namespace,"0.template","http://templateValue.com","0.text","textValue");
        activityDao.save(activity0);
        ActivityProperty activityProperty1 = createSingleActivityProperty(activity0,namespace,"1.template","TemplateValue");
        ActivityProperty activityProperty2 = createSingleActivityProperty(activity0,namespace,"1.text","TextValue");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(null);
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
        ActivityProperty activityProperty = createSingleActivityProperty(activity0,namespace,"0.template","http://newTemplateValue.com");
        expect(activityPropertyDao.getByActivityId(20)).andReturn(null);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(null);
        activityPropertyDao.save(ActivityPropertyEq(activityProperty));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("0").setTemplateValue("template");
        command.updateActivity();
        verifyMocks();
    }

    public void testGetKeyForExistingUri() throws Exception {
        activityProperties = createActivityProperty(activity0,namespace,"template","http://templateValue.com","text","textValue");
        ActivityProperty activityProperty = createSingleActivityProperty(activity0,namespace,"12.template","http://newTemplateValue.com");
        activityProperties.add(activityProperty);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);

        replayMocks();
        Map<String, AdvancedEditActivityCommand.UriPropertyList> actual = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao).getExistingUri();
        verifyMocks();

        assertEquals("Index doesn't work",actual.get("12").getTemplateValue(), "http://newTemplateValue.com");

    }

    public void testGetKeyWithStringValueForExistingUri() throws Exception {
        activityProperties = createActivityProperty(activity0,namespace,"template","http://templateValue.com","text","textValue");
        ActivityProperty activityProperty = createSingleActivityProperty(activity0,namespace,"id.template","http://newTemplateValue.com");
        ActivityProperty activityProperty1 = createSingleActivityProperty(activity0,namespace,"id.text","textValue");
        activityProperties.add(activityProperty);
        activityProperties.add(activityProperty1);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);

        replayMocks();
        Map<String, AdvancedEditActivityCommand.UriPropertyList> actual = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao).getExistingUri();
        verifyMocks();

        assertEquals("Index doesn't work",actual.get("id").getTemplateValue(), "http://newTemplateValue.com");
        assertEquals("Index doesn't work",actual.get("id").getTextValue(), "textValue");

    }

    public void testGetKeyIndexFoNewUri() throws Exception {
        activityProperties = createActivityProperty(activity0,namespace,"0.template","http://templateValue.com","0.text","textValue");
        ActivityProperty activityProperty1 = createSingleActivityProperty(activity0,namespace,"1.template","TemplateValue");
        ActivityProperty activityProperty2 = createSingleActivityProperty(activity0,namespace,"template","TemplateValue");
        ActivityProperty activityProperty3 = createSingleActivityProperty(activity0,namespace,"2.template","TemplateValue");
        activityProperties.add(activityProperty1);
        activityProperties.add(activityProperty2);
        activityDao.save(activity0);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(null);
        expect(activityPropertyDao.getByActivityId(20)).andReturn(activityProperties);
        activityPropertyDao.save(ActivityPropertyEq(activityProperty3));

        replayMocks();
        command = new AdvancedEditActivityCommand(activity0, activityDao, activityPropertyDao);
        command.getNewUri().get("0").setTemplateValue("TemplateValue");
        command.updateActivity();
        verifyMocks();
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
