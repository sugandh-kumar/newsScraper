package com.news.scraper.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.dao.ArticleDao;
import com.news.scraper.dao.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsScraperServiceImpl implements NewsScraperService {

	private final ArticleDao articleDao;

	@Override
	public Map<String, Object> searchArticle(String author, String title, String description,
			Long pageNumber) {
		long page = pageNumber == null ? 0L : Math.max(0L, pageNumber);
		SearchCriteria criteria;
		if (StringUtils.hasText(author)) {
			criteria = SearchCriteria.builder().field("author").value(author).build();
		} else if (StringUtils.hasText(title)) {
			criteria = SearchCriteria.builder().field("title").value(title).build();
		} else if (StringUtils.hasText(description)) {
			criteria = SearchCriteria.builder().field("description").value(description).build();
		} else {
			log.warn("Article search rejected because no search field was provided");
			throw new IllegalArgumentException(SearchConstants.INVALID_INPUT);
		}
		log.debug("Searching articles by field={} and page={}", criteria.field(), page);
		return Map.of("articles", articleDao.findArticles(criteria, page));
	}

	@Override
	public Map<String, Object> searchAuthor(String author, Long pageNumber) {
		long page = pageNumber == null ? 0L : Math.max(0L, pageNumber);
		if (!StringUtils.hasText(author)) {
			log.warn("Author search rejected because author was blank");
			throw new IllegalArgumentException(SearchConstants.INVALID_INPUT);
		}
		log.debug("Searching authors by field=author and page={}", page);
		return Map.of("authors", articleDao.findAuthors(
				SearchCriteria.builder().field("author").value(author).build(), page));
	}

}
