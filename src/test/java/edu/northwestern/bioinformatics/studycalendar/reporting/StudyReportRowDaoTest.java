package edu.northwestern.bioinformatics.studycalendar.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.StudyReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.StudyReportRowDao;

import java.util.List;

/**
 * @author John Dzak
 */
public class StudyReportRowDaoTest extends ContextDaoTestCase<StudyReportRowDao> {
    public void testSearch() {
        List<StudyReportRow> results = getDao().search();
        assertEquals("Wrong result size", 1, results.size());
    }
}
