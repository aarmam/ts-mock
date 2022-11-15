package com.nortal.ts.mock.controller;

import com.nortal.ts.mock.service.TimestampService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
class TimestampController {
    private static final String TIMESTAMP_REQUEST_PATH = "/ts";
    private static final MediaType TIMESTAMP_RESPONSE_MEDIA_TYPE = new MediaType("application", "timestamp-reply");

    private final TimestampService timestampService;

    @PostMapping(TIMESTAMP_REQUEST_PATH)
    ResponseEntity<byte[]> timestamp(@RequestBody byte[] timestampRequest) {
        return ResponseEntity
                .ok()
                .contentType(TIMESTAMP_RESPONSE_MEDIA_TYPE)
                .body(timestampService.timestamp(timestampRequest));
    }
}
