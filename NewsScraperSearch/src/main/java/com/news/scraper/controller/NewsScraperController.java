package com.news.scraper.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.news.scraper.service.NewsScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/")
@RequiredArgsConstructor
@Slf4j
public class NewsScraperController {

	private final NewsScraperService newsScraperService;

	@GetMapping(value = { "/article/search" })
	public ResponseEntity<Map<String, Object>> searchArticle(@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "pageNumber", required = false) Long pageNumber) {
		log.info("Article search request received; pageNumber={}", pageNumber);
		Map<String, Object> response = newsScraperService.searchArticle(author, title, description, pageNumber);
		log.info("Article search request completed");
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = { "/author/search" })
	public ResponseEntity<Map<String, Object>> searchAuthor(@RequestParam String author,
			@RequestParam(value = "pageNumber", required = false) Long pageNumber) {
		log.info("Author search request received; pageNumber={}", pageNumber);
		Map<String, Object> response = newsScraperService.searchAuthor(author, pageNumber);
		log.info("Author search request completed");
		return ResponseEntity.ok(response);
	}

}
