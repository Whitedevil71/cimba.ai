package com.cimba.meetingminutes.service;

import com.cimba.meetingminutes.dto.MinutesResponse;
import com.cimba.meetingminutes.dto.TranscriptRequest;
import com.cimba.meetingminutes.model.MeetingMinutes;
import com.cimba.meetingminutes.repository.MeetingMinutesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMinutesService {
    
    private final MeetingMinutesRepository repository;
    private final OpenAIService openAIService;
    
    public MinutesResponse processTranscript(TranscriptRequest request) {
        Map<String, String> minutes = openAIService.generateMinutes(request.getTranscript());
        
        MeetingMinutes entity = new MeetingMinutes();
        entity.setTitle(request.getTitle());
        entity.setTranscript(request.getTranscript());
        entity.setSummary(minutes.get("summary"));
        entity.setKeyDecisions(minutes.get("keyDecisions"));
        entity.setActionItems(minutes.get("actionItems"));
        
        entity = repository.save(entity);
        
        return toResponse(entity);
    }
    
    public MinutesResponse processAudio(String title, MultipartFile audioFile) {
        String transcript = openAIService.transcribeAudio(audioFile);
        
        TranscriptRequest request = new TranscriptRequest();
        request.setTitle(title);
        request.setTranscript(transcript);
        
        return processTranscript(request);
    }
    
    public List<MinutesResponse> getAllMinutes() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public MinutesResponse getMinutesById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Minutes not found"));
    }
    
    private MinutesResponse toResponse(MeetingMinutes entity) {
        return new MinutesResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getKeyDecisions(),
                entity.getActionItems(),
                entity.getCreatedAt().toString()
        );
    }
}
