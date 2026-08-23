package com.news.scraper.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.service.NewsScraperService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class NewsScraperControllerTest {

  @Mock private NewsScraperService newsScraperService;

  @Test
  void searchArticleReturnsOkResponseFromService() {
    Map<String, Object> serviceResponse = Map.of("articles", "results");
    when(newsScraperService.searchArticle("author", null, null, 2L)).thenReturn(serviceResponse);
    NewsScraperController controller = new NewsScraperController(newsScraperService);

    var response = controller.searchArticle("author", null, null, 2L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(serviceResponse);
    verify(newsScraperService).searchArticle("author", null, null, 2L);
  }

  @Test
  void searchAuthorReturnsOkResponseFromService() {
    Map<String, Object> serviceResponse = Map.of("authors", "results");
    when(newsScraperService.searchAuthor("Ada", 1L)).thenReturn(serviceResponse);
    NewsScraperController controller = new NewsScraperController(newsScraperService);

    var response = controller.searchAuthor("Ada", 1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(serviceResponse);
    verify(newsScraperService).searchAuthor("Ada", 1L);
  }

  @Test
  void searchArticlePropagatesServiceException() {
    when(newsScraperService.searchArticle(null, null, null, null))
        .thenThrow(new IllegalArgumentException(SearchConstants.INVALID_INPUT));
    NewsScraperController controller = new NewsScraperController(newsScraperService);

    assertThatThrownBy(() -> controller.searchArticle(null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(SearchConstants.INVALID_INPUT);
  }
}
