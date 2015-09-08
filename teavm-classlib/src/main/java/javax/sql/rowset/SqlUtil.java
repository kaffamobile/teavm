/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.sql.rowset;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.harmony.sql.internal.nls.Messages;
import org.teavm.classlib.java.sql.TSQLException;
import org.teavm.classlib.java.sql.TTypes;

class SqlUtil {

    static void validateType(int type) throws TSQLException {
        try {
            int modifiers = -1;
            Field[] fields = TTypes.class.getFields();
            for (int index = 0; index < fields.length; index++) {
                // field should be int type
                if (int.class == fields[index].getType()) {
                    modifiers = fields[index].getModifiers();
                    // field should be static and final
                    if (Modifier.isStatic(modifiers)
                            && Modifier.isFinal(modifiers)) {
                        if (type == fields[index].getInt(TTypes.class)) {
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignored: this should never happen
        }
        throw new TSQLException(Messages.getString("sql.28")); //$NON-NLS-1$
    }

    static String getClassNameByType(int type) {
        String className = null;
        switch (type) {
        case TTypes.BINARY:
        case TTypes.BLOB:
        case TTypes.LONGVARBINARY:
        case TTypes.VARBINARY:
            className = new byte[0].getClass().getName();
            break;
        case TTypes.DOUBLE:
        case TTypes.FLOAT:
            className = Double.class.getName();
            break;
        case TTypes.BIGINT:
            className = Long.class.getName();
            break;
        case TTypes.BIT:
            className = Boolean.class.getName();
            break;
        case TTypes.DECIMAL:
        case TTypes.NUMERIC:
            className = java.math.BigDecimal.class.getName();
            break;
        case TTypes.CLOB:
            className = new char[0].getClass().getName();
            break;
        case TTypes.DATE:
            className = org.teavm.classlib.java.sql.TDate.class.getName();
            break;
        case TTypes.INTEGER:
            className = Integer.class.getName();
            break;
        case TTypes.REAL:
            className = Float.class.getName();
            break;
        case TTypes.SMALLINT:
            className = Short.class.getName();
            break;
        case TTypes.TIME:
            className = org.teavm.classlib.java.sql.TTime.class.getName();
            break;
        case TTypes.TIMESTAMP:
            className = org.teavm.classlib.java.sql.TTimestamp.class.getName();
            break;
        case TTypes.TINYINT:
            className = Byte.class.getName();
            break;
        default:
            className = String.class.getName();
        }
        return className;
    }
}
