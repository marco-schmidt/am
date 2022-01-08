/*
 * Copyright 2019, 2020, 2021, 2022 the original author or authors.
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
package am.filesystem.model;

/**
 * Data class for information stored in a file name.
 *
 * @author Marco Schmidt
 */
public class VideoFileName
{
  private Long firstEpisode;
  private Long lastEpisode;
  private Long resolution;
  private Long season;
  private String title;
  private Long year;
  private String episodeTitle;

  public Long getFirstEpisode()
  {
    return firstEpisode;
  }

  public void setFirstEpisode(Long firstEpisode)
  {
    this.firstEpisode = firstEpisode;
  }

  public Long getLastEpisode()
  {
    return lastEpisode;
  }

  public void setLastEpisode(Long lastEpisode)
  {
    this.lastEpisode = lastEpisode;
  }

  public Long getResolution()
  {
    return resolution;
  }

  public void setResolution(Long resolution)
  {
    this.resolution = resolution;
  }

  public Long getSeason()
  {
    return season;
  }

  public void setSeason(Long season)
  {
    this.season = season;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public Long getYear()
  {
    return year;
  }

  public void setYear(Long year)
  {
    this.year = year;
  }

  public String getEpisodeTitle()
  {
    return episodeTitle;
  }

  public void setEpisodeTitle(String episodeTitle)
  {
    this.episodeTitle = episodeTitle;
  }
}
