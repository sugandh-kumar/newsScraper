package com.news.scraper.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.news.scraper.constants.SearchConstants;
import com.news.scraper.entity.Article;
import com.news.scraper.entity.Author;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.Test;

class SolrArticleDaoTest {

  @Test
  void findArticlesBuildsQueryAndMapsDocumentsToRecords() throws Exception {
    SolrClient solrClient = mock(SolrClient.class);
    QueryResponse response =
        queryResponse(articleDocument("https://example.com", "Java", "Ada", "News"));
    when(solrClient.query(any(SolrQuery.class))).thenReturn(response);
    SolrArticleDao dao = new SolrArticleDao(solrClient);

    List<Article> articles =
        dao.findArticles(SearchCriteria.builder().field("title").value("Java!").build(), 2L);

    assertThat(articles)
        .containsExactly(
            Article.builder()
                .url("https://example.com")
                .title("Java")
                .author("Ada")
                .description("News")
                .build());
    var queryCaptor = org.mockito.ArgumentCaptor.forClass(SolrQuery.class);
    verify(solrClient).query(queryCaptor.capture());
    assertThat(queryCaptor.getValue().get("q")).isEqualTo("title:(/.*Java.*/ OR Java)");
    assertThat(queryCaptor.getValue().get("rows")).isEqualTo(SearchConstants.MAX_RESULTS);
    assertThat(queryCaptor.getValue().get("start")).isEqualTo("60");
  }

  @Test
  void findAuthorsRemovesDuplicatesAndPreservesOrder() throws Exception {
    SolrClient solrClient = mock(SolrClient.class);
    QueryResponse response =
        queryResponse(authorDocument("Ada"), authorDocument("Grace"), authorDocument("Ada"));
    when(solrClient.query(any(SolrQuery.class))).thenReturn(response);
    SolrArticleDao dao = new SolrArticleDao(solrClient);

    Set<Author> authors =
        dao.findAuthors(SearchCriteria.builder().field("author").value("engineer").build(), 0L);

    assertThat(authors)
        .containsExactly(
            Author.builder().author("Ada").build(), Author.builder().author("Grace").build());
  }

  @Test
  void findArticlesSupportsDescriptionSearchAndMissingFields() throws Exception {
    SolrClient solrClient = mock(SolrClient.class);
    QueryResponse response = queryResponse(new SolrDocument());
    when(solrClient.query(any(SolrQuery.class))).thenReturn(response);
    SolrArticleDao dao = new SolrArticleDao(solrClient);

    assertThat(
            dao.findArticles(
                SearchCriteria.builder().field("description").value("JVM").build(), 0L))
        .containsExactly(Article.builder().build());
  }

  @Test
  void solrFailureIsTranslatedToIllegalStateException() throws Exception {
    SolrClient solrClient = mock(SolrClient.class);
    doThrow(new IOException("connection failed")).when(solrClient).query(any(SolrQuery.class));
    SolrArticleDao dao = new SolrArticleDao(solrClient);

    assertThatThrownBy(
            () ->
                dao.findArticles(SearchCriteria.builder().field("author").value("Ada").build(), 0L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(SearchConstants.QUERY_FAILURE)
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  void unsupportedSearchFieldIsRejected() {
    SolrClient solrClient = mock(SolrClient.class);
    SolrArticleDao dao = new SolrArticleDao(solrClient);

    assertThatThrownBy(
            () ->
                dao.findArticles(
                    SearchCriteria.builder().field("unknown").value("value").build(), 0L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported search field: unknown");
  }

  private QueryResponse queryResponse(SolrDocument... documents) {
    SolrDocumentList results = new SolrDocumentList();
    results.addAll(List.of(documents));
    QueryResponse response = mock(QueryResponse.class);
    when(response.getResults()).thenReturn(results);
    return response;
  }

  private SolrDocument articleDocument(
      String url, String title, String author, String description) {
    SolrDocument document = new SolrDocument();
    document.setField("url", url);
    document.setField("title", title);
    document.setField("author", author);
    document.setField("description", description);
    return document;
  }

  private SolrDocument authorDocument(String author) {
    SolrDocument document = new SolrDocument();
    document.setField("author", author);
    return document;
  }
}
