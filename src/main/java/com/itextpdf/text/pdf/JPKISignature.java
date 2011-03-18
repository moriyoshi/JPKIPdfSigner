/*
 * Copyright (c) 2011 Moriyoshi Koizumi
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * you must retain the producer line in every PDF that is created or manipulated
 * using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf;

import jp.go.jpki.appli.JPKICryptJNIException;
import java.security.Signature;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;
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
