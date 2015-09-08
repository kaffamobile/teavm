/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.sql.rowset;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.sql.RowSet;
import javax.sql.RowSetEvent;
import javax.sql.RowSetListener;
import javax.sql.rowset.serial.SerialArray;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialRef;

import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TConnection;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TResultSet;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;

public abstract class BaseRowSet implements Cloneable, Serializable {
    private static final long serialVersionUID = 4886719666485113312L;

    public static final int UNICODE_STREAM_PARAM = 0;

    public static final int BINARY_STREAM_PARAM = 1;

    public static final int ASCII_STREAM_PARAM = 2;

    protected InputStream binaryStream;

    protected InputStream unicodeStream;

    protected InputStream asciiStream;

    protected Reader charStream;

    private String command;

    private String URL;

    private String dataSource;

    private int rowSetType = TResultSet.TYPE_SCROLL_INSENSITIVE;

    private boolean showDeleted;

    private int queryTimeout;

    private int maxRows;

    private int maxFieldSize;

    private int concurrency = TResultSet.CONCUR_UPDATABLE;

    //compatiable with RI, default: true
    private boolean readOnly = true;

    private boolean escapeProcessing;

    private int isolation;

    private int fetchDir = TResultSet.FETCH_FORWARD;

    private int fetchSize;

    private Map<String, Class<?>> map;

    private Vector<RowSetListener> listeners;

    private Hashtable<Object, Object> params;

    private transient String username;

    private transient String password;

    public BaseRowSet() {
        super();
        listeners = new Vector<RowSetListener>();
    }

    protected void initParams() {
        params = new Hashtable<Object, Object>();
    }

    public void addRowSetListener(RowSetListener listener) {
        if (listener == null) {
            return;
        }
        listeners.add(listener);
    }

    public void removeRowSetListener(RowSetListener listener) {
        if (listener == null) {
            return;
        }
        listeners.remove(listener);
    }

    protected void notifyCursorMoved() throws TSQLException {
        if (!(this instanceof RowSet)) {
            throw new TSQLException();
        }
        if (listeners.isEmpty()) {
            return;
        }
        for (RowSetListener listener : listeners) {
            listener.cursorMoved(new RowSetEvent((RowSet) this));
        }
    }

    protected void notifyRowChanged() throws TSQLException {
        if (!(this instanceof RowSet)) {
            throw new TSQLException();
        }
        if (listeners.isEmpty()) {
            return;
        }
        for (RowSetListener listener : listeners) {
            listener.rowChanged(new RowSetEvent((RowSet) this));
        }
    }

    protected void notifyRowSetChanged() throws TSQLException {
        if (!(this instanceof RowSet)) {
            throw new TSQLException();
        }
        if (listeners.isEmpty()) {
            return;
        }
        for (RowSetListener listener : listeners) {
            listener.rowSetChanged(new RowSetEvent((RowSet) this));
        }
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String cmd) throws TSQLException {
        // null is allowed, but empty is not
        if (cmd != null && cmd.length() == 0) {
            throw new TSQLException();
        }
        this.command = cmd;
        clearParameters();
    }

    public String getUrl() throws TSQLException {
        // TODO interrogate the DataSource
        return URL;
    }

    public void setUrl(String url) throws TSQLException {
        // null is allowed, but empty is not
        if (url != null && url.length() == 0) {
            throw new TSQLException();
        }
        this.URL = url;
        this.dataSource = null;
    }

    public String getDataSourceName() {
        return dataSource;
    }

