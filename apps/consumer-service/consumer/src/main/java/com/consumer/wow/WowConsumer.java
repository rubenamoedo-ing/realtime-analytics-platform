package com.consumer.wow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WowConsumer {
    private static final Logger log = LoggerFactory.getLogger(WowConsumer.class);

    private final ObjectMapper om = new ObjectMapper();
    private final MeterRegistry registry;
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Counter parseErrors;

    public WowConsumer(MeterRegistry registry) {
        this.registry = registry;
        this.parseErrors = Counter.builder("wow_parse_errors_total").register(registry);
    }

    private Counter counter(String event) {
        return counters.computeIfAbsent(event,
                e -> Counter.builder("wow_events_total")
                        .tag("event", e)
                        .description("Total WoW combat log events by type")
                        .register(registry));
    }

    @KafkaListener(
            topics = "${app.topic:wow.player.metrics.v1}",
            groupId = "${spring.kafka.consumer.group-id:wow-firehose-analytics}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            String msg = record.value();
            Map<String, Object> m = om.readValue(msg, new TypeReference<Map<String,Object>>() {});
            String event = String.valueOf(m.getOrDefault("event", "unknown"));
            counter(event).increment();
            log.debug("nothing");
            log.debug("recv topic={} partition={} offset={} event={}",
                    record.topic(), record.partition(), record.offset(), event);
        } catch (Exception e) {
            parseErrors.increment();
            log.warn("parse error at {}:{}:{} - {}", record.topic(), record.partition(), record.offset(), e.toString());
        }
    }
}
