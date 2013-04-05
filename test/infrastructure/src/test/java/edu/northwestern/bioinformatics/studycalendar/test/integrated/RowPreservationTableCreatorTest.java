/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import edu.northwestern.bioinformatics.studycalendar.test.MockDbMetadata;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import static edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RowPreservationTableCreatorTest {
    private Connection connection;
    private JdbcTemplate template;
    private MockDbMetadata metadata;

    private MockRegistry mocks = new MockRegistry();

    @Before
    public void before() throws Exception {
        metadata = new MockDbMetadata();
        metadata.solo(PK_RECORD_TABLE_NAME).
            column(PK_RECORD_TABLE_NAME, "table_name", Types.VARCHAR).
            column(PK_RECORD_TABLE_NAME, "key0", Types.VARCHAR);

        connection = mocks.registerMockFor(Connection.class);
        expect(connection.getMetaData()).andStubReturn(metadata);
        template = mocks.registerMockFor(JdbcTemplate.class);
    }

    @Test
    public void createsTableWhenNotPresent() throws Exception {
        expect(connection.getMetaData()).andReturn(new MockDbMetadata());
        expect(template.update("CREATE TABLE " + PK_RECORD_TABLE_NAME +
            " (table_name VARCHAR(255))")).andReturn(null);
        expect(template.update("ALTER TABLE " + PK_RECORD_TABLE_NAME + " ADD key0 VARCHAR(255)")).andReturn(null);
        doCreate(1);
    }

    @Test
    public void doesNothingWhenTableIsSufficient() throws Exception {
        metadata.column(PK_RECORD_TABLE_NAME, "key1", Types.VARCHAR);
        doCreate(2);
    }

    @Test
    public void doesNothingWhenTableIsMoreThanSufficient() throws Exception {
        metadata.column(PK_RECORD_TABLE_NAME, "key1", Types.VARCHAR);
        doCreate(1);
    }

    @Test
    public void addsColumnsWhenNecessary() throws Exception {
        expect(template.update("ALTER TABLE " + PK_RECORD_TABLE_NAME + " ADD key1 VARCHAR(255)")).andReturn(null);
        expect(template.update("ALTER TABLE " + PK_RECORD_TABLE_NAME + " ADD key2 VARCHAR(255)")).andReturn(null);
        doCreate(3);
    }

    private void doCreate(int ct) throws SQLException {
        mocks.replayMocks();
        new RowPreservationTableCreator(ct, template).doInConnection(connection);
        mocks.verifyMocks();
    }
}
