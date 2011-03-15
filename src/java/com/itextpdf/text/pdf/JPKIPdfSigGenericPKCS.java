/*
 * $Id: PdfSigGenericPKCS.java 4113 2009-12-01 11:08:59Z blowagie $
 *
 * This file is part of the iText project.
 * Copyright (c) 1998-2009 1T3XT BVBA
 * Authors: Bruno Lowagie, Paulo Soares, et al.
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
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
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

import java.io.ByteArrayOutputStream;
import java.security.cert.Certificate;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfSignature;

/**
 * A signature dictionary representation for the standard filters.
 */
public abstract class JPKIPdfSigGenericPKCS extends PdfSignature implements java.io.Closeable {
    /**
     * The hash algorithm, for example "SHA1"
     */    
    protected String hashAlgorithm;
    /**
     * The crypto provider
     */    
    protected String provider = null;
    /**
     * The class instance that calculates the PKCS#1 and PKCS#7
     */    
    protected JPKIPdfPKCS7 pkcs;
    /**
     * The subject name in the signing certificate (the element "CN")
     */    
    protected String   name;

    public void close() throws java.io.IOException {
        if (this.pkcs != null)
            this.pkcs.close();
    }
    /**
     * Creates a generic standard filter.
     * @param filter the filter name
     * @param subFilter the sub-filter name
     */    
    public JPKIPdfSigGenericPKCS(PdfName filter, PdfName subFilter) {
        super(filter, subFilter);
    }

    /**
     * Sets the crypto information to sign.
     * @param privKey the private key
     * @param certChain the certificate chain
     * @param crlList the certificate revocation list. It can be <CODE>null</CODE>
     */    
    public void setJPKIWrapper(JPKIWrapper jpki)  {
        try {
            boolean hasRSAdata = PdfName.ADBE_PKCS7_SHA1.equals(get(PdfName.SUBFILTER));
            JPKIPdfPKCS7 _pkcs = new JPKIPdfPKCS7(jpki, hasRSAdata);
            try {
                Certificate[] certChain = _pkcs.getSignCertificateChain();
                if (PdfName.ADBE_X509_RSA_SHA1.equals(get(PdfName.SUBFILTER))) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    for (int k = 0; k < certChain.length; ++k) {
                        bout.write(certChain[k].getEncoded());
                    }
                    bout.close();
                    setCert(bout.toByteArray());
                    setContents(_pkcs.getEncodedPKCS1());
                }
                else
                    setContents(_pkcs.getEncodedPKCS7());
                name = JPKIPdfPKCS7.getSubjectFields(_pkcs.getSigningCertificate()).getField("CN");
                if (name != null)
                    put(PdfName.NAME, new PdfString(name, PdfObject.TEXT_UNICODE));
            } finally {
                _pkcs.close();
            }
            this.pkcs = new JPKIPdfPKCS7(jpki, hasRSAdata);
        }
        catch (Exception e) {
            throw ExceptionConverter.convertException(e);
        }
    }
 
    /**
     * Gets the subject name in the signing certificate (the element "CN")
     * @return the subject name in the signing certificate (the element "CN")
     */    
    public String getName() {
        return name;
    }

    /**
     * Gets the class instance that does the actual signing.
     * @return the class instance that does the actual signing
     */    
    public JPKIPdfPKCS7 getSigner() {
        return pkcs;
    }

    /**
     * Gets the signature content. This can be a PKCS#1 or a PKCS#7. It corresponds to
     * the /Contents key.
     * @return the signature content
     */    
    public byte[] getSignerContents() {
        if (PdfName.ADBE_X509_RSA_SHA1.equals(get(PdfName.SUBFILTER)))
            return pkcs.getEncodedPKCS1();
        else
            return pkcs.getEncodedPKCS7();
    }

    /**
     * Creates a standard filter of the type VeriSign.
     */    
    public static class VeriSign extends JPKIPdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public VeriSign() {
            super(PdfName.VERISIGN_PPKVS, PdfName.ADBE_PKCS7_DETACHED);
            hashAlgorithm = "MD5";
            put(PdfName.R, new PdfNumber(65537));
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public VeriSign(String provider) {
            this();
            this.provider = provider;
        }
    }

    /**
     * Creates a standard filter of the type self signed.
     */    
    public static class PPKLite extends JPKIPdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public PPKLite() {
            super(PdfName.ADOBE_PPKLITE, PdfName.ADBE_X509_RSA_SHA1);
            hashAlgorithm = "SHA1";
            put(PdfName.R, new PdfNumber(65541));
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public PPKLite(String provider) {
            this();
            this.provider = provider;
        }
    }

    /**
     * Creates a standard filter of the type Windows Certificate.
     */    
    public static class PPKMS extends JPKIPdfSigGenericPKCS {
        /**
         * The constructor for the default provider.
         */        
        public PPKMS() {
            super(PdfName.ADOBE_PPKMS, PdfName.ADBE_PKCS7_SHA1);
            hashAlgorithm = "SHA1";
        }

        /**
         * The constructor for an explicit provider.
         * @param provider the crypto provider
         */        
        public PPKMS(String provider) {
            this();
            this.provider = provider;
        }
    }
}
