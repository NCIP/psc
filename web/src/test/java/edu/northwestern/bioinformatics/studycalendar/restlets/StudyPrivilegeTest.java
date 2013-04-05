/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Jalpa Patel
 */
public class StudyPrivilegeTest extends WebTestCase {
    public void testValuesFor() throws Exception {
        UserTemplateRelationship utr = registerNiceMockFor(UserTemplateRelationship.class);
        expect(utr.getCanDevelop()).andReturn(true);
        expect(utr.getCanSeeReleasedVersions()).andReturn(true);

        replayMocks();
        List<StudyPrivilege> actual = StudyPrivilege.valuesFor(utr);
        verifyMocks();

        assertEquals("Wrong number of privileges", 2, actual.size());
        assertEquals("Wrong first priv", StudyPrivilege.DEVELOP, actual.get(0));
        assertEquals("Wrong second priv", StudyPrivilege.SEE_RELEASED, actual.get(1));
    }

    public void testAllPrivilegePropertiesExist() throws Exception {
        UserTemplateRelationship utr = new UserTemplateRelationship(null, null, null);
        BeanWrapper bw = new BeanWrapperImpl(utr);

        for (StudyPrivilege privilege : StudyPrivilege.values()) {
            assertTrue("No property named " + privilege.getPropertyName() + " for " + privilege,
                bw.isReadableProperty(privilege.getPropertyName()));
        }
    }

    public void testLookUp() throws Exception {
        StudyPrivilege actual = StudyPrivilege.lookUp("develop");
        assertEquals("Wrong Enum", StudyPrivilege.DEVELOP, actual);
    }

    public void testLookUpForIllegalEnumName() throws Exception {
        StudyPrivilege actual = StudyPrivilege.lookUp("develop1");
        assertNull("Enum created", actual);
    }
}
