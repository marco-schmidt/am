/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.services.wikidata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.http.client.util.HttpClientBuilders;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;

/**
 * Query information from Wikidata.
 *
 * @author Marco Schmidt
 */
public class WikidataService
{
  /**
   * The string representing an unknown Wikidata entity, a non-null value to indicate that the application searched for
   * it without result, avoiding identical queries in the future.
   */
  public static final String UNKNOWN_ENTITY = "?";
  private static final Logger LOGGER = LoggerFactory.getLogger(WikidataService.class);
  private AppConfig appConfig;
  private WikidataConfiguration config;
  private String sparqlFindTelevisionShow;
  private String sparqlFindTelevisionSeasons;
  private String sparqlFindTelevisionEpisodes;

  private void ensureSparqlConnection()
  {
    final RepositoryConnection connection = config.getConnection();
    if (connection == null)
    {
      long millis = System.currentTimeMillis();
      final HttpClientBuilder httpClientBuilder = HttpClientBuilders.getSSLTrustAllHttpClientBuilder();
      LOGGER.debug(appConfig.msg("wikidataservice.debug.created_http_builder", System.currentTimeMillis() - millis));
      millis = System.currentTimeMillis();
      httpClientBuilder.setMaxConnTotal(10);
      httpClientBuilder.setMaxConnPerRoute(5);
      final HttpClient httpClient = httpClientBuilder.build();
      LOGGER
          .debug(appConfig.msg("wikidataservice.debug.initialized_http_builder", System.currentTimeMillis() - millis));
      final SPARQLRepository repo = new SPARQLRepository(config.getUriSparqlEndpoint());
      repo.setHttpClient(httpClient);
      config.setConnection(repo.getConnection());
    }
  }

