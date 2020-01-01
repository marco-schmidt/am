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
package am.app;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieve, store and print information on the environment that the application is running in.
 */
public class SystemInfo
{
  /** Application name (short version). */
  public static final String APP_NAME = "am";
  /** Application URI. */
  public static final String APP_URI = "https://github.com/marco-schmidt/am";
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemInfo.class);
  private final List<AbstractMap.SimpleEntry<String, String>> props = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
  private String applicationVersion;

  public int getNumProperties()
  {
    return props.size();
  }

  public void initialize(final AppConfig config, final String... args)
  {
    add(config.msg("system.workingdir"), System.getProperty("user.dir"));
    add(config.msg("system.java"), System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") + " "
        + System.getProperty("java.version"));
    add(config.msg("system.os"),
        System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
    final InetAddress ipAddress = getIpAddress();
    add(config.msg("system.ipaddress"), getHostAddress(ipAddress));
    add(config.msg("system.hostname"), getHostName(ipAddress));
    final Runtime runtime = Runtime.getRuntime();
    add(config.msg("system.cpus"), getNumCpus(runtime));
    add(config.msg("system.memory.free"), Long.toString(runtime.freeMemory()));
    add(config.msg("system.memory.total"), Long.toString(runtime.totalMemory()));
    add(config.msg("system.memory.max"), Long.toString(runtime.maxMemory()));
    add(config.msg("system.username"), System.getProperty("user.name"));
    add(config.msg("system.userhomedir"), System.getProperty("user.home"));
    add(config.msg("system.argsvm"), getArgumentsVm());
    add(config.msg("system.argsapp"), Arrays.toString(args));
    add(config.msg("system.processid"), getProcessId());
    add(config.msg("system.jars"), getManifestInfo());
  }

  /**
   * Return this application's process ID.
   *
   * Replace with this in Java 9: <code>long pid = ProcessHandle.current().getPid();</code>
   *
   * @return process ID
   */
  private String getProcessId()
  {
    final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    final String name = runtimeMXBean.getName();
    final int index = name == null ? -1 : name.indexOf('@');
    return index > 0 ? name.substring(0, index) : "";
  }

  private String getArgumentsVm()
  {
    final StringBuilder sb = new StringBuilder();
    final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    final List<String> jvmArgs = runtimeMXBean.getInputArguments();
    for (final String arg : jvmArgs)
    {
      if (sb.length() > 0)
      {
        sb.append(' ');
      }
      sb.append(arg);
    }
    return sb.toString();
  }

  private static String getNumCpus(final Runtime runtime)
  {
    return Integer.toString(runtime.availableProcessors());
  }

  private static String getHostAddress(final InetAddress ipAddress)
  {
    return ipAddress == null ? "?" : ipAddress.getHostAddress();
  }

  private static String getHostName(final InetAddress ipAddress)
  {
    return ipAddress == null ? "?" : ipAddress.getHostName();
  }

  private static InetAddress getIpAddress()
  {
    InetAddress result = null;
    try
    {
      result = InetAddress.getLocalHost();
    }
    catch (final UnknownHostException uhe)
    {
      LOGGER.error("Cannot determine local host.", uhe);
    }
    return result;
  }

  public void add(final String key, final String value)
  {
    props.add(new AbstractMap.SimpleEntry<String, String>(key, value));
  }

  public String getApplicationVersion()
  {
    return applicationVersion;
  }

  public void setApplicationVersion(final String applicationVersion)
  {
    this.applicationVersion = applicationVersion;
  }

  public void print(final AppConfig config)
  {
    // determine longest key
    int max = 0;
    for (final AbstractMap.SimpleEntry<String, String> entry : props)
    {
      final String key = entry.getKey();
      max = max(max, key);
    }
    max = Math.max(max, 1);
    // print all pairs as padded key and value
    for (final AbstractMap.SimpleEntry<String, String> entry : props)
    {
      String key = entry.getKey();
      if (key == null || key.isEmpty())
      {
        key = "?";
      }
      LOGGER.info(String.format("%1$-" + max + "s", key) + "=" + entry.getValue());
    }
  }

  private int max(final int value1, final String value2)
  {
    return Math.max(value1, value2.length());
  }

  private String getManifestInfo()
  {
    Enumeration<URL> resources = null;
    try
    {
      resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
    }
    catch (final IOException e)
    {
      LOGGER.error("Unable to retrieve manifest resources.", e);
    }
    final StringBuilder sb = new StringBuilder();
    if (resources != null)
    {
      while (resources.hasMoreElements())
      {
        final URL url = resources.nextElement();
        final String resourceInfo = parseManifest(url);
        append(sb, resourceInfo);
      }
    }
    return sb.toString();
  }

  private void append(final StringBuilder sb, final String msg)
  {
    if (sb != null && msg != null)
    {
      if (sb.length() > 0)
      {
        sb.append(", ");
      }
      sb.append(msg);
    }
  }

  private String parseManifest(final URL url)
  {
    String result = null;
    try
    {
      final Manifest manifest = new Manifest(url.openStream());
      final Attributes attr = manifest.getMainAttributes();
      Object title = attr.getValue("Implementation-Title");
      Object version = attr.getValue("Implementation-Version");
      if (title == null)
      {
        title = attr.getValue("Bundle-Name");
        version = attr.getValue("Bundle-Version");
      }
      if (title != null && version != null)
      {
        result = title + " " + version;
        if (APP_NAME.equals(title))
        {
          setApplicationVersion(version.toString());
        }
      }
      final String jarName = url.getFile();
      if (result == null)
      {
        result = jarName;
      }
      else
      {
        result = String.format("%s (%s)", result, jarName);
      }
    }
    catch (final IOException ioe)
    {
      LOGGER.error("Unable to parse manifest.", ioe);
    }
    return result;
  }
}
