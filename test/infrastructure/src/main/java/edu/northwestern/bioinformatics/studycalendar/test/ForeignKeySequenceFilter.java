/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.SequenceTableFilter;

import java.sql.SQLException;

/**
 * This is conceptually similar to dbunit's {@link org.dbunit.database.DatabaseSequenceFilter}
 * (it works as a drop-in replacement, in fact).  The difference is that it uses a topological
 * sort of a directed graph of the relationships instead of the iterative approach dbunit takes.
 * This seems to work better.
 * <p>
 * Note that reordering will fail with an IllegalStateException if there are dependency loops
 * in the given list of tables.  This may be correctable, if necessary.
 *
 * @see TableOrderer
 * @author Rhett Sutphin
 */
public class ForeignKeySequenceFilter extends SequenceTableFilter {
    public ForeignKeySequenceFilter(IDatabaseConnection conn, String[] tableNames) throws SQLException {
        super(new TableOrderer(conn.getConnection().getMetaData(), tableNames).insertionOrder());
    }

    public ForeignKeySequenceFilter(IDatabaseConnection conn) throws SQLException, DataSetException {
        this(conn, conn.createDataSet().getTableNames());
    }
}
