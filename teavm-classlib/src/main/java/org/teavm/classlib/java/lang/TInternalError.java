package org.teavm.classlib.java.lang;

public class TInternalError extends TError {

	private static final long serialVersionUID = 1L;

	public TInternalError() {
		super();
	}

	public TInternalError(TString message, TThrowable cause) {
		super(message, cause);
	}

	public TInternalError(TString message) {
		super(message);
	}

	public TInternalError(TThrowable cause) {
		super(cause);
	}

	
}
