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
package org.teavm.classlib.java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.util.TEnumeration;

/**
 *
 * @author Alexey Andreev
 */
public abstract class TClassLoader extends TObject {
    public static final class EmptyEnumeration implements TEnumeration<URL> {
		@Override
		public boolean hasMoreElements() {
			return false;
		}

		@Override
		public URL nextElement() {
			return null;
		}
	}

	private TClassLoader parent;
    private static TSystemClassLoader systemClassLoader = new TSystemClassLoader();

    protected TClassLoader() {
        this(null);
    }

    protected TClassLoader(TClassLoader parent) {
        this.parent = parent;
    }

    public TClassLoader getParent() {
        return parent;
    }

    public static TClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }
    
    public TInputStream getResourceAsStream(String resName) {
        return null;
    }
    
    public static InputStream getSystemResourceAsStream(String name) {
    	return null;
    }
    
    public TEnumeration<URL> getResources(String resName) throws IOException {
    	return new EmptyEnumeration();
    }

    public TEnumeration<URL> getSystemResources(String resName) throws IOException {
    	return new EmptyEnumeration();
    }


}
