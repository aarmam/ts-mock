package com.nortal.ts.mock;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.spi.DSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Slf4j
@SpringBootTest
class ApplicationTests {
    @Autowired
    private TimeStampResponseGenerator timeStampResponseGenerator;
    @Autowired
    private X509Certificate tsCertificate;

    @Test
    void timestamp() throws TSPException, IOException, OperatorCreationException {
        byte[] digest = DSSUtils.digest(DigestAlgorithm.SHA256, "Test".getBytes());
        TimeStampRequestGenerator requestGenerator = new TimeStampRequestGenerator();
        requestGenerator.setCertReq(true);
        TimeStampRequest request = requestGenerator.generate(new ASN1ObjectIdentifier(DigestAlgorithm.SHA256.getOid()), digest);
        BigInteger serialNumber = new BigInteger(128, new SecureRandom());

        TimeStampResponse response = timeStampResponseGenerator.generate(request, serialNumber, new Date());

        TimeStampResponse timeStampResponse = new TimeStampResponse(response.getEncoded());
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        assertArrayEquals(digest, timeStampToken.getTimeStampInfo().getMessageImprintDigest());
        timeStampToken.validate(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(tsCertificate));
    }
}
