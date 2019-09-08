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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.rdf4j.http.client.util.HttpClientBuilders;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Query information from Wikidata.
 *
 * @author Marco Schmidt
 */
public class WikidataService
{
  private String uriSparqlEndpoint = "https://query.wikidata.org/sparql";

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
    final String queryStr = "select distinct ?show where\n" + "{\n" + "  ?show wdt:P31/wdt:P279* wd:Q15416.\n"
        + "  ?show rdfs:label ?label .\n" + "  ?show wdt:P580 ?start .\n" + "  filter(str(?label) = \"" + title
        + "\")\n" + "  filter(year(?start) = " + year + ")}";
    final HttpClientBuilder httpClientBuilder = HttpClientBuilders.getSSLTrustAllHttpClientBuilder();
    httpClientBuilder.setMaxConnTotal(10);
    httpClientBuilder.setMaxConnPerRoute(5);
    final HttpClient httpClient = httpClientBuilder.build();
    final SPARQLRepository repo = new SPARQLRepository(getUriSparqlEndpoint());
    repo.setHttpClient(httpClient);
    final RepositoryConnection conn = repo.getConnection();
    try
    {
      final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
      final TupleQueryResult rs = query.evaluate();
      if (rs.hasNext())
      {
        final BindingSet next = rs.next();
        final Value value = next.getValue("show");
        rs.close();
        final String uri = value == null ? null : value.stringValue();
        final int index = uri == null ? -1 : uri.lastIndexOf('/');
        return index < 0 ? null : uri.substring(index + 1);
      }
    }
    finally
    {
      conn.close();
    }
    return null;
  }

  public String getUriSparqlEndpoint()
  {
    return uriSparqlEndpoint;
  }

  public void setUriSparqlEndpoint(String uriSparqlEndpoint)
  {
    this.uriSparqlEndpoint = uriSparqlEndpoint;
  }
}
