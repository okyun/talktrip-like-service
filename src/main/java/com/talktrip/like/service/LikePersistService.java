package com.talktrip.like.service;

import com.talktrip.like.entity.Like;
import com.talktrip.like.messaging.dto.LikeChangeEventDTO;
import com.talktrip.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikePersistService {

    private static final Logger log = LoggerFactory.getLogger(LikePersistService.class);

    private final LikeRepository likeRepository;

    @Transactional
    public void apply(LikeChangeEventDTO event) {
        if (event.getProductId() == null || event.getMemberId() == null || event.getAction() == null) {
            log.warn("like 이벤트 필수값 누락: {}", event);
            return;
        }
        if (LikeChangeEventDTO.ACTION_ADD.equals(event.getAction())) {
            if (!likeRepository.existsByProductIdAndMemberId(event.getProductId(), event.getMemberId())) {
                likeRepository.save(Like.builder()
                        .productId(event.getProductId())
                        .memberId(event.getMemberId())
                        .build());
                log.debug("like 추가 productId={}, memberId={}", event.getProductId(), event.getMemberId());
            }
        } else if (LikeChangeEventDTO.ACTION_REMOVE.equals(event.getAction())) {
            likeRepository.deleteByProductIdAndMemberId(event.getProductId(), event.getMemberId());
            log.debug("like 삭제 productId={}, memberId={}", event.getProductId(), event.getMemberId());
        } else {
            log.warn("알 수 없는 like action: {}", event.getAction());
        }
    }
}
