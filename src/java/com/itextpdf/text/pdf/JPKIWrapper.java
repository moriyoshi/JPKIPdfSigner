package com.itextpdf.text.pdf;

import jp.go.jpki.appli.JPKICryptJNI;
import jp.go.jpki.appli.JPKICryptJNIException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class JPKIWrapper implements Closeable {
	public static final int NONE = 0;
	public static final int VERIFY = 1;
	public static final int USER_KEY = 2;

    static {
        impl = new JPKICryptJNI();
    }

    public void finalize() throws Throwable {
        close();
    }

    public void close() throws java.io.IOException {
        try {
            impl.cryptReleaseContext(ctx);
        } catch (JPKICryptJNIException e) {
            throw new JPKIWrapperException(e);
        }
    }

    public JPKIUserKey getUserKey() throws JPKIWrapperException {
		try {
			acquireContext(USER_KEY);
			return new JPKIUserKey(this, impl.cryptGetUserKey(ctx));
		} catch (JPKICryptJNIException e) {
			throw new JPKIWrapperException(e);
		}
    }
 
    public JPKISignature createSignature() throws JPKIWrapperException {
		try {
			acquireContext(USER_KEY);
			return new JPKISignature(this, impl.cryptCreateHash(ctx));
		} catch (JPKICryptJNIException e) {
			throw new JPKIWrapperException(e);
		}
	}

    public JPKISignature createSignature(PublicKey key) throws JPKIWrapperException {
		try {
			acquireContext(VERIFY);
			return new JPKISignature(this, impl.cryptCreateHash(ctx), key);
		} catch (Exception e) {
			throw new JPKIWrapperException(e);
		}
	}

    public JPKIPublicKey createJPKIPublicKey(byte[] pubKey) throws JPKIWrapperException {
    	try {
    		acquireContext(VERIFY);
    		return new JPKIPublicKey(this, pubKey, impl.cryptImportPublicKey(ctx, pubKey));
    	} catch (JPKICryptJNIException e) {
    		throw new JPKIWrapperException(e);
    	}
    }

    public X509Certificate getCertificate() throws JPKIWrapperException {
    	try {
    		acquireContext(VERIFY);
    		return generateCertificate(impl.cryptGetRootCertificateValue(ctx));
    	} catch (JPKICryptJNIException e) {
    		throw new JPKIWrapperException(e);
    	}
    }
    
    public JPKIWrapper() {}
    
    public static X509Certificate generateCertificate(final byte[] cert) throws JPKIWrapperException {
    	try {
	    	CertificateFactory cf = CertificateFactory.getInstance("X.509");
	    	return (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
    	} catch (Exception e) {
    		throw new JPKIWrapperException(e);
    	}
    }

    void acquireContext(int level) throws JPKICryptJNIException {
    	if (this.level >= level)
    		return;
    	synchronized (this) {
    		final int jpkiLevel = level == USER_KEY ? 0: JPKICryptJNI.JPKI_VERIFYCONTEXT;
    		if (this.level > 0) {
    			impl.cryptReleaseContext(this.ctx);
    		}
            this.ctx = impl.cryptAcquireContext(jpkiLevel);
            this.level = level;
    	}
    }

    static final JPKICryptJNI impl;
    volatile int level = NONE;
    int ctx;
}
