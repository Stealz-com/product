package com.ecommerce.product.service;

import com.ecommerce.product.dto.BargainRequest;
import com.ecommerce.product.dto.BargainResponse;
import com.ecommerce.product.entity.BargainHistory;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.BargainHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentBrain {

    private final BargainHistoryRepository historyRepository;
    private final BargainingModel bargainingModel;

    public BargainResponse processRequest(BargainRequest request, Product product) {
        String msg = request.getMessage() != null ? request.getMessage().toLowerCase() : "";
        BigDecimal proposedPrice = request.getProposedPrice();

        // 1. Intent Detection (Primitive NLP)
        if (isGreeting(msg)) {
            return buildResponse(false, null, AgentConstants.getRandom(AgentConstants.GREETINGS));
        }

        if (isCompliment(msg)) {
            return buildResponse(false, null, AgentConstants.getRandom(AgentConstants.COMPLIMENTS_RESPONSE));
        }

        if (proposedPrice == null || proposedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            if (isPriceComplaint(msg)) {
                return buildResponse(false, null, AgentConstants.getRandom(AgentConstants.PRICE_HIGH_RESPONSE));
            }
            return buildResponse(false, null,
                    "I'm listening! What price are you thinking for this " + product.getName() + "?");
        }

        // 2. Prediction using Neural Network
        BigDecimal currentPrice = product.getPrice();
        BigDecimal minPrice = product.getMinPrice();
        if (minPrice == null)
            minPrice = currentPrice.multiply(new BigDecimal("0.8"));

        LocalDateTime created = product.getCreatedAt();
        if (created == null)
            created = LocalDateTime.now().minusDays(10);
        long daysOnSite = ChronoUnit.DAYS.between(created, LocalDateTime.now());

        double[] inputFeatures = {
                product.getPrice().doubleValue() / 1000.0, // Scale for model
                minPrice.doubleValue() / 1000.0,
                proposedPrice.doubleValue() / 1000.0,
                (double) daysOnSite / 30.0,
                (double) msg.length() / 100.0
        };

        double acceptedProbability = bargainingModel.predict(inputFeatures);
        log.info("Model prediction for acceptance: {}", acceptedProbability);

        boolean accepted = acceptedProbability > 0.6; // Threshold for acceptance

        // Bootstrap: If model is new, fallback to rules but still use prediction as a
        // "vote"
        if (daysOnSite > 7 && proposedPrice.compareTo(minPrice) >= 0) {
            accepted = true;
        }

        BargainResponse response;
        if (accepted) {
            response = buildResponse(true, proposedPrice,
                    AgentConstants.getRandom(AgentConstants.ACCEPT_OFFER).replace("{price}", proposedPrice.toString()));
        } else {
            BigDecimal counter = currentPrice.add(proposedPrice).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            if (proposedPrice.compareTo(minPrice) < 0) {
                counter = minPrice.add(minPrice.multiply(new BigDecimal("0.05"))).setScale(2, RoundingMode.HALF_UP);
                response = buildResponse(false, counter, AgentConstants.getRandom(AgentConstants.REJECT_LOW_OFFER)
                        .replace("{price}", counter.toString()));
            } else {
                response = buildResponse(false, counter,
                        AgentConstants.getRandom(AgentConstants.COUNTER_OFFER).replace("{price}", counter.toString()));
            }
        }

        // 3. Log to History for Training
        historyRepository.save(BargainHistory.builder()
                .productId(product.getId())
                .userMessage(msg)
                .agentMessage(response.getResponseMessage())
                .proposedPrice(proposedPrice)
                .productPrice(currentPrice)
                .productMinPrice(minPrice)
                .accepted(response.isAccepted())
                .build());

        return response;
    }

    public String trainAgent() {
        log.info("Starting agent training process...");
        List<BargainHistory> history = historyRepository.findAll();
        if (history.size() < 5)
            return "Not enough data to train. Need at least 5 records.";

        double[][] inputs = new double[history.size()][5];
        double[][] labels = new double[history.size()][1];

        for (int i = 0; i < history.size(); i++) {
            BargainHistory h = history.get(i);
            inputs[i][0] = h.getProductPrice().doubleValue() / 1000.0;
            inputs[i][1] = h.getProductMinPrice().doubleValue() / 1000.0;
            inputs[i][2] = h.getProposedPrice().doubleValue() / 1000.0;
            inputs[i][3] = 10.0 / 30.0; // Approximation for historical age
            inputs[i][4] = (double) h.getUserMessage().length() / 100.0;

            labels[i][0] = h.isAccepted() ? 1.0 : 0.0;
        }

        bargainingModel.train(inputs, labels);
        return "Training complete! Agent refined with " + history.size() + " records.";
    }

    private boolean isGreeting(String msg) {
        return Pattern.compile("\\b(hi|hello|hey|greetings)\\b").matcher(msg).find();
    }

    private boolean isCompliment(String msg) {
        return Pattern.compile("\\b(good|nice|great|love|amazing|beautiful|cool)\\b").matcher(msg).find();
    }

    private boolean isPriceComplaint(String msg) {
        return Pattern.compile("\\b(expensive|high|costly|pricey)\\b").matcher(msg).find();
    }

    private BargainResponse buildResponse(boolean accepted, BigDecimal price, String message) {
        return BargainResponse.builder()
                .accepted(accepted)
                .counterOffer(price)
                .responseMessage(message)
                .build();
    }
}
