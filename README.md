# News Scraper Search Engine

This project scrapes article data from [The Hindu archive](https://www.thehindu.com/archive/) and stores it in a JSON file. The scraped data is indexed in Apache Solr. A Spring Boot application exposes REST APIs to search the indexed articles.

## Tech Stack

| Component | Version |
| ----------- | --------- |
| Scrapy (Python) | 2.x |
| Apache Solr | 9.10.1 |
| Spring Boot | 3.4.4 |
| Java | 21 |
| Build tool | Gradle |
| SolrJ | 9.7.0 |

> **Note:** The configuration in `solrCore/conf/` has been updated for Solr 9.10.1. The schema defines the `url`, `title`, `description`, and `author` fields used by the application.

## Getting Started

### Prerequisites

#### Scraping

Data scraping uses [Scrapy](https://scrapy.org/), a Python crawling framework that extracts data from web pages using XPath/CSS selectors.

Install Python from <https://www.python.org/downloads/>

Then install Scrapy:

```bash
pip install scrapy
```

After scraping, data is stored in a JSON file. To load that data into Solr, use [pysolr](https://pypi.org/project/pysolr/):

```bash
pip install pysolr
```

#### Searching (Solr)

Search is powered by [Apache Solr](https://solr.apache.org/). Install Solr 9.10.1 from <https://solr.apache.org/downloads.html>

Start the server from the Solr install directory:

```bash
bin/solr start
```

Create a Solr core named `articles`:

```bash
# Solr 9
bin/solr create -c articles
```

Apply the project schema and config by replacing the individual files. From your Solr install directory:

```bash
cp /path/to/newsScraper/solrCore/conf/managed-schema.xml server/solr/articles/conf/
cp /path/to/newsScraper/solrCore/conf/solrconfig.xml server/solr/articles/conf/
```

Restart Solr after updating the config:

```bash
bin/solr restart
```

#### API (Spring Boot)

The Spring Boot application lives in `NewsScraperSearch/`.

- **Java 21** — <https://www.oracle.com/java/technologies/downloads/>
- **Gradle** — not required; the project includes the Gradle Wrapper (`gradlew`)

## Running the Pipeline

### 1. Scrape articles

Open a terminal in the `newsScraper/` folder (the one containing `scrapy.cfg`) and run:

```bash
scrapy crawl thehindubot -o items.json -t json
```

This creates `items.json` with entries like:

```json
{
  "url": "https://www.thehindu.com/sport/other-sports/kore-stuns-deepan-in-joint-lead/article2790567.ece",
  "title": "Kore stuns Deepan, in joint lead",
  "description": "IM Akshayraj Kore shocked GM Deepan Chakkravarthy to join the leaders at the end of the ninth round in the SDAT-RMK Chennai Open international Grandmaster chess tournament here on Tuesday.",
  "author": "Arvind Aaron"
}
```

### 2. Index data into Solr

From the same `newsScraper/` folder:

```bash
python inject.py items.json http://localhost:8983/solr/articles
```

The importer sends the documents but does not commit them. Commit the batch before searching:

```bash
curl -X POST 'http://localhost:8983/solr/articles/update?commit=true' \
  -H 'Content-Type: application/json' \
  --data-binary '[]'
```

Replace the host if Solr is running on a different machine.

If Solr and the Spring Boot app run on different hosts, update `solr.server.articles.url` in `NewsScraperSearch/src/main/resources/application.properties`.

### 3. Start the API server

From the `NewsScraperSearch/` folder:

```bash
./gradlew clean build
java -jar build/libs/NewsScraper-0.0.1-SNAPSHOT.jar
```

Or run without building a jar:

```bash
./gradlew bootRun
```

The server starts on port **8081** with context path `/newsScraper`.

## API Reference

### Author Search

Searches for author names matching the query.

- **Endpoint:** `GET /newsScraper/author/search?author={query}`
- **Sample:** <http://localhost:8081/newsScraper/author/search?author=ap>

```json
{
  "authors": [
    { "author": "AP" },
    { "author": "Rajulapudi Srinivas" },
    { "author": "Vijay Lokapally" }
  ]
}
```

The `author` parameter is required.

### Article Search

Searches indexed articles in Solr. Exactly **one** of `author`, `title`, or `description` is required.

- **Method:** GET

#### By author

- **Endpoint:** `GET /newsScraper/article/search?author={query}`
- **Sample:** <http://localhost:8081/newsScraper/article/search?author=ap>

#### By title or description

- **Endpoints:**
  - `GET /newsScraper/article/search?title={query}`
  - `GET /newsScraper/article/search?description={query}`
- **Samples:**
  - <http://localhost:8081/newsScraper/article/search?title=Bomb>
  - <http://localhost:8081/newsScraper/article/search?description=exploded>

```json
{
  "articles": [
    {
      "url": "https://www.thehindu.com/news/international/bomb-kills-20-in-northwest-pakistan/article2789825.ece",
      "title": "Bomb kills 20 in northwest Pakistan",
      "author": "AP",
      "description": "A bomb exploded close to a bus in northwest Pakistan on Tuesday, killing 20 people in the deadliest blast in the country in several months, a government official said."
    },
    {
      "url": "https://www.thehindu.com/opinion/op-ed/fake-bomb-smuggled-into-olympic-site/article2788319.ece",
      "title": "Fake bomb smuggled into Olympic site",
      "author": "AP",
      "description": "U.K. police managed to smuggle a fake bomb into Olympic Park in a security test, overshadowing a special Cabinet meeting on Monday at the park that marked 200 days until the Summer Games begin."
    }
  ]
}
```

Optional pagination: `pageNumber` (0-based, 30 results per page).

## Authors

- **Sugandh Chaudhary**

## Acknowledgments

- Hat tip to anyone whose code was used
