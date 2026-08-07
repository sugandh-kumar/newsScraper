package com.news.scraper.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;

@Component
@SuppressWarnings("rawtypes")
public class NewsScraperServiceImpl implements NewsScraperService {

	@Value(value = "${solr.server.articles.url}")
	private String articlesUrl;

	@Autowired
	QueryBuilder queryBuilder;

	private static final Logger logger = LoggerFactory.getLogger(NewsScraperService.class);

	@Override
	public ResponseEntity searchArticle(String author, String title, String description, Long pageNumber) {
		Map<String, Object> responseData = new HashMap<>();
		List<Article> articles = null;
		if (pageNumber == null || pageNumber < 0l) {
			pageNumber = 0l;
		}
		HttpJdkSolrClient solrClient = new HttpJdkSolrClient.Builder(articlesUrl).build();
		SolrQuery query = null;
		if (StringUtils.hasText(author)) {
			author = getSearchString(author);
			query = queryBuilder.buildArticleQuery(String.format(SearchConstants.AUTHOR_QUERY_STRING, author, author),
					pageNumber);
		} else if (StringUtils.hasText(title)) {
			title = getSearchString(title);
			query = queryBuilder.buildArticleQuery(String.format(SearchConstants.TITLE_QUERY_STRING, title, title),
					pageNumber);
		} else if (StringUtils.hasText(description)) {
			description = getSearchString(description);
			query = queryBuilder.buildArticleQuery(
					String.format(SearchConstants.DESCRIPTION_QUERY_STRING, description, description), pageNumber);
		} else {
			throw new IllegalArgumentException(SearchConstants.INVALID_INPUT);
		}
		try {
			QueryResponse response = solrClient.query(query);
			articles = response.getBeans(Article.class);
		} catch (SolrServerException | IOException e) {
			logger.error(e.getMessage());
			throw new IllegalArgumentException(SearchConstants.QUERY_FAILURE);
		}
		responseData.put("articles", articles);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	@Override
	public ResponseEntity searchAuthor(String author, Long pageNumber) {
		Map<String, Object> responseData = new HashMap<>();
		Set<Author> authorsSet = new HashSet<>();
		if (pageNumber == null || pageNumber < 0l) {
			pageNumber = 0l;
		}
		HttpJdkSolrClient solrClient = new HttpJdkSolrClient.Builder(articlesUrl).build();
		SolrQuery query = null;
		if (StringUtils.hasText(author)) {
			author = getSearchString(author);
			query = queryBuilder.buildArticleQuery(String.format(SearchConstants.AUTHOR_QUERY_STRING, author, author),
					pageNumber);
		} else {
			throw new IllegalArgumentException(SearchConstants.INVALID_INPUT);
		}
		try {
			QueryResponse response = solrClient.query(query);
			List<Author> authors = response.getBeans(Author.class);
			Map<String, Integer> authorMap = new HashMap<>();
			for (Author authorObject : authors) {
				if (!authorMap.containsKey(authorObject.getAuthor())) {
					authorMap.put(authorObject.getAuthor(), 1);
					authorsSet.add(authorObject);
				}
			}
		} catch (SolrServerException | IOException e) {
			logger.error(e.getMessage());
			throw new IllegalArgumentException(SearchConstants.QUERY_FAILURE);
		}
		responseData.put("authors", authorsSet);
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}

	private String getSearchString(String input) {
		String replacedString = input.replaceAll(SearchConstants.PATTERN_STRING, "");
		return (replacedString.isEmpty() ? input : replacedString);
	}

}
