package com.news.scraper.service;

import java.util.Map;

public interface NewsScraperService {
	
	Map<String, Object> searchArticle(String author, String title, String description, Long pageNumber);
	
	Map<String, Object> searchAuthor(String author, Long pageNumber);

}
