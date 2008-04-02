package edu.northwestern.bioinformatics.studycalendar.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.StudiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.StudiesReportRowDao;

import java.util.List;

/**
 * @author John Dzak
 */
public class StudiesReportRowDaoTest extends ContextDaoTestCase<StudiesReportRowDao> {
    public void testSearch() {
        List<StudiesReportRow> results = getDao().search();
        assertEquals("Wrong result size", 1, results.size());
    }
}
