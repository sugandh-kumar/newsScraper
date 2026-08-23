package com.news.scraper.constants;

public final class SearchConstants {

  private SearchConstants() {}

  public static final String PATTERN_STRING = "[^a-zA-Z\\d\\s:]";
  public static final String QUERY_FAILURE = "Error while querying for the results";
  public static final String INVALID_INPUT = "Please provide search parameter";
  public static final String AUTHOR_QUERY_STRING = "author:(/.*%s.*/ OR %s)";
  public static final String TITLE_QUERY_STRING = "title:(/.*%s.*/ OR %s)";
  public static final String DESCRIPTION_QUERY_STRING = "description:(/.*%s.*/ OR %s)";
  public static final String MAX_RESULTS = "30";
}
