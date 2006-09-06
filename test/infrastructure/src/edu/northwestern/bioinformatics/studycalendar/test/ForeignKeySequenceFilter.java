package edu.northwestern.bioinformatics.studycalendar.test;

import org.dbunit.dataset.filter.SequenceTableFilter;
import org.dbunit.dataset.DataSetException;
import org.dbunit.database.IDatabaseConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.jgrapht.alg.StrongConnectivityInspector;

import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
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
                    log.warn("Table set contains a circular FK dependency: " + set + ".  Order of these tables will be arbitrary.");
                }
            }

            Iterator<String> topo
                = new TopologicalOrderIterator<String, DefaultEdge>(graph);
            List<String> ordered = new ArrayList<String>(graph.vertexSet().size());
            while (topo.hasNext()) {
                ordered.add(topo.next());
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
                    graph.addEdge(parent, child);
                }
            }
        }

        private Collection<String> getChildTableNames(String parent) throws SQLException {
            ResultSet resultSet = metadata.getExportedKeys(null, conn.getSchema(), parent);
            try {
                Set<String> foreignTableSet = new HashSet<String>();

                while (resultSet.next()) {
                    foreignTableSet.add(resultSet.getString(7));
                }

                return foreignTableSet;
            } finally {
                resultSet.close();
            }
        }
    }

}
