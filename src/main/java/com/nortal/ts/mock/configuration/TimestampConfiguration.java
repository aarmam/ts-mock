package com.nortal.ts.mock.configuration;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.bouncycastle.tsp.TSPAlgorithms.*;
import static org.bouncycastle.tsp.TimeStampTokenGenerator.R_MILLISECONDS;

@Configuration
public class TimestampConfiguration {

    @Bean
    public KeyStore tsKeyStore(TimestampProperties tsProperties, ResourceLoader loader) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        Resource resource = loader.getResource(tsProperties.keyStorePath());
        KeyStore trustStore = KeyStore.getInstance(tsProperties.keyStoreType());
        trustStore.load(resource.getInputStream(), tsProperties.keyStorePassword().toCharArray());
        return trustStore;
    }

    @Bean
    public X509Certificate tsCertificate(TimestampProperties tsProperties, KeyStore tsKeyStore) throws KeyStoreException {
        return (X509Certificate) tsKeyStore.getCertificate(tsProperties.keyAlias());
    }

    @Bean
    public PrivateKey tsPrivateKey(TimestampProperties tsProperties, KeyStore tsKeyStore) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return (PrivateKey) tsKeyStore.getKey(tsProperties.keyAlias(), tsProperties.keyPassword().toCharArray());
    }

    @Bean
    public TimeStampResponseGenerator timeStampResponseGenerator(X509Certificate tsCertificate, PrivateKey tsPrivateKey) throws CertificateEncodingException, TSPException, IOException, OperatorCreationException {
        X509CertificateHolder certificate = new X509CertificateHolder(tsCertificate.getEncoded());
        AlgorithmIdentifier digestAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(DigestAlgorithm.SHA256.getOid()));
        AlgorithmIdentifier encryptionAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption);
        DefaultCMSSignatureAlgorithmNameGenerator sigAlgoGenerator = new DefaultCMSSignatureAlgorithmNameGenerator();
        String sigAlgoName = sigAlgoGenerator.getSignatureName(digestAlgorithmIdentifier, encryptionAlg);
        ContentSigner signer = new JcaContentSignerBuilder(sigAlgoName).build(tsPrivateKey);
        List<X509Certificate> chain = new ArrayList<>();
        chain.add(tsCertificate);
        SignerInfoGenerator infoGenerator = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider()).build(signer, certificate);
        DigestCalculator digestCalculator = new JcaDigestCalculatorProviderBuilder().build().get(digestAlgorithmIdentifier);
        TimeStampTokenGenerator tokenGenerator = new TimeStampTokenGenerator(infoGenerator, digestCalculator, new ASN1ObjectIdentifier("1.2.3.4"));
        tokenGenerator.addCertificates(new JcaCertStore(chain));
        tokenGenerator.setAccuracySeconds(1);
        tokenGenerator.setResolution(R_MILLISECONDS);
        return new TimeStampResponseGenerator(tokenGenerator, Set.of(SHA1, SHA256, SHA384, SHA512));
    }
}
