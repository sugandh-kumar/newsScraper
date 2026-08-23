package com.news.scraper.service;

import java.util.Map;

/** Defines the application use cases for searching news data. */
public interface NewsScraperService {

  /** Validates article search input and returns matching articles. */
  Map<String, Object> searchArticle(
      String author, String title, String description, Long pageNumber);

  /** Validates author search input and returns unique matching authors. */
  Map<String, Object> searchAuthor(String author, Long pageNumber);
}
