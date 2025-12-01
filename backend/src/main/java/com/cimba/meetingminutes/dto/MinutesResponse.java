package com.cimba.meetingminutes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinutesResponse {
    private Long id;
    private String title;
    private String summary;
    private String keyDecisions;
    private String actionItems;
    private String createdAt;
}
