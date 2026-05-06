package com.talktrip.like.service;

import com.talktrip.like.entity.Like;
import com.talktrip.like.messaging.dto.LikeChangeEventDTO;
import com.talktrip.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikePersistService {

    private static final Logger log = LoggerFactory.getLogger(LikePersistService.class);
    private static final int BATCH_SIZE = 5;

    private final LikeRepository likeRepository;

    private final List<LikeChangeEventDTO> buffer = new ArrayList<>();

    /**
     * Kafka에서 들어온 이벤트를 버퍼에 쌓고, 5개(BATCH_SIZE) 이상일 때만 DB에 반영합니다.
     * (요구사항: \"5개가 큐에 차야만 insert\") → DB 반영 자체를 5개 단위로 지연.
     */
    public void enqueue(List<LikeChangeEventDTO> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        List<List<LikeChangeEventDTO>> toFlush = null;
        synchronized (buffer) {
            buffer.addAll(events);
            int flushCount = buffer.size() / BATCH_SIZE;
            if (flushCount <= 0) {
                return;
            }
            toFlush = new ArrayList<>(flushCount);
            for (int i = 0; i < flushCount; i++) {
                List<LikeChangeEventDTO> batch = new ArrayList<>(BATCH_SIZE);
                for (int j = 0; j < BATCH_SIZE; j++) {
                    batch.add(buffer.remove(0));
                }
                toFlush.add(batch);
            }
        }
        for (List<LikeChangeEventDTO> batch : toFlush) {
            applyBatch(batch);
        }
    }

    @Transactional
    protected void applyBatch(List<LikeChangeEventDTO> batch) {
        for (LikeChangeEventDTO event : batch) {
            applyOne(event);
        }
    }

    @SuppressWarnings("null")
    private void applyOne(LikeChangeEventDTO event) {
        if (event == null || event.getProductId() == null || event.getMemberId() == null || event.getAction() == null) {
            log.warn("like 이벤트 필수값 누락: {}", event);
            return;
        }
        if (LikeChangeEventDTO.ACTION_ADD.equals(event.getAction())) {
            if (!likeRepository.existsByProductIdAndMemberId(event.getProductId(), event.getMemberId())) {
                Like like = Like.builder()
                        .productId(event.getProductId())
                        .memberId(event.getMemberId())
                        .build();
                likeRepository.save(like);
                log.debug("like 추가 productId={}, memberId={}", event.getProductId(), event.getMemberId());
            }
            return;
        }
        if (LikeChangeEventDTO.ACTION_REMOVE.equals(event.getAction())) {
            likeRepository.deleteByProductIdAndMemberId(event.getProductId(), event.getMemberId());
            log.debug("like 삭제 productId={}, memberId={}", event.getProductId(), event.getMemberId());
            return;
        }
        log.warn("알 수 없는 like action: {}", event.getAction());
    }
}
