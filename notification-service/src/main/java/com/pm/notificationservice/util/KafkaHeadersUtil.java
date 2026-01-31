package com.pm.notificationservice.util;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class KafkaHeadersUtil {

    private KafkaHeadersUtil() {}

    public static Map<String, String> extractHeaders(Headers headers) {
        Map<String, String> map = new HashMap<>();
        for (Header header : headers) {
            map.put(
                    header.key(),
                    header.value() == null
                            ? null
                            : new String(header.value(), StandardCharsets.UTF_8)
            );
        }
        return map;
    }

    public static String getHeader(
            Headers headers,
            String key
    ) {
        Header header = headers.lastHeader(key);
        return header == null
                ? null
                : new String(header.value(), StandardCharsets.UTF_8);
    }
}
