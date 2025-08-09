package com.producer.web;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final KafkaTemplate<String, String> kafka;

    public EventController(KafkaTemplate<String, String> kafka) { this.kafka = kafka; }

    @PostMapping
    public ResponseEntity<String> send(@RequestBody String payload) {
        kafka.send("events", payload);
        return ResponseEntity.ok("sent");
    }
}
