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

import java.io.Serializable;
import java.util.Map;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TRef;
import org.teavm.classlib.java.sql.TSQLException;

public class SerialRef implements TRef, Serializable, Cloneable {
    private static final long serialVersionUID = -4727123500609662274L;

    /**
     * required by serialized form
     */
    private String baseTypeName;

    /**
     * store <code> reference </code> as an object, required by serialized form.
     */
    private Object object;

    /**
     * required by serialized form
     */
    private TRef reference;

    public SerialRef(TRef ref) throws SerialException, TSQLException {
        if (ref == null) {
            throw new TSQLException(Messages.getString("sql.9")); //$NON-NLS-1$
        }
        reference = ref;
        object = ref;
        baseTypeName = ref.getBaseTypeName();
        if (baseTypeName == null) {
            throw new TSQLException(Messages.getString("sql.10"));//$NON-NLS-1$
        }
    }

    public String getBaseTypeName() throws SerialException {
        return baseTypeName;
    }

    public Object getObject() throws SerialException {
        try {
            return reference.getObject();
        } catch (TSQLException e) {
            throw new SerialException(Messages.getString(
                    "sql.11", e.getMessage())); //$NON-NLS-1$
        }
    }

    public Object getObject(Map<String, Class<?>> map) throws SerialException {
        return map.get(object);
    }

    public void setObject(Object value) throws SerialException {
        try {
            reference.setObject(value);
            object = value;
        } catch (TSQLException e) {
            throw new SerialException(Messages.getString(
                    "sql.11", e.getMessage())); //$NON-NLS-1$
        }
    }

}
