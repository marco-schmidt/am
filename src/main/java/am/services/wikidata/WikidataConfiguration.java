/*
 * Copyright 2019, 2020 the original author or authors.
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

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;

/**
 * Configuration data class about usage of <a href="https://www.wikidata.org/">Wikidata</a> information.
 *
 * @author Marco Schmidt
 */
public class WikidataConfiguration
{
  private boolean enabled;
  private WikibaseDataFetcher fetcher;
  private RepositoryConnection connection;
  private String uriSparqlEndpoint = "https://query.wikidata.org/sparql";
  private WikidataService service;

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public WikibaseDataFetcher getFetcher()
  {
    return fetcher;
  }

  public void setFetcher(WikibaseDataFetcher fetcher)
  {
    this.fetcher = fetcher;
  }

  public RepositoryConnection getConnection()
  {
    return connection;
  }

  public void setConnection(RepositoryConnection connection)
  {
    this.connection = connection;
  }

  public String getUriSparqlEndpoint()
  {
    return uriSparqlEndpoint;
  }

  public void setUriSparqlEndpoint(String uriSparqlEndpoint)
  {
    this.uriSparqlEndpoint = uriSparqlEndpoint;
  }

  public WikidataService getService()
  {
    return service;
  }

  public void setService(WikidataService service)
  {
    this.service = service;
  }
}
