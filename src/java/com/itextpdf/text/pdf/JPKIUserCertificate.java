package com.itextpdf.text.pdf;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import jp.go.jpki.appli.JPKIUserCertService;
import jp.go.jpki.appli.JPKIUserCertException;
import jp.go.jpki.appli.JPKIUserCertBasicData;
import jp.go.jpki.appli.JPKIUserCertCertStatus;
import jp.go.jpki.appli.JPKIConfirmResult;

public class JPKIUserCertificate extends X509Certificate {
    public void checkValidity() throws java.security.cert.CertificateExpiredException, java.security.cert.CertificateNotYetValidException {
        impl.checkValidity();
    }

    public void checkValidity(java.util.Date date) throws java.security.cert.CertificateExpiredException, java.security.cert.CertificateNotYetValidException {
        impl.checkValidity(date);
    }

    public int getVersion() {
        return impl.getVersion();
    }

    public java.math.BigInteger getSerialNumber() {
        return impl.getSerialNumber();
    }

    public java.security.Principal getIssuerDN() {
        return impl.getIssuerDN();
    }

    public javax.security.auth.x500.X500Principal getIssuerX500Principal() {
        return impl.getIssuerX500Principal();
    }

    public java.security.Principal getSubjectDN() {
        return impl.getSubjectDN();
    }

    public javax.security.auth.x500.X500Principal getSubjectX500Principal() {
        return impl.getSubjectX500Principal();
    }

    public java.util.Date getNotBefore() {
        return impl.getNotBefore();
    }

    public java.util.Date getNotAfter() {
        return impl.getNotAfter();
    }

    public byte[] getTBSCertificate() throws java.security.cert.CertificateEncodingException {
        return impl.getTBSCertificate();
    }

    public byte[] getSignature() {
        return impl.getSignature();
    }

    public java.lang.String getSigAlgName() {
        return impl.getSigAlgName();
    }

    public java.lang.String getSigAlgOID() {
        return impl.getSigAlgOID();
    }

    public byte[] getSigAlgParams() {
        return impl.getSigAlgParams();
    }

    public boolean[] getIssuerUniqueID() {
        return impl.getIssuerUniqueID();
    }

    public boolean[] getSubjectUniqueID() {
        return impl.getSubjectUniqueID();
    }

    public boolean[] getKeyUsage() {
        return impl.getKeyUsage();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public java.util.List getExtendedKeyUsage() throws java.security.cert.CertificateParsingException {
        return impl.getExtendedKeyUsage();
    }

    public int getBasicConstraints() {
        return impl.getBasicConstraints();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public java.util.Collection getSubjectAlternativeNames() throws java.security.cert.CertificateParsingException {
        return impl.getSubjectAlternativeNames();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public java.util.Collection getIssuerAlternativeNames() throws java.security.cert.CertificateParsingException {
        return impl.getIssuerAlternativeNames();
    }

    public byte[] getEncoded() {
        return this.encoded;
    }

    public JPKIUserCertBasicData getBasicData() throws JPKIUserCertException {
        return service.getBasicData();
    }

    public JPKIConfirmResult confirm() throws JPKIUserCertException {
        return service.confirm();
    }

    public JPKIUserCertCertStatus verifyCertificate() throws JPKIUserCertException {
        return service.verifyCert();
    }

    public Set<String> getCriticalExtensionOIDs() {
        return impl.getCriticalExtensionOIDs();
    }

    public byte[] getExtensionValue(String arg0) {
        return impl.getExtensionValue(arg0);
    }

    public Set<String> getNonCriticalExtensionOIDs() {
        return impl.getNonCriticalExtensionOIDs();
    }

    public boolean hasUnsupportedCriticalExtension() {
        return impl.hasUnsupportedCriticalExtension();
    }

    @Override
    public PublicKey getPublicKey() {
        return impl.getPublicKey();
    }

    @Override
    public void verify(PublicKey arg0) throws CertificateException,
            NoSuchAlgorithmException, InvalidKeyException,
            NoSuchProviderException, SignatureException {
        impl.verify(arg0);
    }

    @Override
    public void verify(PublicKey arg0, String arg1)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException, SignatureException {
        impl.verify(arg0, arg1);
    }

    @Override
    public String toString() {
        return impl.toString();
    }

    public JPKIUserCertificate(X509Certificate impl, byte[] encoded) throws JPKIUserCertException {
        this.impl = impl;
        this.encoded = encoded;
        this.service = new JPKIUserCertService(encoded);
    }

    private X509Certificate impl;
    private byte[] encoded;
    private JPKIUserCertService service;
}