  /**
   * Load file content from argument path using a class loader's getResourceAsStream method.
   *
   * @param path
   *          file to be read
   * @return file content as String or null on failure
   */
  String loadSparqlTemplate(final String path)
  {
    final StringBuilder sb = new StringBuilder();
    final Reader in = new BufferedReader(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8));
    int c = 0;
    try
    {
      while ((c = in.read()) != -1)
      {
        sb.append((char) c);
      }
    }
    catch (final IOException e)
    {
      LOGGER.error(appConfig.msg("wikidataservice.error.failed_reading_file", path));
      return null;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (final IOException e)
      {
        LOGGER.error(appConfig.msg("wikidataservice.error.failed_closing_file", path));
      }
    }
    return sb.toString();
  }

  private String getFindTelevisionShowByTitleAndYearTemplate()
  {
    if (sparqlFindTelevisionShow == null)
    {
      sparqlFindTelevisionShow = loadSparqlTemplate("am/services/wikidata/FindTelevisionShowByTitleAndStartTime.rq");
    }
    return sparqlFindTelevisionShow;
  }

  private String getFindTelevisionSeasonsByShowTemplate()
  {
    if (sparqlFindTelevisionSeasons == null)
    {
      sparqlFindTelevisionSeasons = loadSparqlTemplate("am/services/wikidata/FindTelevisionSeasonsByShow.rq");
    }
    return sparqlFindTelevisionSeasons;
  }

  private String getFindTelevisionEpisodesBySeasonTemplate()
  {
    if (sparqlFindTelevisionEpisodes == null)
    {
      sparqlFindTelevisionEpisodes = loadSparqlTemplate("am/services/wikidata/FindTelevisionEpisodesBySeason.rq");
    }
    return sparqlFindTelevisionEpisodes;
  }

  String buildFindTelevisionShowQuery(String title, Integer year)
  {
    String query = getFindTelevisionShowByTitleAndYearTemplate();
    if (query != null)
    {
      query = query.replace("@TITLE@", title);
      query = query.replace("@YEAR@", year.toString());
    }
    return query;
  }

  String buildFindTelevisionSeasons(String title, Integer year)
  {
    String query = getFindTelevisionSeasonsByShowTemplate();
    if (query != null)
    {
      query = query.replace("@TITLE@", title);
      query = query.replace("@YEAR@", year.toString());
    }
    return query;
  }

  String buildFindTelevisionSeasonsQuery(String showEntityId)
  {
    String query = getFindTelevisionSeasonsByShowTemplate();
    if (query != null)
    {
      query = query.replace("@SHOW@", showEntityId);
    }
    return query;
  }

  String buildFindTelevisionEpisodesQuery(String seasonEntityId)
  {
    String query = getFindTelevisionEpisodesBySeasonTemplate();
    if (query != null)
    {
      query = query.replace("@SEASON@", seasonEntityId);
    }
    return query;
  }

  String extractEntity(Value uri)
  {
    final String s = uri == null ? null : uri.stringValue();
    return extractEntity(s);
  }

  String extractEntity(String uri)
  {
    final int index = uri == null ? -1 : uri.lastIndexOf('/');
    return index < 0 ? null : uri.substring(index + 1);
  }

  /**
   * Search for television show.
   *
   * @param title
   *          name of show
   * @param year
   *          year of the first airdate of an episode of the show
   * @return WikiData entity id or null
   */
  public String searchTelevisionShow(String title, Integer year)
  {
    final String queryStr = buildFindTelevisionShowQuery(title, year);
    ensureSparqlConnection();
    final RepositoryConnection conn = config.getConnection();
    if (conn == null)
    {
      return null;
    }
    try
    {
      final long millis = System.currentTimeMillis();
      final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
      final TupleQueryResult rs = query.evaluate();
      LOGGER.debug(appConfig.msg("wikidataservice.debug.sparql_query_time", System.currentTimeMillis() - millis));
      if (rs.hasNext())
      {
        final BindingSet next = rs.next();
        final Value value = next.getValue("show");
        rs.close();
        return value == null ? null : extractEntity(value.stringValue());
      }
    }
    catch (final QueryEvaluationException qee)
    {
      LOGGER.error(appConfig.msg("wikidataservice.error.failed_to_run_query"), qee);
      conn.close();
      config.setConnection(null);
    }
    return null;
  }

  /**
   * Search for television seasons.
   *
   * @param dir
   *          show {@link Directory}
   * @param showEntityId
   *          Wikidata entity ID of show
   * @param mapMissing
   *          map from season number string (no leading zeroes) to {@link Directory} of that season
   */
  public void searchTelevisionSeasons(final Directory dir, final String showEntityId,
      final Map<String, Directory> mapMissing)
  {
    final String queryStr = buildFindTelevisionSeasonsQuery(showEntityId);
    ensureSparqlConnection();
    final RepositoryConnection conn = config.getConnection();
    if (conn == null)
    {
      return;
    }
    try
    {
      final long millis = System.currentTimeMillis();
      final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
      final TupleQueryResult rs = query.evaluate();
      LOGGER.debug(appConfig.msg("wikidataservice.debug.sparql_query_time", System.currentTimeMillis() - millis));
      while (rs.hasNext())
      {
        final BindingSet next = rs.next();
        final Value season = next.getValue("season");
        final String entity = extractEntity(season);
        final Value seasonNumber = next.getValue("seasNr");
        final String numberString = seasonNumber.stringValue();
        final Directory directory = mapMissing.get(numberString);
        if (directory != null)
        {
          directory.setWikidataEntityId(entity);
          LOGGER
              .info(appConfig.msg("wikidataservice.info.television_show_season", entity, dir.getName(), numberString));
        }
      }
      rs.close();
    }
    catch (final QueryEvaluationException qee)
    {
      LOGGER.error(appConfig.msg("wikidataservice.error.failed_to_run_query"), qee);
      conn.close();
      config.setConnection(null);
    }
  }

  /**
   * Search for television episodes.
   *
   * @param seasonEntityId
   *          Wikidata entity ID of show
   * @param mapMissing
   *          map from episode number string (number relative to the season) to {@link File}
   */
  public void searchTelevisionEpisodes(final String seasonEntityId, final Map<Long, File> mapMissing)
  {
    if (seasonEntityId == null || seasonEntityId.equals(WikidataService.UNKNOWN_ENTITY))
    {
      return;
    }
    final String queryStr = buildFindTelevisionEpisodesQuery(seasonEntityId);
    ensureSparqlConnection();
    final RepositoryConnection conn = config.getConnection();
    if (conn == null)
    {
      return;
    }
    try
    {
      final long millis = System.currentTimeMillis();
      final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
      final TupleQueryResult rs = query.evaluate();
      LOGGER.debug(appConfig.msg("wikidataservice.debug.sparql_query_time", System.currentTimeMillis() - millis));
      while (rs.hasNext())
      {
        final BindingSet next = rs.next();
        final Value episode = next.getValue("episode");
        final String entity = extractEntity(episode);
        final Value relativeNumberValue = next.getValue("relNr");
        final String relativeNumberString = relativeNumberValue.stringValue();
        final Long relativeNumber = Long.valueOf(relativeNumberString);
        final File file = mapMissing.get(relativeNumber);
        if (file != null)
        {
          file.setWikidataEntityId(entity);
          LOGGER.info(appConfig.msg("wikidataservice.info.television_show_episode", entity, file.getName()));
        }
      }
      rs.close();
    }
    catch (final QueryEvaluationException qee)
    {
      LOGGER.error(appConfig.msg("wikidataservice.error.failed_to_run_query"), qee);
      conn.close();
      config.setConnection(null);
    }
  }

  public WikidataConfiguration getConfig()
  {
    return config;
  }

  public void setConfig(WikidataConfiguration config)
  {
    this.config = config;
  }

  public AppConfig getAppConfig()
  {
    return appConfig;
  }

  public void setAppConfig(AppConfig appConfig)
  {
    this.appConfig = appConfig;
  }
}
