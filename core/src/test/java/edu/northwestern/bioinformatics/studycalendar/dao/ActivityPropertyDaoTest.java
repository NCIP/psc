/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ActivityProperty;

import java.util.List;

/**
 * @author Jalpa Patel
 */
public class ActivityPropertyDaoTest  extends ContextDaoTestCase<ActivityPropertyDao> {
    public void testGetById() throws Exception {
        ActivityProperty activityProperty = getDao().getById(-29);
        assertNotNull(activityProperty);
        assertEquals("wrong id",-29,(int)activityProperty.getId());
        assertEquals("Wrong name", "-1.template", activityProperty.getName());
        assertEquals("Wrong value", "templateValue1",activityProperty.getValue());
        assertEquals("Wrong activity", -201, (int) activityProperty.getActivity().getId());
    }

    public void testGetByActivityId() throws Exception {
        List<ActivityProperty> properties = getDao().getByActivityId(-201);
        assertNotNull(properties);
        assertEquals("Wrong size", 2, properties.size());
        assertEquals("Wrong name", "-1.template", properties.get(0).getName());
        assertEquals("Wrong name", "-1.text", properties.get(1).getName());
        assertEquals("Wrong value", "templateValue1", properties.get(0).getValue());
        assertEquals("Wrong value", "textValue1", properties.get(1).getValue());
    }

}
