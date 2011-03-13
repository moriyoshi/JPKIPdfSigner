package com.itextpdf.text.pdf;

import java.security.PublicKey;
import jp.go.jpki.appli.JPKICryptJNIException;
import java.io.Closeable;
import java.io.IOException;

@SuppressWarnings("serial")
public class JPKIPublicKey implements PublicKey, Closeable {
    public void finalize() throws Throwable {
        close();
    }

	public void close() throws IOException {
        try {
            JPKIWrapper.impl.cryptDestroyKey(handle);
        } catch (JPKICryptJNIException e) {
        }
	}
	
	public String getAlgorithm() {
		return "RSA";
	}

	public String getFormat() {
		return "X.509";
	}

	public byte[] getEncoded() {
		return bytes;
	}

	public JPKIPublicKey(JPKIWrapper wrapper, byte[] bytes, int handle) {
		this.bytes = bytes;
		this.handle = handle;
	}

	private byte[] bytes;
	int handle;
}
