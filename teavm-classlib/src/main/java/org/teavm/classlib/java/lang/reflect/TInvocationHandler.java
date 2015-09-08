package org.teavm.classlib.java.lang.reflect;

import org.teavm.classlib.java.lang.TObject;
import org.teavm.classlib.java.lang.TThrowable;

public interface TInvocationHandler {

	public TObject invoke(TObject proxy, TMethod method, TObject[] args)
	        throws TThrowable;
}
