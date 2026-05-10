package com.gigu.marketplace.application.dto;
public record SearchQuery(String category, Double priceMin, Double priceMax, Double minRating, String q, int page, int pageSize) {}
