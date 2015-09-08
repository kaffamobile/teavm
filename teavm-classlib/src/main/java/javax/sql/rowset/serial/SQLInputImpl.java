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

package javax.sql.rowset.serial;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TSQLData;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSQLInput;
import org.teavm.classlib.java.sql.TStruct;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;

/**
 * A concrete implementation of SQLInput. The readXXX methods will be called by
 * SQLData.readSQL, which read different objects such as Array, BigDecimal from
 * this SQLInputImpl object.
 * 
 * Different JDBC drivers may have their own implementation of SQLInput and
 * won't use this class.
 */
public class SQLInputImpl implements TSQLInput {

    private Object[] attributes;

    private Map<String, Class<?>> map;

    private int readPosition = 0;

    /**
     * Constructs a new SQLInputImpl object using an array of attributes and a
     * custom name-type map.
     * 
     * @param attributes -
     *            the array of given attribute objects.
     * @param map -
     *            the UDT(user defined type) name-type map
     * @throws TSQLException -
     *             if the attributes or the map is null
     */
    public SQLInputImpl(Object[] attributes, Map<String, Class<?>> map)
            throws TSQLException {
        if (null == attributes || null == map) {
            throw new TSQLException(Messages.getString("sql.34")); //$NON-NLS-1$
        }
        this.attributes = attributes;
        this.map = map;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readArray()
     */
    public TArray readArray() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TArray) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readAsciiStream()
     */
    public InputStream readAsciiStream() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (InputStream) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readBigDecimal()
     */
    public BigDecimal readBigDecimal() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (BigDecimal) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readBinaryStream()
     */
    public InputStream readBinaryStream() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (InputStream) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readBlob()
     */
    public TBlob readBlob() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TBlob) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readBoolean()
     */
    public boolean readBoolean() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? false : ((Boolean) o).booleanValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readByte()
     */
    public byte readByte() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? (byte) 0 : ((Byte) o).byteValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readBytes()
     */
    public byte[] readBytes() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (byte[]) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readCharacterStream()
     */
    public Reader readCharacterStream() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (Reader) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readClob()
     */
    public TClob readClob() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TClob) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readDate()
     */
    public TDate readDate() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TDate) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readDouble()
     */
    public double readDouble() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? 0 : ((Double) o).doubleValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readFloat()
     */
    public float readFloat() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? 0f : ((Float) o).floatValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readInt()
     */
    public int readInt() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? 0 : ((Integer) o).intValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readLong()
     */
    public long readLong() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? 0 : ((Long) o).longValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readObject()
     */
    public Object readObject() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        if (o instanceof TStruct) {
            TStruct structuredType = (TStruct) o;
            String typeName = structuredType.getSQLTypeName();
            Class<?> c = map.get(typeName);
            if (c != null) {
                try {
                    TSQLData data = (TSQLData) c.newInstance();
                    SQLInputImpl input = new SQLInputImpl(structuredType
                            .getAttributes(), map);
                    data.readSQL(input, typeName);
                    return data;
                } catch (IllegalAccessException e) {
                    throw new TSQLException(e.getMessage());
                } catch (InstantiationException e) {
                    throw new TSQLException(e.getMessage());
                }

            }
        }
        return o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readRef()
     */
    public TRef readRef() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TRef) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readShort()
     */
    public short readShort() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return o == null ? (short) 0 : ((Short) o).shortValue();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readString()
     */
    public String readString() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (String) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readTime()
     */
    public TTime readTime() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TTime) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readTimestamp()
     */
    public TTimestamp readTimestamp() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        Object o = attributes[readPosition++];
        return (TTimestamp) o;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#readURL()
     */
    public TURL readURL() throws TSQLException {
        if (readPosition >= attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        throw new TSQLException(Messages.getString("sql.37")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLInput#wasNull()
     */
    public boolean wasNull() throws TSQLException {
        if (readPosition > attributes.length) {
            throw new TSQLException(Messages.getString("sql.35")); //$NON-NLS-1$
        }
        return readPosition == 0 ? false : attributes[readPosition - 1] == null;
    }

}
