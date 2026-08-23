package com.news.scraper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.dao.ArticleDao;
import com.news.scraper.dao.SearchCriteria;
import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsScraperServiceImplTest {

  @Mock private ArticleDao articleDao;

  @Test
  void searchArticleUsesFirstProvidedCriteriaAndNormalizesPage() {
    List<Article> articles = List.of(Article.builder().title("Java").build());
    when(articleDao.findArticles(org.mockito.ArgumentMatchers.any(), eq(0L))).thenReturn(articles);
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    Map<String, Object> response = service.searchArticle("Ada", "ignored", null, -2L);

    assertThat(response).containsEntry("articles", articles);
    ArgumentCaptor<SearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
    verify(articleDao).findArticles(criteriaCaptor.capture(), eq(0L));
    assertThat(criteriaCaptor.getValue().field()).isEqualTo("author");
    assertThat(criteriaCaptor.getValue().value()).isEqualTo("Ada");
  }

  @Test
  void searchAuthorDelegatesToDao() {
    Set<Author> authors = Set.of(Author.builder().author("Ada").build());
    when(articleDao.findAuthors(org.mockito.ArgumentMatchers.any(), eq(3L))).thenReturn(authors);
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    Map<String, Object> response = service.searchAuthor("Ada", 3L);

    assertThat(response).containsEntry("authors", authors);
    verify(articleDao)
        .findAuthors(SearchCriteria.builder().field("author").value("Ada").build(), 3L);
  }

  @Test
  void searchArticleUsesTitleCriteriaWhenAuthorIsMissing() {
    when(articleDao.findArticles(org.mockito.ArgumentMatchers.any(), eq(0L))).thenReturn(List.of());
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    service.searchArticle(null, "Java", null, null);

    verify(articleDao)
        .findArticles(SearchCriteria.builder().field("title").value("Java").build(), 0L);
  }

  @Test
  void searchArticleUsesDescriptionCriteriaWhenOtherFieldsAreMissing() {
    when(articleDao.findArticles(org.mockito.ArgumentMatchers.any(), eq(0L))).thenReturn(List.of());
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    service.searchArticle(null, null, "JVM", null);

    verify(articleDao)
        .findArticles(SearchCriteria.builder().field("description").value("JVM").build(), 0L);
  }

  @Test
  void searchArticleRejectsMissingCriteria() {
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    assertThatThrownBy(() -> service.searchArticle(" ", null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(SearchConstants.INVALID_INPUT);
  }

  @Test
  void searchAuthorRejectsBlankAuthor() {
    NewsScraperService service = new NewsScraperServiceImpl(articleDao);

    assertThatThrownBy(() -> service.searchAuthor(" ", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(SearchConstants.INVALID_INPUT);
  }
}
