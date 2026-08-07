package com.news.scraper.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.news.scraper.service.NewsScraperService;

@SuppressWarnings("unchecked")
@RestController
@RequestMapping(value = "/")
public class NewsScraperController {

	@Autowired
	NewsScraperService newsScraperService;

	@GetMapping(value = { "/article/search" })
	public ResponseEntity<Object> searchArticle(@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "pageNumber", required = false) Long pageNumber, HttpServletRequest httpRequest) {
		return newsScraperService.searchArticle(author, title, description, pageNumber);
	}
	
	@GetMapping(value = { "/author/search" })
	public ResponseEntity<Object> searchAuthor(@RequestParam(value = "author", required = true) String author,
			@RequestParam(value = "pageNumber", required = false) Long pageNumber, HttpServletRequest httpRequest) {
		return newsScraperService.searchAuthor(author, pageNumber);
	}

}
