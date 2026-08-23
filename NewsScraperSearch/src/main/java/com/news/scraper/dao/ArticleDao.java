package com.news.scraper.dao;

import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;
import java.util.List;
import java.util.Set;

/** Provides persistence operations for article search data. */
public interface ArticleDao {

  /** Finds articles matching the supplied criteria and page. */
  List<Article> findArticles(SearchCriteria criteria, long pageNumber);

  /** Finds unique authors matching the supplied criteria and page. */
  Set<Author> findAuthors(SearchCriteria criteria, long pageNumber);
}
