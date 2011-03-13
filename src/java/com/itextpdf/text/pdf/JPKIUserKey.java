package com.itextpdf.text.pdf;

import jp.go.jpki.appli.JPKICryptJNIException;
import java.io.Closeable;
import java.security.cert.X509Certificate;

public class JPKIUserKey implements Closeable {
    public void finalize() throws Throwable {
        close();
    }

    public void close() throws java.io.IOException {
        try {
            JPKIWrapper.impl.cryptDestroyKey(handle);
        } catch (JPKICryptJNIException e) {
            throw new JPKIWrapperException(e);
        }
    }

    public X509Certificate getCertificate() throws JPKIWrapperException {
    	try {
    		return JPKIWrapper.generateCertificate(JPKIWrapper.impl.cryptGetCertificateValue(handle));
    	} catch (Exception e) {
    		throw new JPKIWrapperException(e);
    	}
    }

    JPKIUserKey(JPKIWrapper wrapper, int key) {
        this.wrapper = wrapper;
        this.handle = key;
    }

    JPKIWrapper wrapper;
    int handle;
}