    public void setDataSourceName(String name) throws TSQLException {
        // null is allowed, but empty is not
        if (name != null && name.length() == 0) {
            throw new TSQLException();
        }
        this.dataSource = name;
        this.URL = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setType(int type) throws TSQLException {
        switch (type) {
        case TResultSet.TYPE_FORWARD_ONLY:
        case TResultSet.TYPE_SCROLL_INSENSITIVE:
        case TResultSet.TYPE_SCROLL_SENSITIVE: {
            this.rowSetType = type;
            return;
        }
        default: {
            throw new TSQLException();
        }
        }
    }

    public int getType() throws TSQLException {
        return rowSetType;
    }

    public void setConcurrency(int concurrency) throws TSQLException {
        switch (concurrency) {
        case TResultSet.CONCUR_READ_ONLY:
        case TResultSet.CONCUR_UPDATABLE: {
            this.concurrency = concurrency;
            return;
        }
        default: {
            throw new TSQLException();
        }
        }
    }

    public int getConcurrency() throws TSQLException {
        return concurrency;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean value) {
        this.readOnly = value;
    }

    public int getTransactionIsolation() {
        return isolation;
    }

    public void setTransactionIsolation(int level) throws TSQLException {
        switch (level) {
        case TConnection.TRANSACTION_NONE:
        case TConnection.TRANSACTION_READ_UNCOMMITTED:
        case TConnection.TRANSACTION_READ_COMMITTED:
        case TConnection.TRANSACTION_REPEATABLE_READ:
        case TConnection.TRANSACTION_SERIALIZABLE: {
            this.isolation = level;
            return;
        }
        default: {
            throw new TSQLException();
        }
        }
    }

    public Map<String, Class<?>> getTypeMap() {
        return map;
    }

    public void setTypeMap(Map<String, Class<?>> map) {
        this.map = map;
    }

    public int getMaxFieldSize() throws TSQLException {
        return maxFieldSize;
    }

    public void setMaxFieldSize(int max) throws TSQLException {
        // TODO test maximum based on field type
        this.maxFieldSize = max;
    }

    public int getMaxRows() throws TSQLException {
        return maxRows;
    }

    public void setMaxRows(int max) throws TSQLException {
        this.maxRows = max;
    }

    public void setEscapeProcessing(boolean enable) throws TSQLException {
        this.escapeProcessing = enable;
    }

    public boolean getEscapeProcessing() throws TSQLException {
        return escapeProcessing;
    }

    public int getQueryTimeout() throws TSQLException {
        return queryTimeout;
    }

    public void setQueryTimeout(int seconds) throws TSQLException {
        if (seconds < 0) {
            throw new TSQLException();
        }
        this.queryTimeout = seconds;
    }

    public boolean getShowDeleted() throws TSQLException {
        return showDeleted;
    }

    public void setShowDeleted(boolean value) throws TSQLException {
        this.showDeleted = value;
    }

    public void setFetchDirection(int direction) throws TSQLException {
        switch (direction) {
        case TResultSet.FETCH_REVERSE:
        case TResultSet.FETCH_UNKNOWN: {
            if (rowSetType == TResultSet.TYPE_FORWARD_ONLY) {
                throw new TSQLException();
            }
        }
        case TResultSet.FETCH_FORWARD: {
            this.fetchDir = direction;
            return;
        }
        default: {
            throw new TSQLException();
        }
        }
    }

    public int getFetchDirection() throws TSQLException {
        return fetchDir;
    }

    public void setFetchSize(int rows) throws TSQLException {
        if (rows < 0) {
            throw new TSQLException();
        }
        if (maxRows != 0 && rows > maxRows) {
            throw new TSQLException();
        }
        this.fetchSize = rows;
    }

    public int getFetchSize() throws TSQLException {
        return fetchSize;
    }

    public void setNull(int parameterIndex, int sqlType) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[1] = Integer.valueOf(sqlType);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setNull(int parameterIndex, int sqlType, String typeName)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[3];
        value[1] = Integer.valueOf(sqlType);
        value[2] = typeName;
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setBoolean(int parameterIndex, boolean x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Boolean.valueOf(x));
    }

    public void setByte(int parameterIndex, byte x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Byte.valueOf(x));
    }

    public void setShort(int parameterIndex, short x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Short.valueOf(x));
    }

    public void setInt(int parameterIndex, int x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Integer.valueOf(x));
    }

    public void setLong(int parameterIndex, long x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Long.valueOf(x));
    }

    public void setFloat(int parameterIndex, float x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Float.valueOf(x));
    }

    public void setDouble(int parameterIndex, double x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), Double.valueOf(x));
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setString(int parameterIndex, String x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setDate(int parameterIndex, TDate x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setTime(int parameterIndex, TTime x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setTimestamp(int parameterIndex, TTimestamp x)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[3];
        value[0] = x;
        value[1] = Integer.valueOf(length);
        value[2] = Integer.valueOf(ASCII_STREAM_PARAM);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[3];
        value[0] = x;
        value[1] = Integer.valueOf(length);
        value[2] = Integer.valueOf(BINARY_STREAM_PARAM);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[3];
        value[0] = x;
        value[1] = Integer.valueOf(length);
        value[2] = Integer.valueOf(UNICODE_STREAM_PARAM);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[0] = reader;
        value[1] = Integer.valueOf(length);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType,
            int scale) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[3];
        value[0] = x;
        value[1] = Integer.valueOf(targetSqlType);
        value[2] = Integer.valueOf(scale);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[0] = x;
        value[1] = Integer.valueOf(targetSqlType);
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setObject(int parameterIndex, Object x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), x);
    }

    public void setRef(int parameterIndex, TRef ref) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), new SerialRef(ref));
    }

    public void setBlob(int parameterIndex, TBlob x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), new SerialBlob(x));
    }

    public void setClob(int parameterIndex, TClob x) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), new SerialClob(x));
    }

    public void setArray(int parameterIndex, TArray array) throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        params.put(Integer.valueOf(parameterIndex - 1), new SerialArray(array));
    }

    public void setDate(int parameterIndex, TDate x, Calendar cal)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[0] = x;
        value[1] = cal;
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setTime(int parameterIndex, TTime x, Calendar cal)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[0] = x;
        value[1] = cal;
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void setTimestamp(int parameterIndex, TTimestamp x, Calendar cal)
            throws TSQLException {
        if (parameterIndex < 1) {
            throw new TSQLException();
        }
        if (params == null) {
            throw new TSQLException();
        }
        Object[] value = new Object[2];
        value[0] = x;
        value[1] = cal;
        params.put(Integer.valueOf(parameterIndex - 1), value);
    }

    public void clearParameters() throws TSQLException {
        if (params == null) {
            return;
        }
        params.clear();
    }

    public Object[] getParams() throws TSQLException {
        if (params == null) {
            return new Object[0];
        }
        Object[] result = new Object[params.size()];
        for (int i = 0; i < result.length; i++) {
            Object param = params.get(Integer.valueOf(i));
            if (param == null) {
                throw new TSQLException();
            }
            result[i] = param;
        }
        return result;
    }

}
