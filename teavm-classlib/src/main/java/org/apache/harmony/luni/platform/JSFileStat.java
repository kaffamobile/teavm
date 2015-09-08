package org.apache.harmony.luni.platform;

import org.teavm.jso.JSObject;

public interface JSFileStat extends JSObject {

	public int getMode();

	public int getSize();

	public JSObject getMtime();
	
}
