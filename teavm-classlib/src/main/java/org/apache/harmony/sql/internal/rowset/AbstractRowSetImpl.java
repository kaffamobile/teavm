/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.sql.internal.rowset;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.rowset.BaseRowSet;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TConnection;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TDriverManager;
import org.teavm.classlib.java.sql.TPreparedStatement;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TResultSetMetaData;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSQLWarning;
import org.teavm.classlib.java.sql.TStatement;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;

public class AbstractRowSetImpl extends BaseRowSet implements RowSet {

    protected TResultSet resultSet;

    protected TConnection connection;

    protected TPreparedStatement statement;

    private boolean isClosed = false;

    public AbstractRowSetImpl() {
        initialProperties();
        initParams();
    }

    public void execute() throws TSQLException {
        if (isClosed) {
            throw new TSQLException(Messages.getString("rowset.31"));
        }

        if (connection != null) {
            connection.close();
        }

        connection = retrieveConnection();
        String localCommand = getCommand();
        if (localCommand == null || getParams() == null) {
            // rowset.16=Not a valid command
            throw new TSQLException(Messages.getString("rowset.16")); //$NON-NLS-1$
        }

        statement = connection.prepareStatement(localCommand,
                TResultSet.TYPE_SCROLL_INSENSITIVE, TResultSet.CONCUR_UPDATABLE);
        setParameter(statement);

        resultSet = statement.executeQuery();
    }

    private TConnection retrieveConnection() throws TSQLException {
        if (getUrl() == null && getDataSourceName() == null) {
            throw new NullPointerException();
        }

        if (getUrl() != null) {
            return TDriverManager.getConnection(getUrl(), getUsername(), getPassword());
        } else if (getDataSourceName() != null) {
            try {
                Context contex = new InitialContext();
                DataSource ds = (DataSource) contex.lookup(getDataSourceName());
                return ds.getConnection();
            } catch (Exception e) {
                // rowset.25=(JNDI)Unable to get connection
                TSQLException ex = new TSQLException(Messages
                        .getString("rowset.25")); //$NON-NLS-1$
                throw ex;
            }
        }
        // rowset.24=Unable to get connection
        throw new TSQLException(Messages.getString("rowset.24")); //$NON-NLS-1$
    }

