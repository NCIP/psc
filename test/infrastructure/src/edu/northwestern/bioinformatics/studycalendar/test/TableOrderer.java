package edu.northwestern.bioinformatics.studycalendar.test;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Orders a set of table names according to their FK relationships.  The ordering
 * is such that referenced tables will be listed before the tables that reference
 * them.  Such an order is appropriate for processing INSERTs on the tables -- to
 * process DELETEs, just reverse it.
 * <p>
 * Discovery of FK relationships is accomplished via {@link DatabaseMetaData}, so
 * a live connection to the database is necessary.
 * <p>
 * Note that reordering will fail with an IllegalStateException if there are dependency loops
 * in the given list of tables.  This may be correctable, if necessary.
 *
 * @author Rhett Sutphin
*/
public class TableOrderer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String[] originalTableNames;
    private DatabaseMetaData metadata;
    private DirectedGraph<String, DefaultEdge> graph;

    public TableOrderer(DatabaseMetaData metadata, String[] tableNames) throws SQLException {
        this.metadata = metadata;
        this.originalTableNames = tableNames == null ? getAllTableNames(metadata) : tableNames;
        if (log.isDebugEnabled()) log.debug("Using table names list: " + Arrays.asList(originalTableNames));
        initGraph();
    }

    public TableOrderer(DatabaseMetaData metadata) throws SQLException {
        this(metadata, null);
    }

    public String[] insertionOrder() {
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
        ResultSet rs = metadata.getExportedKeys(null, null, parent);
        try {
            Set<String> children = new HashSet<String>();
            while (rs.next()) children.add(rs.getString(7));
            return children;
        } finally {
            rs.close();
        }
    }

    private static String[] getAllTableNames(DatabaseMetaData metadata) throws SQLException {
        ResultSet tableResult = metadata.getTables(null, null, null, new String[] { "TABLE" });
        try {
            List<String> children = new ArrayList<String>();
            while (tableResult.next()) children.add(tableResult.getString("TABLE_NAME"));
            return children.toArray(new String[children.size()]);
        } finally {
            tableResult.close();
        }
    }
}
