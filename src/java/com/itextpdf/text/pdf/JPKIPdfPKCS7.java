/*
 * $Id: PdfPKCS7.java 4602 2010-10-25 21:19:02Z psoares33 $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.tsp.TimeStampToken;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.error_messages.MessageLocalization;

/**
 * This class does all the processing related to signing and verifying a PKCS#7
 * signature.
 * <p>
 * It's based in code found at org.bouncycastle.
 */
public class JPKIPdfPKCS7 implements java.io.Closeable {
    private Certificate[] certs;
    private Certificate[] signCerts;
    private X509Certificate signCert;
    private byte[] digest;
    private int version = 1;
    private int signerversion = 1;
    private JPKISignature sig;

    private static final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    private static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    private static final String ID_RSA = "1.2.840.113549.1.1.1";
    private static final String ID_DSA = "1.2.840.10040.4.1";
    private static final String ID_CONTENT_TYPE = "1.2.840.113549.1.9.3";
    private static final String ID_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";
    private static final String ID_SIGNING_TIME = "1.2.840.113549.1.9.5";
    private static final String ID_ADBE_REVOCATION = "1.2.840.113583.1.1.8";
    private static final String ID_DIGEST_SHA1 = "1.3.14.3.2.26";

    private TimeStampToken timeStampToken;

    public void close() throws java.io.IOException {
    	sig.close();
    }
    
    /**
     * Gets the timestamp token if there is one.
     * @return the timestamp token or null
     * @since	2.1.6
     */
    public TimeStampToken getTimeStampToken() {
    	return timeStampToken;
    }

    /**
     * Gets the timestamp date
     * @return	a date
     * @since	2.1.6
     */
    public Calendar getTimeStampDate() {
        if (timeStampToken == null)
            return null;
        Calendar cal = new GregorianCalendar();
        Date date = timeStampToken.getTimeStampInfo().getGenTime();
        cal.setTime(date);
        return cal;
    }

    /**
     * Generates a signature.
     * @param jpki the JPKIWrapper
     */
    public JPKIPdfPKCS7(JPKIWrapper jpki) throws JPKIWrapperException
    {
    	this.signCert = jpki.getCertificate();
    	this.certs = new X509Certificate[] { this.signCert, jpki.getUserKey().getCertificate() };
    	this.signCerts = new X509Certificate[] { this.signCert };
    	this.sig = jpki.createSignature();
    }

    /**
     * Update the digest with the specified bytes. This method is used both for signing and verifying
     * @param buf the data buffer
     * @param off the offset in the data buffer
     * @param len the data length
     * @throws SignatureException on error
     */
    public void update(byte[] buf, int off, int len) throws SignatureException {
        sig.update(buf, off, len);
    }

    /**
     * Get all the X.509 certificates associated with this PKCS#7 object in no particular order.
     * Other certificates, from OCSP for example, will also be included.
     * @return the X.509 certificates associated with this PKCS#7 object
     */
    public Certificate[] getCertificates() {
        return certs;
    }

    /**
     * Get the X.509 sign certificate chain associated with this PKCS#7 object.
     * Only the certificates used for the main signature will be returned, with
     * the signing certificate first.
     * @return the X.509 certificates associated with this PKCS#7 object
     * @since	2.1.6
     */
    public Certificate[] getSignCertificateChain() {
        return signCerts;
    }

    /**
     * Get the X.509 certificate actually used to sign the digest.
     * @return the X.509 certificate actually used to sign the digest
     */
    public X509Certificate getSigningCertificate() {
        return signCert;
    }

    /**
     * Get the version of the PKCS#7 object. Always 1
     * @return the version of the PKCS#7 object. Always 1
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the version of the PKCS#7 "SignerInfo" object. Always 1
     * @return the version of the PKCS#7 "SignerInfo" object. Always 1
     */
    public int getSigningInfoVersion() {
        return signerversion;
    }

