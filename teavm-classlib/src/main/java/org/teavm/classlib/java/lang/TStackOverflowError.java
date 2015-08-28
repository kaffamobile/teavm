package org.teavm.classlib.java.lang;


public class TStackOverflowError extends TVirtualMachineError {

    private static final long serialVersionUID = 1477673893596993491L;

	public TStackOverflowError() {
        super();
    }

    public TStackOverflowError(TString s) {
        super(s);
    }
    
}