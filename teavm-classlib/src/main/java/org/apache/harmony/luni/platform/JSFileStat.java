package org.apache.harmony.luni.platform;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface JSFileStat extends JSObject {

	@JSProperty
	public int getMode();

	@JSProperty
	public int getSize();

	@JSProperty("mtime")
	public JSObject getMtime();
	
}
