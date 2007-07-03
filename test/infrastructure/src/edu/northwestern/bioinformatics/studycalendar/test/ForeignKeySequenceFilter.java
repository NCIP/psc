package edu.northwestern.bioinformatics.studycalendar.test;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.SequenceTableFilter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

/**
 * This is conceptually similar to dbunit's {@link org.dbunit.database.DatabaseSequenceFilter}
 * (it works as a drop-in replacement, in fact).  The difference is that it uses a topological
 * sort of a directed graph of the relationships instead of the iterative approach dbunit takes.
 * This seems to work better.
 * <p>
 * Note that reordering will fail with an IllegalStateException if there are dependency loops
 * in the given list of tables.  This may be correctable, if necessary.
 *
 * @author Rhett Sutphin
 */
public class ForeignKeySequenceFilter extends SequenceTableFilter {
    private static final Log log = LogFactory.getLog(ForeignKeySequenceFilter.class);

    public ForeignKeySequenceFilter(IDatabaseConnection conn, String[] tableNames) throws SQLException {
        super(new Reorderer(conn, tableNames).newOrder());
    }

    public ForeignKeySequenceFilter(IDatabaseConnection conn) throws SQLException, DataSetException {
        this(conn, conn.createDataSet().getTableNames());
    }

    private static class Reorderer {
        private String[] originalTableNames;
        private IDatabaseConnection conn;
        private DatabaseMetaData metadata;
        private DirectedGraph<String, DefaultEdge> graph;

        public Reorderer(IDatabaseConnection conn, String[] tableNames) throws SQLException {
            if (log.isDebugEnabled()) log.debug("Table names received: " + Arrays.asList(tableNames));
            this.conn = conn;
            this.originalTableNames = tableNames;
            this.metadata = conn.getConnection().getMetaData();
            initGraph();
        }

        public String[] newOrder() {
            StrongConnectivityInspector<String,DefaultEdge> inspector
                = new StrongConnectivityInspector<String, DefaultEdge>(graph);

            for (Set<String> set : inspector.stronglyConnectedSets()) {
                if (set.size() > 1) {
                    throw new IllegalStateException(
                        "Table set contains a circular FK dependency: " + set + ".  Cannot reorder.");
                }
            }

            Iterator<String> topo
                = new TopologicalOrderIterator<String, DefaultEdge>(graph);
            List<String> ordered = new ArrayList<String>(graph.vertexSet().size());
            while (topo.hasNext()) ordered.add(topo.next());

            if (ordered.size() != originalTableNames.length) {
                throw new IllegalStateException("One or more tables disappeared during reordering.\nOriginal:  " + Arrays.asList(originalTableNames) + "\nReordered: " + ordered);
            }

            return ordered.toArray(new String[ordered.size()]);
        }

        private void initGraph() throws SQLException {
            graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

            for (String tableName : originalTableNames) {
                graph.addVertex(tableName);
            }

            for (String parent : originalTableNames) {
                for (String child : getChildTableNames(parent)) {
                    if (parent.equals(child)) {
                        log.debug("Ignoring self-reference in " + parent);
                    } else if (graph.containsVertex(child)) {
                        graph.addEdge(parent, child);
                    } else {
                        log.debug("Skipping " + child + " (referenced by " + parent + ") as it is not in the dataset");
                    }
                }
            }

            log.debug("Graph contains " + graph.vertexSet().size() + " vertices: " + graph.vertexSet());
            for (String vertex : graph.vertexSet()) {
                log.debug("  " + vertex + " -> " + graph.outgoingEdgesOf(vertex));
            }
        }

        private Collection<String> getChildTableNames(String parent) throws SQLException {
            ResultSet rs = metadata.getExportedKeys(null, conn.getSchema(), parent);
            try {
                Set<String> children = new HashSet<String>();
                while (rs.next()) children.add(rs.getString(7));
                return children;
            } finally {
                rs.close();
            }
        }
    }

}
