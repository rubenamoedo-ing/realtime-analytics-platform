package com.producer.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public EventController(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<?> send(@RequestBody @Valid EventV1 body) throws JsonProcessingException {
        body.ensureDefaults();                         // fill messageId/sentAt if missing
        String key = body.getUserId();                 // partition by user
        String json = mapper.writeValueAsString(body); // serialize to JSON
        kafka.send("events", key, json);
        return ResponseEntity.ok(body.getMessageId()); // return the id so clients can track
    }
}
