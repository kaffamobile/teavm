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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TDate;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TSQLData;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TSQLOutput;
import org.teavm.classlib.java.sql.TStruct;
import org.teavm.classlib.java.sql.TTime;
import org.teavm.classlib.java.sql.TTimestamp;

public class SQLOutputImpl implements TSQLOutput {
    private Vector attributes;

    private Map map;

    /**
     * Constructs a new SQLOutputImpl object using a list of attributes and a
     * custom name-type map. JDBC drivers will use this map to identify which
     * SQLData.writeSQL will be invoked.
     * 
     * @param attributes -
     *            the list of given attribute objects.
     * @param map -
     *            the UDT(user defined type) name-type map
     * @throws TSQLException -
     *             if the attributes or the map is null
     */
    public SQLOutputImpl(Vector<?> attributes, Map<String, ?> map)
            throws TSQLException {
        if (null == attributes || null == map) {
            throw new TSQLException(Messages.getString("sql.33")); //$NON-NLS-1$
        }
        this.attributes = attributes;
        this.map = map;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeArray(TArray)
     */
    @SuppressWarnings("unchecked")
    public void writeArray(TArray theArray) throws TSQLException {
        if (theArray != null) {
            SerialArray serialArray = new SerialArray(theArray, map);
            attributes.addElement(serialArray);
        } else {
            attributes.addElement(theArray);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeAsciiStream(InputStream)
     */
    @SuppressWarnings("unchecked")
    public void writeAsciiStream(InputStream theStream) throws TSQLException {
        BufferedReader br = new BufferedReader(new InputStreamReader(theStream));
        StringBuilder stringBuffer = new StringBuilder();
        String line;
        try {
            line = br.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = br.readLine();
            }
            attributes.addElement(stringBuffer.toString());
        } catch (IOException e) {
            throw new TSQLException();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeBigDecimal(BigDecimal)
     */
    @SuppressWarnings("unchecked")
    public void writeBigDecimal(BigDecimal theBigDecimal) throws TSQLException {
        attributes.addElement(theBigDecimal);
    }

    /**
     * {@inheritDoc}
     * 
     * FIXME So far NO difference has been detected between writeBinaryStream
     * and writeAsciiStream in RI. Keep their implementation same temporarily
     * until some bug is found.
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeBinaryStream(InputStream)
     */
    @SuppressWarnings("unchecked")
    public void writeBinaryStream(InputStream theStream) throws TSQLException {
        writeAsciiStream(theStream);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeBlob(TBlob)
     */
    @SuppressWarnings("unchecked")
    public void writeBlob(TBlob theBlob) throws TSQLException {
        if (theBlob != null) {
            SerialBlob serialBlob = new SerialBlob(theBlob);
            attributes.addElement(serialBlob);
        } else {
            attributes.addElement(theBlob);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeBoolean(boolean)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeBoolean(boolean theFlag) throws TSQLException {
        attributes.addElement(theFlag);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeByte(byte)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeByte(byte theByte) throws TSQLException {
        attributes.addElement(theByte);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeBytes(byte[])
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeBytes(byte[] theBytes) throws TSQLException {
        attributes.addElement(theBytes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeCharacterStream(Reader)
     */
    @SuppressWarnings("unchecked")
    public void writeCharacterStream(Reader theStream) throws TSQLException {
        BufferedReader br = new BufferedReader(theStream);
        StringBuilder stringBuffer = new StringBuilder();
        String line;
        try {
            line = br.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = br.readLine();
            }
            attributes.addElement(stringBuffer.toString());
        } catch (IOException e) {
            throw new TSQLException();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeClob(TClob)
     */
    @SuppressWarnings("unchecked")
    public void writeClob(TClob theClob) throws TSQLException {
        if (theClob != null) {
            SerialClob serialClob = new SerialClob(theClob);
            attributes.addElement(serialClob);
        } else {
            attributes.addElement(theClob);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeDate(TDate)
     */
    @SuppressWarnings("unchecked")
    public void writeDate(TDate theDate) throws TSQLException {
        attributes.addElement(theDate);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeDouble(double)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeDouble(double theDouble) throws TSQLException {
        attributes.addElement(theDouble);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeFloat(float)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeFloat(float theFloat) throws TSQLException {
        attributes.addElement(theFloat);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeInt(int)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeInt(int theInt) throws TSQLException {
        attributes.addElement(theInt);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeLong(long)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeLong(long theLong) throws TSQLException {
        attributes.addElement(theLong);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeObject(TSQLData)
     */
    @SuppressWarnings("unchecked")
    public void writeObject(TSQLData theObject) throws TSQLException {
        if (theObject == null) {
            attributes.addElement(null);
        } else {
            attributes
                    .addElement(new SerialStruct(theObject, new HashMap(map)));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeRef(TRef)
     */
    @SuppressWarnings("unchecked")
    public void writeRef(TRef theRef) throws TSQLException {
        if (theRef != null) {
            SerialRef serialRef = new SerialRef(theRef);
            attributes.addElement(serialRef);
        } else {
            attributes.addElement(theRef);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeShort(short)
     */
    @SuppressWarnings( { "boxing", "unchecked" })
    public void writeShort(short theShort) throws TSQLException {
        attributes.addElement(theShort);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeString(String)
     */
    @SuppressWarnings("unchecked")
    public void writeString(String theString) throws TSQLException {
        attributes.addElement(theString);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeStruct(TStruct)
     */
    @SuppressWarnings("unchecked")
    public void writeStruct(TStruct theStruct) throws TSQLException {
        if (theStruct != null) {
            SerialStruct serialStruct = new SerialStruct(theStruct, map);
            attributes.addElement(serialStruct);
        } else {
            attributes.addElement(theStruct);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeTime(TTime)
     */
    @SuppressWarnings("unchecked")
    public void writeTime(TTime theTime) throws TSQLException {
        attributes.addElement(theTime);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeTimestamp(TTimestamp)
     */
    @SuppressWarnings("unchecked")
    public void writeTimestamp(TTimestamp theTimestamp) throws TSQLException {
        attributes.addElement(theTimestamp);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.teavm.classlib.java.sql.TSQLOutput#writeURL(TURL)
     */
    @SuppressWarnings("unchecked")
    public void writeURL(TURL theURL) throws TSQLException {
        if (theURL != null) {
            SerialDatalink serialDatalink = new SerialDatalink(theURL);
            attributes.addElement(serialDatalink);
        } else {
            attributes.addElement(theURL);
        }
    }
}
