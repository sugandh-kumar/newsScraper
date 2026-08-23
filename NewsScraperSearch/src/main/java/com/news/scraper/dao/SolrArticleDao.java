package com.news.scraper.dao;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.stereotype.Repository;

/** Implements article persistence operations using Apache Solr. */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SolrArticleDao implements ArticleDao {

  private final SolrClient solrClient;

  /** Executes an article query and maps Solr documents to immutable article records. */
  @Override
  public List<Article> findArticles(SearchCriteria criteria, long pageNumber) {
    List<Article> articles =
        query(criteria, pageNumber).getResults().stream().map(this::toArticle).toList();
    log.debug("Solr article query returned {} results", articles.size());
    return articles;
  }

  /** Executes an author query, maps the results, and removes duplicate author names. */
  @Override
  public Set<Author> findAuthors(SearchCriteria criteria, long pageNumber) {
    Map<String, Author> authorsByName = new LinkedHashMap<>();
    query(criteria, pageNumber).getResults().stream()
        .map(this::toAuthor)
        .forEach(author -> authorsByName.putIfAbsent(author.author(), author));
    Set<Author> authors = new LinkedHashSet<>(authorsByName.values());
    log.debug("Solr author query returned {} unique results", authors.size());
    return authors;
  }

  private QueryResponse query(SearchCriteria criteria, long pageNumber) {
    log.debug("Executing Solr query; field={}, page={}", criteria.field(), pageNumber);
    SolrQuery query = new SolrQuery();
    query.set("q", buildQueryString(criteria));
    query.set("rows", SearchConstants.MAX_RESULTS);
    query.set("sort", "score desc");
    query.set("start", Long.toString(pageNumber * Long.parseLong(SearchConstants.MAX_RESULTS)));
    try {
      return solrClient.query(query);
    } catch (SolrServerException | IOException exception) {
      log.error("Solr query failed; field={}, page={}", criteria.field(), pageNumber, exception);
      throw new IllegalStateException(SearchConstants.QUERY_FAILURE, exception);
    }
  }

  private String buildQueryString(SearchCriteria criteria) {
    String value = sanitize(criteria.value());
    return switch (criteria.field()) {
      case "author" -> String.format(SearchConstants.AUTHOR_QUERY_STRING, value, value);
      case "title" -> String.format(SearchConstants.TITLE_QUERY_STRING, value, value);
      case "description" -> String.format(SearchConstants.DESCRIPTION_QUERY_STRING, value, value);
      default ->
          throw new IllegalArgumentException("Unsupported search field: " + criteria.field());
    };
  }

  private String sanitize(String value) {
    String sanitized = value.replaceAll(SearchConstants.PATTERN_STRING, "");
    return sanitized.isEmpty() ? value : sanitized;
  }

  private Article toArticle(SolrDocument document) {
    return Article.builder()
        .url(fieldAsString(document, "url"))
        .title(fieldAsString(document, "title"))
        .author(fieldAsString(document, "author"))
        .description(fieldAsString(document, "description"))
        .build();
  }

  private Author toAuthor(SolrDocument document) {
    return Author.builder().author(fieldAsString(document, "author")).build();
  }

  private String fieldAsString(SolrDocument document, String fieldName) {
    Object value = document.getFieldValue(fieldName);
    return value == null ? null : value.toString();
  }
}
