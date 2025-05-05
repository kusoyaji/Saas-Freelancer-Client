package com.freelancer.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for participant IDs in a conversation request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequestDto {
    private List<Long> participantIds;
}