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

import java.util.HashMap;

import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.sql.TArray;
import org.teavm.classlib.java.sql.TBlob;
import org.teavm.classlib.java.sql.TClob;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TStruct;
import org.teavm.classlib.java.sql.TTypes;

class DefaultUDTMap<T> {

    public static HashMap<String, Class<?>> DEFAULTMAP = new HashMap<String, Class<?>>();

    public static boolean isDefault(int type) {
        return (type == TTypes.ARRAY || type == TTypes.BLOB || type == TTypes.CLOB
                || type == TTypes.DATALINK || type == TTypes.STRUCT || type == TTypes.JAVA_OBJECT);
    }

    public static SerialDatalink[] processDatalink(Object[] elements)
            throws SerialException {
        SerialDatalink[] ret = new SerialDatalink[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = new SerialDatalink((TURL) elements[i]);
        }
        return ret;
    }

    public static TStruct[] processStruct(Object[] elements)
            throws SerialException {
        TStruct[] ret = new TStruct[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = (TStruct) elements[i];
        }
        return ret;
    }

    public static TArray[] processArray(Object[] elements)
            throws SerialException {
        TArray[] ret = new TArray[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = (TArray) elements[i];
        }
        return ret;
    }

    public static TClob[] processClob(Object[] elements) throws TSQLException {
        TClob[] ret = new TClob[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = new SerialClob((TClob) elements[i]);
        }
        return ret;
    }

    public static TBlob[] processBlob(Object[] elements) throws TSQLException {
        TBlob[] ret = new TBlob[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = new SerialBlob((TBlob) elements[i]);
        }
        return ret;
    }

    public static Object[] processObject(Object[] elements)
            throws SerialException {
        Object[] ret = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            ret[i] = new SerialJavaObject(elements[i]);
            // TODO according to RI, should do like this, but does it make
            // sense?
            elements[i] = ret[i];
        }
        return ret;
    }
}
