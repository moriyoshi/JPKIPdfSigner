package com.itextpdf.text.pdf;

import java.io.IOException;

@SuppressWarnings("serial")
public class JPKIWrapperException extends IOException {
	public JPKIWrapperException(String message) {
		super(message);
	}

	public JPKIWrapperException(Exception e) {
		initCause(e);
	}

	public JPKIWrapperException(String message, Exception e) {
		super(message);
		initCause(e);
	}
}
