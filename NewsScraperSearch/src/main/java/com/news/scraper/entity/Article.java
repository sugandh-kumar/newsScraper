package com.news.scraper.entity;

import lombok.Builder;

@Builder
public record Article(String url, String title, String author, String description) {
}
