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

import jp.go.jpki.appli.JPKICryptJNI;
import jp.go.jpki.appli.JPKICryptJNIException;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.security.PublicKey;
import java.security.cert.CertificateException;
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
    	} catch (Exception e) {
    		throw new JPKIWrapperException(e);
    	}
    }
    
    public JPKIWrapper() {}
    
    public static X509Certificate generateCertificate(final byte[] cert) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
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
