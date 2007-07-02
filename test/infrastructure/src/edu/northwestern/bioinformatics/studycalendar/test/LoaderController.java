package edu.northwestern.bioinformatics.studycalendar.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.filter.SequenceTableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.DatabaseUnitException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.sql.SQLException;

/**
 * @author Rhett Sutphin
 */
public class LoaderController implements Controller {
    private static Log log = LogFactory.getLog(LoaderController.class);
    private String resourcePrefix;
    private DataSource dataSource;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        Writer w = new OutputStreamWriter(response.getOutputStream());
        Loader loader = createLoader(request, w);

        loader.load();
        w.close();

        return null;
    }

    private Loader createLoader(HttpServletRequest request, Writer writer) throws IOException {
        String commaDelim = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        List<String> names = Arrays.asList(commaDelim.split(","));
        return new Loader(names, writer);
    }

    public void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private class Loader {
        private List<String> names;
        private Writer out;

        private Loader(List<String> names, Writer out) {
            this.names = names;
            this.out = out;
        }

        public void load() throws Exception {
            IDatabaseConnection conn = null;
            try {
                log("Opening connection");
                conn = new DatabaseConnection(dataSource.getConnection());
                executeLoad(conn);
            } catch (Exception e) {
                error("Load failed due to exception", e);
            } finally {
                if (conn != null) conn.close();
            }
        }

        private void executeLoad(IDatabaseConnection conn) throws IOException, SQLException, DatabaseUnitException {

            log("Creating merged dataset from " + names);
            List<IDataSet> sets = new ArrayList<IDataSet>();
            for (String name : names) {
                InputStream resource = getResourceFor(name);
                if (resource != null) {
                    sets.add(new FlatXmlDataSet(resource));
                }
            }
            if (sets.size() == 0) {
                error("No datasets found");
            } else {
                CompositeDataSet source = new CompositeDataSet(sets.toArray(new IDataSet[sets.size()]));
                log("Merged dataset contains tables " + Arrays.asList(source.getTableNames()));
                log("Ordering tables to prevent foreign key conflicts");
                SequenceTableFilter filter = new ForeignKeySequenceFilter(conn, source.getTableNames());
                log(" - Filtered order is " + Arrays.asList(filter.getTableNames(source)));
                IDataSet dataset = new NullAsNoValueDataSet(new FilteredDataSet(filter, source));

                log("Wiping all configured tables");
                DatabaseOperation.DELETE_ALL.execute(conn, dataset);

                log("Inserting test data");
                if (log.isDebugEnabled()) {
                    for (ITable table : dataset.getTables()) {
                        log.debug("Found " + table.getRowCount() + " row(s) for " + table.getTableMetaData().getTableName());
                        for (int i = 0 ; i < table.getRowCount() ; i++) {
                            log.debug("Row " + i);
                            for (Column column : table.getTableMetaData().getColumns()) {
                                Object value = table.getValue(i, column.getColumnName());
                                log.debug(" - " + column.getColumnName() + '=' + value + " (null? " + (value == null) + ')');
                            }
                        }
                    }
                }
                DatabaseOperation.INSERT.execute(conn, dataset);

                conn.getConnection().commit();
                log("Load complete");
            }
        }

        private InputStream getResourceFor(String name) throws IOException {
            StringBuilder sb = new StringBuilder().append(resourcePrefix);
            if (sb.charAt(sb.length() - 1) != '/') sb.append('/');
            String resName = sb.append(name).append(".xml").toString();

            log(" - Loading dataset " + name);
            InputStream resource = getClass().getResourceAsStream(resName);
            if (resource == null) {
                error("   Cannot find resource " + resName + " implied by " + name);
            }
            return resource;
        }

        private void log(String message) throws IOException {
            log.info(message);
            out.write("INFO  " + message + '\n');
            out.flush();
        }

        private void error(String message) throws IOException {
            error(message, null);
        }

        private void error(String message, Exception e) throws IOException {
            if (e == null) {
                log.error(message);
            } else {
                log.error(message, e);
            }

            out.write("ERROR " + message + '\n');
            if (e != null) {
                out.write("      " + e.getMessage() + '\n');
                for (StackTraceElement element : e.getStackTrace()) {
                    out.write("        " + element + '\n');
                }
            }

            out.flush();
        }
    }

    private static class NullAsNoValueDataSet implements IDataSet {
        private Map<String, ITable> byName;

        public NullAsNoValueDataSet(IDataSet source) throws DataSetException {
            byName = new LinkedHashMap<String, ITable>();
            for (int i = 0; i < source.getTables().length; i++) {
                ITable table = source.getTables()[i];
                DefaultTable newTable = new DefaultTable(table.getTableMetaData());
                newTable.addTableRows(table);
                for (int r = 0; r < table.getRowCount(); r++) {
                    for (Column column : table.getTableMetaData().getColumns()) {
                        Object value = transformValue(table.getValue(r, column.getColumnName()));
                        newTable.setValue(r, column.getColumnName(), value);
                    }
                }
                byName.put(newTable.getTableMetaData().getTableName(), newTable);
            }
        }

        private Object transformValue(Object value) {
            return value == null ? ITable.NO_VALUE : value;
        }

        public ITable getTable(String tableName) throws DataSetException {
            return byName.get(tableName);
        }

        public ITable[] getTables() throws DataSetException {
            return byName.values().toArray(new ITable[byName.size()]);
        }

        public String[] getTableNames() throws DataSetException {
            return byName.keySet().toArray(new String[byName.size()]);
        }

        public ITableMetaData getTableMetaData(String tableName) throws DataSetException {
            return getTable(tableName).getTableMetaData();
        }

        public ITableIterator iterator() throws DataSetException {
            return new DefaultTableIterator(getTables(), false);
        }

        public ITableIterator reverseIterator() throws DataSetException {
            return new DefaultTableIterator(getTables(), true);
        }
    }
}
