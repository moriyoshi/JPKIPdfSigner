package com.itextpdf.text.pdf;

import jp.go.jpki.appli.JPKICryptJNIException;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.io.Closeable;

public class JPKISignature extends Signature implements Closeable {
    public void finalize() throws Throwable {
        close();
    }

    public void close() throws java.io.IOException {
        try {
            JPKIWrapper.impl.cryptDestroyHash(handle);
        } catch (JPKICryptJNIException e) {
            throw new JPKIWrapperException(e);
        }
    }

    protected void engineSetParameter(AlgorithmParameterSpec p) {}

    /**
     * @deprecated
     */
    protected void engineSetParameter(String name, Object value) {}

    /**
     * @deprecated
     */
    protected Object engineGetParameter(String name) {
        return null;
    }

    protected void engineInitSign(PrivateKey key) {
        state = SIGN;
    }

    protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
        if (key instanceof JPKIPublicKey) {
            pubKey = (JPKIPublicKey)key;
        } else {
            if (!key.getAlgorithm().equals("RSA")) {
                throw new InvalidKeyException("Key must be RSA");
            }
            if (!key.getFormat().equals("X.509")) {
                throw new InvalidKeyException("Key must be encoded according to X.509");
            }
            try {
                pubKey = wrapper.createJPKIPublicKey(key.getEncoded());
            } catch (JPKIWrapperException e) {
                throw new InvalidKeyException(e);
            }
        }
        state = VERIFY;
    }
    
    protected void engineUpdate(byte c) throws SignatureException {
        try {
            JPKIWrapper.impl.cryptHashData(handle, new byte[] { c });
        } catch (JPKICryptJNIException e) {
            throw new SignatureException(e);
        }
    }
 
    protected void engineUpdate(byte[] bytes, int off, int len) throws SignatureException {
        byte[] buf = new byte[len];
        System.arraycopy(bytes, off, buf, 0, len);
        try {
            JPKIWrapper.impl.cryptHashData(handle, bytes);
        } catch (JPKICryptJNIException e) {
            throw new SignatureException(e);
        }
    }

    protected byte[] engineSign() throws SignatureException {
        try {
            return JPKIWrapper.impl.cryptSignHash(handle);
        } catch (JPKICryptJNIException e) {
            throw new SignatureException(e);
        }
      }

    protected int engineSign(byte[] outbuf, int offset, int len) throws SignatureException {
        byte[] buf = engineSign();
        if (len < buf.length) {
            throw new SignatureException("length too short");
        }
        System.arraycopy(buf, 0, outbuf, offset, buf.length);
        return buf.length;
    }
    
    protected boolean engineVerify(byte[] data) throws SignatureException {
        try {
            return JPKIWrapper.impl.cryptVerifySignature(handle, data, pubKey.handle);
        } catch (JPKICryptJNIException e) {
            throw new SignatureException(e);
        }
    }

    public byte[] digest() throws SignatureException {
        try {
            return JPKIWrapper.impl.cryptGetHashValue(handle);
        } catch (JPKICryptJNIException e) {
            throw new SignatureException(e);
        }
    }

    JPKISignature(JPKIWrapper wrapper, int handle) {
        super("SHA1");
        this.wrapper = wrapper;
        this.handle = handle;
        this.engineInitSign(null);
    }

    JPKISignature(JPKIWrapper wrapper, int handle, PublicKey key) throws InvalidKeyException {
        super("SHA1");
        this.wrapper = wrapper;
        this.handle = handle;
        this.engineInitVerify(key);
    }

    JPKIWrapper wrapper;
    JPKIPublicKey pubKey;
    int handle;
}