    private void setParameter(TPreparedStatement ps) throws TSQLException {
        Object[] params = getParams();
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Object[]) {
                Object[] objs = (Object[]) params[i];
                // character stream
                if (objs.length == 2) {
                    ps.setCharacterStream(i + 1, (Reader) objs[0],
                            ((Integer) objs[1]).intValue());
                } else {
                    int type = ((Integer) objs[2]).intValue();
                    switch (type) {
                    case BaseRowSet.ASCII_STREAM_PARAM:
                        ps.setAsciiStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    case BaseRowSet.BINARY_STREAM_PARAM:
                        ps.setBinaryStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    case BaseRowSet.UNICODE_STREAM_PARAM:
                        ps.setUnicodeStream(i + 1, (InputStream) objs[0],
                                ((Integer) objs[1]).intValue());
                        break;
                    }
                }
            } else {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    public boolean absolute(int row) throws TSQLException {
        checkValid();
        return resultSet.absolute(row);
    }

    public void afterLast() throws TSQLException {
        checkValid();
        resultSet.afterLast();
    }

    public void beforeFirst() throws TSQLException {
        checkValid();
        resultSet.beforeFirst();
    }

    public void cancelRowUpdates() throws TSQLException {
        checkValid();
        resultSet.cancelRowUpdates();
    }

    public void clearWarnings() throws TSQLException {
        checkValid();
        resultSet.clearWarnings();
    }

    public void close() throws TSQLException {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                connection.close();
            }

        } finally {
            isClosed = true;
        }
    }

    public void deleteRow() throws TSQLException {
        checkValid();
        resultSet.deleteRow();
    }

    public int findColumn(String columnName) throws TSQLException {
        checkValid();
        return resultSet.findColumn(columnName);
    }

    public boolean first() throws TSQLException {
        checkValid();
        return resultSet.first();
    }

    public TArray getArray(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getArray(columnIndex);
    }

    public TArray getArray(String colName) throws TSQLException {
        checkValid();
        return resultSet.getArray(colName);
    }

    public InputStream getAsciiStream(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getAsciiStream(columnIndex);
    }

    public InputStream getAsciiStream(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getAsciiStream(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws TSQLException {
        checkValid();
        return resultSet.getBigDecimal(columnIndex, scale);
    }

    public BigDecimal getBigDecimal(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getBigDecimal(columnName);
    }

    public BigDecimal getBigDecimal(String columnName, int scale)
            throws TSQLException {
        checkValid();
        return resultSet.getBigDecimal(columnName, scale);
    }

    public InputStream getBinaryStream(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getBinaryStream(columnIndex);
    }

    public InputStream getBinaryStream(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getBinaryStream(columnName);
    }

    public TBlob getBlob(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getBlob(columnIndex);
    }

    public TBlob getBlob(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getBlob(columnName);
    }

    public boolean getBoolean(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getBoolean(columnIndex);
    }

    public boolean getBoolean(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getBoolean(columnName);
    }

    public byte getByte(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getByte(columnIndex);
    }

    public byte getByte(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getByte(columnName);
    }

    public byte[] getBytes(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getBytes(columnIndex);
    }

    public byte[] getBytes(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getBytes(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getCharacterStream(columnName);
    }

    public TClob getClob(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getClob(columnIndex);
    }

    public TClob getClob(String colName) throws TSQLException {
        checkValid();
        return resultSet.getClob(colName);
    }

    public String getCursorName() throws TSQLException {
        checkValid();
        return resultSet.getCursorName();
    }

    public TDate getDate(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getDate(columnIndex);
    }

    public TDate getDate(int columnIndex, Calendar cal) throws TSQLException {
        checkValid();
        return resultSet.getDate(columnIndex, cal);
    }

    public TDate getDate(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getDate(columnName);
    }

    public TDate getDate(String columnName, Calendar cal) throws TSQLException {
        checkValid();
        return resultSet.getDate(columnName, cal);
    }

    public double getDouble(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getDouble(columnIndex);
    }

    public double getDouble(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getDouble(columnName);
    }

    public float getFloat(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getFloat(columnIndex);
    }

    public float getFloat(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getFloat(columnName);
    }

    public int getInt(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getInt(columnIndex);
    }

    public int getInt(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getInt(columnName);
    }

    public long getLong(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getLong(columnIndex);
    }

    public long getLong(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getLong(columnName);
    }

    public TResultSetMetaData getMetaData() throws TSQLException {
        checkValid();
        return resultSet.getMetaData();
    }

    public Object getObject(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getObject(columnIndex);
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws TSQLException {
        checkValid();
        return resultSet.getObject(columnIndex, map);
    }

    public Object getObject(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getObject(columnName);
    }

    public Object getObject(String columnName, Map<String, Class<?>> map)
            throws TSQLException {
        checkValid();
        return resultSet.getObject(columnName, map);
    }

    public TRef getRef(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getRef(columnIndex);
    }

    public TRef getRef(String colName) throws TSQLException {
        checkValid();
        return resultSet.getRef(colName);
    }

    public int getRow() throws TSQLException {
        checkValid();
        return resultSet.getRow();
    }

    public short getShort(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getShort(columnIndex);
    }

    public short getShort(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getShort(columnName);
    }

    public TStatement getStatement() throws TSQLException {
        if (statement != null && isClosed) {
            throw new TSQLException();
        }
        return statement;
    }

    public String getString(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getString(columnIndex);
    }

    public String getString(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getString(columnName);
    }

    public TTime getTime(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getTime(columnIndex);
    }

    public TTime getTime(int columnIndex, Calendar cal) throws TSQLException {
        checkValid();
        return resultSet.getTime(columnIndex, cal);
    }

    public TTime getTime(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getTime(columnName);
    }

    public TTime getTime(String columnName, Calendar cal) throws TSQLException {
        checkValid();
        return resultSet.getTime(columnName, cal);
    }

    public TTimestamp getTimestamp(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getTimestamp(columnIndex);
    }

    public TTimestamp getTimestamp(int columnIndex, Calendar cal)
            throws TSQLException {
        checkValid();
        return resultSet.getTimestamp(columnIndex, cal);
    }

    public TTimestamp getTimestamp(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getTimestamp(columnName);
    }

    public TTimestamp getTimestamp(String columnName, Calendar cal)
            throws TSQLException {
        checkValid();
        return resultSet.getTimestamp(columnName, cal);
    }

    public org.teavm.classlib.java.net.TURL getURL(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getURL(columnIndex);
    }

    public org.teavm.classlib.java.net.TURL getURL(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getURL(columnName);
    }

    public InputStream getUnicodeStream(int columnIndex) throws TSQLException {
        checkValid();
        return resultSet.getUnicodeStream(columnIndex);
    }

    public InputStream getUnicodeStream(String columnName) throws TSQLException {
        checkValid();
        return resultSet.getUnicodeStream(columnName);
    }

    public TSQLWarning getWarnings() throws TSQLException {
        checkValid();
        return resultSet.getWarnings();
    }

    public void insertRow() throws TSQLException {
        checkValid();
        resultSet.insertRow();
    }

    public boolean isAfterLast() throws TSQLException {
        checkValid();
        return resultSet.isAfterLast();
    }

    public boolean isBeforeFirst() throws TSQLException {
        checkValid();
        return resultSet.isBeforeFirst();
    }

    public boolean isFirst() throws TSQLException {
        checkValid();
        return resultSet.isFirst();
    }

    public boolean isLast() throws TSQLException {
        checkValid();
        return resultSet.isLast();
    }

    public boolean last() throws TSQLException {
        checkValid();
        return resultSet.last();
    }

    public void moveToCurrentRow() throws TSQLException {
        checkValid();
        resultSet.moveToCurrentRow();
    }

    public void moveToInsertRow() throws TSQLException {
        checkValid();
        resultSet.moveToInsertRow();
    }

    public boolean next() throws TSQLException {
        checkValid();
        return resultSet.next();
    }

    public boolean previous() throws TSQLException {
        checkValid();
        return resultSet.previous();
    }

    public void refreshRow() throws TSQLException {
        checkValid();
        resultSet.refreshRow();
    }

    public boolean relative(int rows) throws TSQLException {
        checkValid();
        return resultSet.relative(rows);
    }

    public boolean rowDeleted() throws TSQLException {
        checkValid();
        return resultSet.rowDeleted();
    }

    public boolean rowInserted() throws TSQLException {
        checkValid();
        return resultSet.rowInserted();
    }

    public boolean rowUpdated() throws TSQLException {
        checkValid();
        return resultSet.rowUpdated();
    }

    public void updateArray(int columnIndex, TArray x) throws TSQLException {
        checkValid();
        resultSet.updateArray(columnIndex, x);
    }

    public void updateArray(String columnName, TArray x) throws TSQLException {
        checkValid();
        resultSet.updateArray(columnName, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws TSQLException {
        checkValid();
        resultSet.updateAsciiStream(columnIndex, x, length);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws TSQLException {
        checkValid();
        resultSet.updateAsciiStream(columnName, x, length);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws TSQLException {
        checkValid();
        resultSet.updateBigDecimal(columnIndex, x);
    }

    public void updateBigDecimal(String columnName, BigDecimal x)
            throws TSQLException {
        checkValid();
        resultSet.updateBigDecimal(columnName, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws TSQLException {
        checkValid();
        resultSet.updateBinaryStream(columnIndex, x, length);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws TSQLException {
        checkValid();
        resultSet.updateBinaryStream(columnName, x, length);
    }

    public void updateBlob(int columnIndex, TBlob x) throws TSQLException {
        checkValid();
        resultSet.updateBlob(columnIndex, x);
    }

    public void updateBlob(String columnName, TBlob x) throws TSQLException {
        checkValid();
        resultSet.updateBlob(columnName, x);
    }

    public void updateBoolean(int columnIndex, boolean x) throws TSQLException {
        checkValid();
        resultSet.updateBoolean(columnIndex, x);
    }

    public void updateBoolean(String columnName, boolean x) throws TSQLException {
        checkValid();
        resultSet.updateBoolean(columnName, x);
    }

    public void updateByte(int columnIndex, byte x) throws TSQLException {
        checkValid();
        resultSet.updateByte(columnIndex, x);
    }

    public void updateByte(String columnName, byte x) throws TSQLException {
        checkValid();
        resultSet.updateByte(columnName, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws TSQLException {
        checkValid();
        resultSet.updateBytes(columnIndex, x);
    }

    public void updateBytes(String columnName, byte[] x) throws TSQLException {
        checkValid();
        resultSet.updateBytes(columnName, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws TSQLException {
        checkValid();
        resultSet.updateCharacterStream(columnIndex, x, length);
    }

    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws TSQLException {
        checkValid();
        resultSet.updateCharacterStream(columnName, reader, length);
    }

    public void updateClob(int columnIndex, TClob x) throws TSQLException {
        checkValid();
        resultSet.updateClob(columnIndex, x);
    }

    public void updateClob(String columnName, TClob x) throws TSQLException {
        checkValid();
        resultSet.updateClob(columnName, x);
    }

    public void updateDate(int columnIndex, TDate x) throws TSQLException {
        checkValid();
        resultSet.updateDate(columnIndex, x);
    }

    public void updateDate(String columnName, TDate x) throws TSQLException {
        checkValid();
        resultSet.updateDate(columnName, x);
    }

    public void updateDouble(int columnIndex, double x) throws TSQLException {
        checkValid();
        resultSet.updateDouble(columnIndex, x);
    }

    public void updateDouble(String columnName, double x) throws TSQLException {
        checkValid();
        resultSet.updateDouble(columnName, x);
    }

    public void updateFloat(int columnIndex, float x) throws TSQLException {
        checkValid();
        resultSet.updateFloat(columnIndex, x);
    }

    public void updateFloat(String columnName, float x) throws TSQLException {
        checkValid();
        resultSet.updateFloat(columnName, x);
    }

    public void updateInt(int columnIndex, int x) throws TSQLException {
        checkValid();
        resultSet.updateInt(columnIndex, x);
    }

    public void updateInt(String columnName, int x) throws TSQLException {
        checkValid();
        resultSet.updateInt(columnName, x);
    }

    public void updateLong(int columnIndex, long x) throws TSQLException {
        checkValid();
        resultSet.updateLong(columnIndex, x);
    }

    public void updateLong(String columnName, long x) throws TSQLException {
        checkValid();
        resultSet.updateLong(columnName, x);
    }

    public void updateNull(int columnIndex) throws TSQLException {
        checkValid();
        resultSet.updateNull(columnIndex);
    }

    public void updateNull(String columnName) throws TSQLException {
        checkValid();
        resultSet.updateNull(columnName);
    }

    public void updateObject(int columnIndex, Object x) throws TSQLException {
        checkValid();
        resultSet.updateObject(columnIndex, x);
    }

    public void updateObject(int columnIndex, Object x, int scale)
            throws TSQLException {
        checkValid();
        resultSet.updateObject(columnIndex, x, scale);
    }

    public void updateObject(String columnName, Object x) throws TSQLException {
        checkValid();
        resultSet.updateObject(columnName, x);
    }

    public void updateObject(String columnName, Object x, int scale)
            throws TSQLException {
        checkValid();
        resultSet.updateObject(columnName, x, scale);
    }

    public void updateRef(int columnIndex, TRef x) throws TSQLException {
        checkValid();
        resultSet.updateRef(columnIndex, x);
    }

    public void updateRef(String columnName, TRef x) throws TSQLException {
        checkValid();
        resultSet.updateRef(columnName, x);
    }

    public void updateRow() throws TSQLException {
        checkValid();
        resultSet.updateRow();
    }

    public void updateShort(int columnIndex, short x) throws TSQLException {
        checkValid();
        resultSet.updateShort(columnIndex, x);
    }

    public void updateShort(String columnName, short x) throws TSQLException {
        checkValid();
        resultSet.updateShort(columnName, x);
    }

    public void updateString(int columnIndex, String x) throws TSQLException {
        checkValid();
        resultSet.updateString(columnIndex, x);
    }

    public void updateString(String columnName, String x) throws TSQLException {
        checkValid();
        resultSet.updateString(columnName, x);
    }

    public void updateTime(int columnIndex, TTime x) throws TSQLException {
        checkValid();
        resultSet.updateTime(columnIndex, x);
    }

    public void updateTime(String columnName, TTime x) throws TSQLException {
        checkValid();
        resultSet.updateTime(columnName, x);
    }

    public void updateTimestamp(int columnIndex, TTimestamp x)
            throws TSQLException {
        checkValid();
        resultSet.updateTimestamp(columnIndex, x);
    }

    public void updateTimestamp(String columnName, TTimestamp x)
            throws TSQLException {
        checkValid();
        resultSet.updateTimestamp(columnName, x);
    }

    public boolean wasNull() throws TSQLException {
        checkValid();
        return resultSet.wasNull();
    }

    public int getConcurrency() throws TSQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }

        return resultSet.getConcurrency();
    }

    public int getFetchDirection() throws TSQLException {
        if (resultSet == null) {
            throw new NullPointerException();
        }

        return resultSet.getFetchDirection();
    }

    private void checkValid() throws TSQLException {
        if (resultSet == null && connection == null) {
            throw new TSQLException(Messages.getString("rowset.30")); //$NON-NLS-1$
        }

        if (resultSet == null && connection != null) {
            throw new NullPointerException();
        }
    }

    private void initialProperties() {
        try {
            setEscapeProcessing(true);
            setTransactionIsolation(TConnection.TRANSACTION_READ_COMMITTED);
            setConcurrency(TResultSet.CONCUR_UPDATABLE);
            setType(TResultSet.TYPE_SCROLL_INSENSITIVE);
            setMaxRows(0);
            setQueryTimeout(0);
            setShowDeleted(false);
            setUsername(null);
            setPassword(null);
            setMaxFieldSize(0);
            setTypeMap(null);
            setFetchSize(0);
        } catch (TSQLException e) {
            // ignore, never reached
        }

    }
}
