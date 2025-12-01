package com.cimba.meetingminutes.dto;

import lombok.Data;

@Data
public class TranscriptRequest {
    private String title;
    private String transcript;
}
