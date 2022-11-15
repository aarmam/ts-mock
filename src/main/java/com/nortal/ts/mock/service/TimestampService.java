package com.nortal.ts.mock.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimestampService {
    private static final SecureRandom random = new SecureRandom();
    private final TimeStampResponseGenerator timeStampResponseGenerator;

    @SneakyThrows
    public byte[] timestamp(byte[] timestampRequest) {
        TimeStampRequest timeStampRequest = new TimeStampRequest(timestampRequest);
        BigInteger serialNumber = new BigInteger(128, random);
        Date timestampTime = Date.from(Instant.now().minusMillis(1000));
        return timeStampResponseGenerator.generate(timeStampRequest, serialNumber, timestampTime).getEncoded();
    }
}
