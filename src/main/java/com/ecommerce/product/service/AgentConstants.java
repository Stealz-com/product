package com.ecommerce.product.service;

import java.util.List;
import java.util.Random;

public class AgentConstants {
    private static final Random random = new Random();

    public static final List<String> GREETINGS = List.of(
            "Hello! I'm your digital shopping assistant. How can I help you with this item?",
            "Hi there! Glad you're interested in this product. It's one of my favorites!",
            "Greetings! I'm here to help you get the best deal. What's on your mind?");

    public static final List<String> COMPLIMENTS_RESPONSE = List.of(
            "I totally agree! It's a high-quality item. Would you like to make it yours?",
            "Thank you! We take pride in our selection. Ready to talk about the price?",
            "Glad you like it! It's even better in person.");

    public static final List<String> PRICE_HIGH_RESPONSE = List.of(
            "I understand quality comes at a price, but I might have some wiggle room since it's been in stock for a bit.",
            "I hear you. Value is important. What price feels more reasonable to you?",
            "It is a premium item, but I'm authorized to negotiate. Make me an offer!");

    public static final List<String> REJECT_LOW_OFFER = List.of(
            "Ouch! That's a bit too low for such a great item. Can you come up a bit?",
            "I'd love to help, but I can't go that low without getting in trouble with my boss! How about a slightly better offer?",
            "That's below our cost! I'm authorized to give a discount, but not that much. What about ₹{price}?");

    public static final List<String> ACCEPT_OFFER = List.of(
            "Deal! I like your style. I've updated the price to ₹{price} for you.",
            "You drive a hard bargain! I accept your offer of ₹{price}.",
            "Alright, you've convinced me. ₹{price} it is! Adding it to your session.");

    public static final List<String> COUNTER_OFFER = List.of(
            "I can't do that, but I can meet you in the middle at ₹{price}. How does that sound?",
            "That's a bit low, but I want to see you happy. Can we settle on ₹{price}?",
            "How about we split the difference? My best counter is ₹{price}.");

    public static String getRandom(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }
}
