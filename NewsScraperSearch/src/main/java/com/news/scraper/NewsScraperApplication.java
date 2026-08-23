package com.news.scraper;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NewsScraperApplication {

  public static void main(String[] args) {
    SpringApplication.run(NewsScraperApplication.class, args);
  }

  @Bean
  SolrClient solrClient(@Value("${solr.server.articles.url}") String articlesUrl) {
    return new HttpJdkSolrClient.Builder(articlesUrl).build();
  }
}
