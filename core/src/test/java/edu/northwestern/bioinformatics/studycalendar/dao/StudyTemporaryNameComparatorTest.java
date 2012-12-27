/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class StudyTemporaryNameComparatorTest extends StudyCalendarTestCase {

    private StudyDao studyDao;

    private Study study1, study2, study3, study4, study5, study6;

    private List<Study> studyList = new ArrayList<Study>();

    private StudyService studyService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        studyService = new StudyService();
        studyService.setStudyDao(studyDao);

        study1 = new Study();
        study1.setName("[ABC 999]");

        study2 = new Study();
        study2.setName("[ABC 1001]");

        study3 = new Study();
        study3.setName("[ABC 1000]");


        study4 = new Study();
        study4.setName("[ABC non mumeric]");

        study5 = new Study();
        study5.setName("[ABC 10]");

        study6 = new Study();
        study6.setName("[ABC   1002]");

        studyList.add(study1);


    }

    public void testStudyTemporaryNameComparator() {
        studyList.add(study2);

        studyList.add(study3);

        expect(studyDao.searchStudiesByAssignedIdentifier("[ABC %]")).andReturn(studyList);
        replayMocks();
        String newStudyName = studyService.getNewStudyName();
        assertEquals("[ABC 1002]", newStudyName);
        verifyMocks();


    }

    public void testSortWhenStudyNameHasWrongFormat() {
        studyList.add(study4);
        studyList.add(study5);
        studyList.add(study6);
        studyList.add(study2);

        studyList.add(study3);

        expect(studyDao.searchStudiesByAssignedIdentifier("[ABC %]")).andReturn(studyList);
        replayMocks();
        String newStudyName = studyService.getNewStudyName();
        assertEquals("[ABC 1002]", newStudyName);
        verifyMocks();


    }
}
