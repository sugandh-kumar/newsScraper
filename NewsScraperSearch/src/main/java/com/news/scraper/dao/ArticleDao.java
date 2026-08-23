package com.news.scraper.dao;

import java.util.List;
import java.util.Set;

import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;

public interface ArticleDao {

	List<Article> findArticles(SearchCriteria criteria, long pageNumber);

	Set<Author> findAuthors(SearchCriteria criteria, long pageNumber);
}
