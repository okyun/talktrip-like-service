package com.talktrip.like.messaging.consumer;

import com.talktrip.like.messaging.dto.LikeChangeEventDTO;
import com.talktrip.like.service.LikePersistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeChangeKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(LikeChangeKafkaConsumer.class);

    private final LikePersistService likePersistService;

    @KafkaListener(
            topics = "${kafka.topics.like-change:like-change}",
            containerFactory = "likeChangeKafkaListenerContainerFactory"
    )
    public void onLikeChange(@Payload LikeChangeEventDTO event) {
        try {
            likePersistService.apply(event);
        } catch (Exception e) {
            log.error("like 이벤트 처리 실패 event={}", event, e);
            throw e;
        }
    }
}
