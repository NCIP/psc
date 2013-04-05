/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.ContextDaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.Collection;
import java.util.List;

/**
 * @author John Dzak
 */
public abstract class ReportDaoTestCase<F extends ReportFilters, R extends DomainObject, D extends ReportDao<F, R>> extends ContextDaoTestCase {
    private D dao;
    protected F filters;

    @Override
    @SuppressWarnings({ "unchecked" })
    protected void setUp() throws Exception {
        super.setUp();
        dao = (D) getApplicationContext().getBean(getDaoBeanName());
        filters = createFilters();
    }

    protected abstract F createFilters();

    protected List<R> assertSearchWithResults(long... expectedIds) {
        List<R> rows = doSearch();
        Collection<Integer> actualIds = DomainObjectTools.collectIds(rows);
        assertEquals("Wrong number of results: " + actualIds, expectedIds.length, rows.size());
        for (int i = 0; i < expectedIds.length; i++) {
            long id = expectedIds[i];
            assertEquals("Wrong row " + i + ": " + actualIds, id, (long) rows.get(i).getId());
        }
        return rows;
    }

    protected List<R> doSearch() {
        return dao.search(filters);
    }

    @Override
    protected D getDao() {
        return dao;
    }
}
