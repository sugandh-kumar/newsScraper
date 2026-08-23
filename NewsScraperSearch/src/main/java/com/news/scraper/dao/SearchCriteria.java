package com.news.scraper.dao;

import lombok.Builder;

@Builder
public record SearchCriteria(String field, String value) {}
