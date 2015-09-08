/*
 *  Copyright 2014 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.harmony.luni.platform;

import java.io.IOException;

import org.teavm.codegen.SourceWriter;
import org.teavm.javascript.spi.Generator;
import org.teavm.javascript.spi.GeneratorContext;
import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;

/**
 *
 * @author Alexey Andreev
 */
public class FSNativeGenerator implements Generator {
    @Override
    public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) throws IOException {
        final String name = methodRef.getName();
        if (name.startsWith("FS_")) {
        	function(context, writer, "FS." + name.substring(3), methodRef.parameterCount(), methodRef.getReturnType());
        } else {
        	System.err.println("Invalid method name " + name);
        }
    }

    private void function(GeneratorContext context, SourceWriter writer, String name, int paramCount, ValueType returnType)
            throws IOException {
    	
    	if (returnType != ValueType.VOID) {
    		writer.append("try {").softNewLine();
    		writer.append("return ");
    	}
    	
    	writer.append(name).append("(");
        
        for (int i = 0; i < paramCount; ++i) {
            if (i > 0) {
                writer.append(",").ws();
            }
            writer.append(context.getParameterName(i + 1));
        }
        
        writer.append(");").softNewLine();
        
    	if (returnType != ValueType.VOID) {
    		writer.append("} catch (e) {}").softNewLine();
    		writer.append("return null;").softNewLine();
    		//XXX set error info somewhere?
    	}
    }
}