    /**
     * Get the "issuer" from the TBSCertificate bytes that are passed in
     * @param enc a TBSCertificate in a byte array
     * @return a DERObject
     */
    private static DERObject getIssuer(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 3 : 2);
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the "subject" from the TBSCertificate bytes that are passed in
     * @param enc A TBSCertificate in a byte array
     * @return a DERObject
     */
    private static DERObject getSubject(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence)in.readObject();
            return (DERObject)seq.getObjectAt(seq.getObjectAt(0) instanceof DERTaggedObject ? 5 : 4);
        }
        catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the issuer fields from an X509 Certificate
     * @param cert an X509Certificate
     * @return an X509Name
     */
    public static X509Name getIssuerFields(X509Certificate cert) {
        try {
            return new X509Name((ASN1Sequence)getIssuer(cert.getTBSCertificate()));
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the subject fields from an X509 Certificate
     * @param cert an X509Certificate
     * @return an X509Name
     */
    public static X509Name getSubjectFields(X509Certificate cert) {
        try {
            return new X509Name((ASN1Sequence)getSubject(cert.getTBSCertificate()));
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Gets the bytes for the PKCS#1 object.
     * @return a byte array
     */
    public byte[] getEncodedPKCS1() {
        try {
            digest = sig.sign();
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();

            ASN1OutputStream dout = new ASN1OutputStream(bOut);
            dout.writeObject(new DEROctetString(digest));
            dout.close();

            return bOut.toByteArray();
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Gets the bytes for the PKCS7SignedData object.
     * @return the bytes for the PKCS7SignedData object
     */
    public byte[] getEncodedPKCS7() {
    	try {
            digest = sig.sign();

            // Create the set of Hash algorithms
            ASN1EncodableVector digestAlgorithms = new ASN1EncodableVector();
            ASN1EncodableVector algos = new ASN1EncodableVector();
            algos.add(new DERObjectIdentifier(ID_DIGEST_SHA1));
            algos.add(DERNull.INSTANCE);
            digestAlgorithms.add(new DERSequence(algos));

            // Create the contentInfo.
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(ID_PKCS7_DATA));
            DERSequence contentinfo = new DERSequence(v);

            // Get all the certificates
            //
            v = new ASN1EncodableVector();
            for (Object element : certs) {
                ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(((X509Certificate)element).getEncoded()));
                v.add(tempstream.readObject());
            }

            DERSet dercertificates = new DERSet(v);

            // Create signerinfo structure.
            //
            ASN1EncodableVector signerinfo = new ASN1EncodableVector();

            // Add the signerInfo version
            //
            signerinfo.add(new DERInteger(signerversion));

            v = new ASN1EncodableVector();
            v.add(getIssuer(signCert.getTBSCertificate()));
            v.add(new DERInteger(signCert.getSerialNumber()));
            signerinfo.add(new DERSequence(v));

            // Add the digestAlgorithm
            v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(ID_DIGEST_SHA1));
            v.add(new DERNull());
            signerinfo.add(new DERSequence(v));

            // Add the digestEncryptionAlgorithm
            v = new ASN1EncodableVector();
            v.add(new DERObjectIdentifier(ID_RSA));
            v.add(new DERNull());
            signerinfo.add(new DERSequence(v));

            // Add the digest
            signerinfo.add(new DEROctetString(digest));

            // Finally build the body out of all the components above
            ASN1EncodableVector body = new ASN1EncodableVector();
            body.add(new DERInteger(version));
            body.add(new DERSet(digestAlgorithms));
            body.add(contentinfo);
            body.add(new DERTaggedObject(false, 0, dercertificates));

            // Only allow one signerInfo
            body.add(new DERSet(new DERSequence(signerinfo)));

            // Now we have the body, wrap it in it's PKCS7Signed shell
            // and return it
            //
            ASN1EncodableVector whole = new ASN1EncodableVector();
            whole.add(new DERObjectIdentifier(ID_PKCS7_SIGNED_DATA));
            whole.add(new DERTaggedObject(0, new DERSequence(body)));

            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();

            ASN1OutputStream dout = new ASN1OutputStream(bOut);
            dout.writeObject(new DERSequence(whole));
            dout.close();

            return bOut.toByteArray();
    	} catch (Exception e) {
    		throw ExceptionConverter.convertException(e);
    	}
    }

    /**
     * a class that holds an X509 name
     */
    public static class X509Name {
        /**
         * country code - StringType(SIZE(2))
         */
        public static final DERObjectIdentifier C = new DERObjectIdentifier("2.5.4.6");

        /**
         * organization - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier O = new DERObjectIdentifier("2.5.4.10");

        /**
         * organizational unit name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier OU = new DERObjectIdentifier("2.5.4.11");

        /**
         * Title
         */
        public static final DERObjectIdentifier T = new DERObjectIdentifier("2.5.4.12");

        /**
         * common name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier CN = new DERObjectIdentifier("2.5.4.3");

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier SN = new DERObjectIdentifier("2.5.4.5");

        /**
         * locality name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier L = new DERObjectIdentifier("2.5.4.7");

        /**
         * state, or province name - StringType(SIZE(1..64))
         */
        public static final DERObjectIdentifier ST = new DERObjectIdentifier("2.5.4.8");

        /** Naming attribute of type X520name */
        public static final DERObjectIdentifier SURNAME = new DERObjectIdentifier("2.5.4.4");
        /** Naming attribute of type X520name */
        public static final DERObjectIdentifier GIVENNAME = new DERObjectIdentifier("2.5.4.42");
        /** Naming attribute of type X520name */
        public static final DERObjectIdentifier INITIALS = new DERObjectIdentifier("2.5.4.43");
        /** Naming attribute of type X520name */
        public static final DERObjectIdentifier GENERATION = new DERObjectIdentifier("2.5.4.44");
        /** Naming attribute of type X520name */
        public static final DERObjectIdentifier UNIQUE_IDENTIFIER = new DERObjectIdentifier("2.5.4.45");

        /**
         * Email address (RSA PKCS#9 extension) - IA5String.
         * <p>Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
         */
        public static final DERObjectIdentifier EmailAddress = new DERObjectIdentifier("1.2.840.113549.1.9.1");

        /**
         * email address in Verisign certificates
         */
        public static final DERObjectIdentifier E = EmailAddress;

        /** object identifier */
        public static final DERObjectIdentifier DC = new DERObjectIdentifier("0.9.2342.19200300.100.1.25");

        /** LDAP User id. */
        public static final DERObjectIdentifier UID = new DERObjectIdentifier("0.9.2342.19200300.100.1.1");

        /** A HashMap with default symbols */
        public static HashMap<DERObjectIdentifier, String> DefaultSymbols = new HashMap<DERObjectIdentifier, String>();

        static {
            DefaultSymbols.put(C, "C");
            DefaultSymbols.put(O, "O");
            DefaultSymbols.put(T, "T");
            DefaultSymbols.put(OU, "OU");
            DefaultSymbols.put(CN, "CN");
            DefaultSymbols.put(L, "L");
            DefaultSymbols.put(ST, "ST");
            DefaultSymbols.put(SN, "SN");
            DefaultSymbols.put(EmailAddress, "E");
            DefaultSymbols.put(DC, "DC");
            DefaultSymbols.put(UID, "UID");
            DefaultSymbols.put(SURNAME, "SURNAME");
            DefaultSymbols.put(GIVENNAME, "GIVENNAME");
            DefaultSymbols.put(INITIALS, "INITIALS");
            DefaultSymbols.put(GENERATION, "GENERATION");
        }
        /** A HashMap with values */
        public HashMap<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>();

        /**
         * Constructs an X509 name
         * @param seq an ASN1 Sequence
         */
        @SuppressWarnings("unchecked")
        public X509Name(ASN1Sequence seq) {
            Enumeration<ASN1Set> e = seq.getObjects();

            while (e.hasMoreElements()) {
                ASN1Set set = e.nextElement();

                for (int i = 0; i < set.size(); i++) {
                    ASN1Sequence s = (ASN1Sequence)set.getObjectAt(i);
                    String id = DefaultSymbols.get(s.getObjectAt(0));
                    if (id == null)
                        continue;
                    ArrayList<String> vs = values.get(id);
                    if (vs == null) {
                        vs = new ArrayList<String>();
                        values.put(id, vs);
                    }
                    vs.add(((DERString)s.getObjectAt(1)).getString());
                }
            }
        }
        /**
         * Constructs an X509 name
         * @param dirName a directory name
         */
        public X509Name(String dirName) {
            X509NameTokenizer   nTok = new X509NameTokenizer(dirName);

            while (nTok.hasMoreTokens()) {
                String  token = nTok.nextToken();
                int index = token.indexOf('=');

                if (index == -1) {
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("badly.formated.directory.string"));
                }

                String id = token.substring(0, index).toUpperCase();
                String value = token.substring(index + 1);
                ArrayList<String> vs = values.get(id);
                if (vs == null) {
                    vs = new ArrayList<String>();
                    values.put(id, vs);
                }
                vs.add(value);
            }

        }

        public String getField(String name) {
            ArrayList<String> vs = values.get(name);
            return vs == null ? null : (String)vs.get(0);
        }

        /**
         * gets a field array from the values Hashmap
         * @param name
         * @return an ArrayList
         */
        public ArrayList<String> getFieldArray(String name) {
            ArrayList<String> vs = values.get(name);
            return vs == null ? null : vs;
        }

        /**
         * getter for values
         * @return a HashMap with the fields of the X509 name
         */
        public HashMap<String, ArrayList<String>> getFields() {
            return values;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return values.toString();
        }
    }

    /**
     * class for breaking up an X500 Name into it's component tokens, ala
     * java.util.StringTokenizer. We need this class as some of the
     * lightweight Java environment don't support classes like
     * StringTokenizer.
     */
    public static class X509NameTokenizer {
        private String          oid;
        private int             index;
        private StringBuffer    buf = new StringBuffer();

        public X509NameTokenizer(
        String oid) {
            this.oid = oid;
            this.index = -1;
        }

        public boolean hasMoreTokens() {
            return index != oid.length();
        }

        public String nextToken() {
            if (index == oid.length()) {
                return null;
            }

            int     end = index + 1;
            boolean quoted = false;
            boolean escaped = false;

            buf.setLength(0);

            while (end != oid.length()) {
                char    c = oid.charAt(end);

                if (c == '"') {
                    if (!escaped) {
                        quoted = !quoted;
                    }
                    else {
                        buf.append(c);
                    }
                    escaped = false;
                }
                else {
                    if (escaped || quoted) {
                        buf.append(c);
                        escaped = false;
                    }
                    else if (c == '\\') {
                        escaped = true;
                    }
                    else if (c == ',') {
                        break;
                    }
                    else {
                        buf.append(c);
                    }
                }
                end++;
            }

            index = end;
            return buf.toString().trim();
        }
    }
}
