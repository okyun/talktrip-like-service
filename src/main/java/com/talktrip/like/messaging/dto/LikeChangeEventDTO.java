package com.talktrip.like.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * back_end {@code LikeChangeEventDTO} 와 동일 JSON 필드 (타입 헤더 없음).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikeChangeEventDTO {

    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_REMOVE = "REMOVE";

    private Long productId;
    private Long memberId;
    private String action;
    private String eventId;
    private Instant occurredAt;
}
