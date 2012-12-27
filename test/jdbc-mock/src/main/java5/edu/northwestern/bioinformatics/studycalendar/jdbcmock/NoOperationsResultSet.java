/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.jdbcmock;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * A ResultSet that throws an exception when any of its methods are called.
 * A useful basis for mocks.
 *
 * @author Rhett Sutphin
 */
public class NoOperationsResultSet implements ResultSet {
    public boolean next() throws SQLException {
        throw new UnsupportedOperationException("next not implemented");
    }

    public void close() throws SQLException {
        throw new UnsupportedOperationException("close not implemented");
    }

    public boolean wasNull() throws SQLException {
        throw new UnsupportedOperationException("wasNull not implemented");
    }

    public String getString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getString not implemented");
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBoolean not implemented");
    }

    public byte getByte(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getByte not implemented");
    }

    public short getShort(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getShort not implemented");
    }

    public int getInt(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getInt not implemented");
    }

    public long getLong(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getLong not implemented");
    }

    public float getFloat(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getFloat not implemented");
    }

    public double getDouble(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getDouble not implemented");
    }

    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal not implemented");
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBytes not implemented");
    }

    public Date getDate(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getDate not implemented");
    }

    public Time getTime(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getTime not implemented");
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp not implemented");
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getAsciiStream not implemented");
    }

    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getUnicodeStream not implemented");
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBinaryStream not implemented");
    }

    public String getString(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getString not implemented");
    }

    public boolean getBoolean(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getBoolean not implemented");
    }

    public byte getByte(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getByte not implemented");
    }

    public short getShort(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getShort not implemented");
    }

    public int getInt(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getInt not implemented");
    }

    public long getLong(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getLong not implemented");
    }

    public float getFloat(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getFloat not implemented");
    }

    public double getDouble(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getDouble not implemented");
    }

    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal not implemented");
    }

    public byte[] getBytes(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getBytes not implemented");
    }

    public Date getDate(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getDate not implemented");
    }

    public Time getTime(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getTime not implemented");
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp not implemented");
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getAsciiStream not implemented");
    }

    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getUnicodeStream not implemented");
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getBinaryStream not implemented");
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("getWarnings not implemented");
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("clearWarnings not implemented");
    }

    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException("getCursorName not implemented");
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("getMetaData not implemented");
    }

    public Object getObject(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getObject not implemented");
    }

    public Object getObject(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getObject not implemented");
    }

    public int findColumn(String columnName) throws SQLException {
        throw new UnsupportedOperationException("findColumn not implemented");
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getCharacterStream not implemented");
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getCharacterStream not implemented");
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal not implemented");
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getBigDecimal not implemented");
    }

    public boolean isBeforeFirst() throws SQLException {
        throw new UnsupportedOperationException("isBeforeFirst not implemented");
    }

    public boolean isAfterLast() throws SQLException {
        throw new UnsupportedOperationException("isAfterLast not implemented");
    }

    public boolean isFirst() throws SQLException {
        throw new UnsupportedOperationException("isFirst not implemented");
    }

    public boolean isLast() throws SQLException {
        throw new UnsupportedOperationException("isLast not implemented");
    }

    public void beforeFirst() throws SQLException {
        throw new UnsupportedOperationException("beforeFirst not implemented");
    }

    public void afterLast() throws SQLException {
        throw new UnsupportedOperationException("afterLast not implemented");
    }

    public boolean first() throws SQLException {
        throw new UnsupportedOperationException("first not implemented");
    }

    public boolean last() throws SQLException {
        throw new UnsupportedOperationException("last not implemented");
    }

    public int getRow() throws SQLException {
        throw new UnsupportedOperationException("getRow not implemented");
    }

    public boolean absolute(int row) throws SQLException {
        throw new UnsupportedOperationException("absolute not implemented");
    }

    public boolean relative(int rows) throws SQLException {
        throw new UnsupportedOperationException("relative not implemented");
    }

    public boolean previous() throws SQLException {
        throw new UnsupportedOperationException("previous not implemented");
    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException("setFetchDirection not implemented");
    }

    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException("getFetchDirection not implemented");
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException("setFetchSize not implemented");
    }

    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException("getFetchSize not implemented");
    }

    public int getType() throws SQLException {
        throw new UnsupportedOperationException("getType not implemented");
    }

    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException("getConcurrency not implemented");
    }

    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException("rowUpdated not implemented");
    }

    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException("rowInserted not implemented");
    }

    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException("rowDeleted not implemented");
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("updateNull not implemented");
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException("updateBoolean not implemented");
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException("updateByte not implemented");
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnsupportedOperationException("updateShort not implemented");
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnsupportedOperationException("updateInt not implemented");
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnsupportedOperationException("updateLong not implemented");
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnsupportedOperationException("updateFloat not implemented");
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnsupportedOperationException("updateDouble not implemented");
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("updateBigDecimal not implemented");
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnsupportedOperationException("updateString not implemented");
    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        throw new UnsupportedOperationException("updateBytes not implemented");
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException("updateDate not implemented");
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException("updateTime not implemented");
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("updateTimestamp not implemented");
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("updateAsciiStream not implemented");
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("updateBinaryStream not implemented");
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new UnsupportedOperationException("updateCharacterStream not implemented");
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        throw new UnsupportedOperationException("updateObject not implemented");
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException("updateObject not implemented");
    }

    public void updateNull(String columnName) throws SQLException {
        throw new UnsupportedOperationException("updateNull not implemented");
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        throw new UnsupportedOperationException("updateBoolean not implemented");
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        throw new UnsupportedOperationException("updateByte not implemented");
    }

    public void updateShort(String columnName, short x) throws SQLException {
        throw new UnsupportedOperationException("updateShort not implemented");
    }

    public void updateInt(String columnName, int x) throws SQLException {
        throw new UnsupportedOperationException("updateInt not implemented");
    }

    public void updateLong(String columnName, long x) throws SQLException {
        throw new UnsupportedOperationException("updateLong not implemented");
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        throw new UnsupportedOperationException("updateFloat not implemented");
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        throw new UnsupportedOperationException("updateDouble not implemented");
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException("updateBigDecimal not implemented");
    }

    public void updateString(String columnName, String x) throws SQLException {
        throw new UnsupportedOperationException("updateString not implemented");
    }

    public void updateBytes(String columnName, byte x[]) throws SQLException {
        throw new UnsupportedOperationException("updateBytes not implemented");
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        throw new UnsupportedOperationException("updateDate not implemented");
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        throw new UnsupportedOperationException("updateTime not implemented");
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        throw new UnsupportedOperationException("updateTimestamp not implemented");
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("updateAsciiStream not implemented");
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        throw new UnsupportedOperationException("updateBinaryStream not implemented");
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        throw new UnsupportedOperationException("updateCharacterStream not implemented");
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        throw new UnsupportedOperationException("updateObject not implemented");
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        throw new UnsupportedOperationException("updateObject not implemented");
    }

    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException("insertRow not implemented");
    }

    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException("updateRow not implemented");
    }

    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException("deleteRow not implemented");
    }

    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException("refreshRow not implemented");
    }

    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException("cancelRowUpdates not implemented");
    }

    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException("moveToInsertRow not implemented");
    }

    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException("moveToCurrentRow not implemented");
    }

    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException("getStatement not implemented");
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("getObject not implemented");
    }

    public Ref getRef(int i) throws SQLException {
        throw new UnsupportedOperationException("getRef not implemented");
    }

    public Blob getBlob(int i) throws SQLException {
        throw new UnsupportedOperationException("getBlob not implemented");
    }

    public Clob getClob(int i) throws SQLException {
        throw new UnsupportedOperationException("getClob not implemented");
    }

    public Array getArray(int i) throws SQLException {
        throw new UnsupportedOperationException("getArray not implemented");
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("getObject not implemented");
    }

    public Ref getRef(String colName) throws SQLException {
        throw new UnsupportedOperationException("getRef not implemented");
    }

    public Blob getBlob(String colName) throws SQLException {
        throw new UnsupportedOperationException("getBlob not implemented");
    }

    public Clob getClob(String colName) throws SQLException {
        throw new UnsupportedOperationException("getClob not implemented");
    }

    public Array getArray(String colName) throws SQLException {
        throw new UnsupportedOperationException("getArray not implemented");
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getDate not implemented");
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getDate not implemented");
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getTime not implemented");
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getTime not implemented");
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp not implemented");
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException("getTimestamp not implemented");
    }

    public URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException("getURL not implemented");
    }

    public URL getURL(String columnName) throws SQLException {
        throw new UnsupportedOperationException("getURL not implemented");
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException("updateRef not implemented");
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        throw new UnsupportedOperationException("updateRef not implemented");
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException("updateBlob not implemented");
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        throw new UnsupportedOperationException("updateBlob not implemented");
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException("updateClob not implemented");
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        throw new UnsupportedOperationException("updateClob not implemented");
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException("updateArray not implemented");
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        throw new UnsupportedOperationException("updateArray not implemented");
    }
}
