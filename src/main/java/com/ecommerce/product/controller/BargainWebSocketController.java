package com.ecommerce.product.controller;

import com.ecommerce.product.client.BargainServiceClient;
import com.ecommerce.product.dto.BargainRequest;
import com.ecommerce.product.dto.BargainResponse;
import com.ecommerce.product.entity.BargainMessage;
import com.ecommerce.product.entity.BargainSession;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.BargainMessageRepository;
import com.ecommerce.product.repository.BargainSessionRepository;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BargainWebSocketController {

    private final BargainSessionRepository sessionRepository;
    private final BargainMessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final BargainServiceClient bargainServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/bargain/{productId}/{userId}")
    public void handleBargainMessage(@DestinationVariable Long productId,
            @DestinationVariable String userId,
            BargainRequest clientRequest) {
        try {
            log.info("WebSocket bargain request from {}: {}", userId, clientRequest);

            // 1. Get or Create Session
            BargainSession session = sessionRepository.findByProductIdAndUserIdAndActiveTrue(productId, userId)
                    .orElseGet(() -> sessionRepository.save(BargainSession.builder()
                            .productId(productId)
                            .userId(userId)
                            .build()));

            // 2. Save User Message
            BargainMessage userMsg = BargainMessage.builder()
                    .sessionId(session.getId())
                    .sender("USER")
                    .message(clientRequest.getMessage())
                    .proposedPrice(clientRequest.getProposedPrice())
                    .build();
            messageRepository.save(userMsg);

            // 3. Prepare AI Request with History
            Product product = productRepository.findById(productId).orElseThrow();
            BigDecimal minPrice = product.getMinPrice();
            if (minPrice == null)
                minPrice = product.getPrice().multiply(new BigDecimal("0.85"));

            List<BargainRequest.BargainMessageDTO> history = messageRepository
                    .findBySessionIdOrderByTimestampAsc(session.getId())
                    .stream()
                    .map(m -> BargainRequest.BargainMessageDTO.builder()
                            .sender(m.getSender())
                            .message(m.getMessage())
                            .proposedPrice(m.getProposedPrice())
                            .build())
                    .collect(Collectors.toList());

            BargainRequest aiRequest = BargainRequest.builder()
                    .productId(productId)
                    .currentPrice(product.getPrice())
                    .minPrice(minPrice)
                    .proposedPrice(clientRequest.getProposedPrice())
                    .message(clientRequest.getMessage())
                    .sessionId(session.getId().toString())
                    .history(history)
                    .build();

            // 4. Call AI
            BargainResponse aiResponse = bargainServiceClient.bargain(aiRequest);

            // 5. Save AI Response
            BargainMessage aiMsg = BargainMessage.builder()
                    .sessionId(session.getId())
                    .sender("AI")
                    .message(aiResponse.getResponseMessage())
                    .proposedPrice(aiResponse.getCounterOffer())
                    .build();
            messageRepository.save(aiMsg);

            // 6. Broadcast back to user
            messagingTemplate.convertAndSend("/topic/bargain/" + userId, aiResponse);

        } catch (Exception e) {
            log.error("Error in WebSocket bargaining", e);
            messagingTemplate.convertAndSend("/topic/bargain/" + userId, BargainResponse.builder()
                    .accepted(false)
                    .responseMessage("My AI brain is a bit overwhelmed. Could you say that again?")
                    .build());
        }
    }
}
