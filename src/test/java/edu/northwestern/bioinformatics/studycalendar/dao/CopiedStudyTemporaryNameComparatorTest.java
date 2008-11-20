package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saurabh Agrawal
 */
public class CopiedStudyTemporaryNameComparatorTest extends StudyCalendarTestCase {

    private StudyDao studyDao;

    private Study study1, study2, study3, study4, study5, study6;

    private List<Study> studyList = new ArrayList<Study>();

    private HibernateTemplate hibernateTemplate;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = new StudyDao();
        hibernateTemplate = registerMockFor(HibernateTemplate.class);

        studyDao.setHibernateTemplate(hibernateTemplate);

        study1 = new Study();
        study1.setName("ECOG 0123 copy");

        study2 = new Study();
        study2.setName("ECOG 0123");

        study3 = new Study();
        study3.setName("ECOG 0123 copy 2");


        study4 = new Study();
        study4.setName("ECOG 0123 copy 3");

        study5 = new Study();
        study5.setName("ECOG 0123 copy (wrong format)");

        study6 = new Study();
        study6.setName("ECOG 0123 copy (no integer value here)");


    }


    public void testStudyNameForCopyingFirstTime() {

        studyList.clear();
        expect(hibernateTemplate.find(isA(String.class), isA(String[].class))).andReturn(studyList);
        replayMocks();
        String newStudyName = studyDao.getNewStudyNameForCopyingStudy(study2.getName());
        verifyMocks();
        assertEquals("ECOG 0123 copy", newStudyName);


    }

    public void testStudyNameForCopyingSecondTimeWithoutChangingNameOfFirstCopiedStudy() {
        studyList.add(study1);
        expect(hibernateTemplate.find(isA(String.class), isA(String[].class))).andReturn(studyList);
        replayMocks();
        String newStudyName = studyDao.getNewStudyNameForCopyingStudy(study2.getName());
        verifyMocks();
        assertEquals("ECOG 0123 copy 2", newStudyName);


    }

    public void testStudyNameForCopyingCopiedStudy() {
        expect(hibernateTemplate.find(isA(String.class), isA(String[].class))).andReturn(studyList);
        replayMocks();
        String newStudyName = studyDao.getNewStudyNameForCopyingStudy(study1.getName());
        verifyMocks();
        assertEquals("ECOG 0123 copy copy", newStudyName);


    }

    public void testStudyNameForCopyingFourthTimeWithoutChangingNameOfCopiedStudy() {
        studyList.add(study4);
        studyList.add(study3);
        expect(hibernateTemplate.find(isA(String.class), isA(String[].class))).andReturn(studyList);
        replayMocks();
        String newStudyName = studyDao.getNewStudyNameForCopyingStudy(study2.getName());
        verifyMocks();
        assertEquals("ECOG 0123 copy 4", newStudyName);


    }

    public void testSortWhenStudyNameHasWrongFormat() {
        studyList.add(study4);
        studyList.add(study5);
        studyList.add(study6);
        studyList.add(study2);

        studyList.add(study3);

        expect(hibernateTemplate.find(isA(String.class), isA(String[].class))).andReturn(studyList);
        replayMocks();
        String newStudyName = studyDao.getNewStudyNameForCopyingStudy(study2.getName());
        assertEquals("ECOG 0123 copy 4", newStudyName);
        verifyMocks();


    }
}