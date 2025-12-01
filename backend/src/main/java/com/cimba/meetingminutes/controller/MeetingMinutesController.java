package com.cimba.meetingminutes.controller;

import com.cimba.meetingminutes.dto.MinutesResponse;
import com.cimba.meetingminutes.dto.TranscriptRequest;
import com.cimba.meetingminutes.service.MeetingMinutesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/minutes")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class MeetingMinutesController {
    
    private final MeetingMinutesService service;
    
    @PostMapping("/transcript")
    public ResponseEntity<MinutesResponse> processTranscript(@RequestBody TranscriptRequest request) {
        return ResponseEntity.ok(service.processTranscript(request));
    }
    
    @PostMapping("/audio")
    public ResponseEntity<MinutesResponse> processAudio(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.processAudio(title, file));
    }
    
    @GetMapping
    public ResponseEntity<List<MinutesResponse>> getAllMinutes() {
        return ResponseEntity.ok(service.getAllMinutes());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MinutesResponse> getMinutesById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMinutesById(id));
    }
}
