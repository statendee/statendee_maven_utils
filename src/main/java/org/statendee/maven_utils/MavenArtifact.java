/*
Copyright 2021 Statendee (statendee.org)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.statendee.maven_utils;

import org.codehaus.plexus.util.Base64;
import org.statendee.maven_utils.version.ComparableVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * A {@link MavenArtifact} represents an artifact in a maven repository.
 *
 * @author jojomatik
 * @version 0.2.1
 * @since 0.1.0
 */
public class MavenArtifact {

  /**
   * The URL to the repository that contains the artifact.
   *
   * @since 0.1.0
   */
  public final String repository;

  /**
   * The groupId of the artifact.
   *
   * @since 0.1.0
   */
  public final String groupId;

  /**
   * The artifactId of the artifact.
   *
   * @since 0.1.0
   */
  public final String artifactId;

  /**
   * The username to authenticate to the repository.
   *
   * @since 0.1.0
   */
  private String username;

  /**
   * The token (or password) to authenticate to the repository.
   *
   * @since 0.1.0
   */
  private String token;

  /**
   * Creates an instance of {@link MavenArtifact} based on the URL of the repository, the groupId
   * and the artifactId.
   *
   * <p>Appends a trailing slash to the repository if it's missing.
   *
   * @param repository the URL to the repository that contains the artifact.
   * @param groupId the URL to the repository that contains the artifact.
   * @param artifactId the artifactId of the artifact.
   * @since 0.1.0
   */
  public MavenArtifact(String repository, String groupId, String artifactId) {
    this.repository = repository.endsWith("/") ? repository : (repository + "/");
    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  /**
   * Creates an instance of {@link MavenArtifact} based on the URL of the repository, the groupId,
   * the artifactId and the credentials.
   *
   * <p>Appends a trailing slash to the repository if it's missing.
   *
   * @param repository the URL to the repository that contains the artifact.
   * @param groupId the URL to the repository that contains the artifact.
   * @param artifactId the artifactId of the artifact.
   * @param username the username to authenticate to the repository.
   * @param token the token (or password) to authenticate to the repository.
   * @since 0.1.0
   */
  public MavenArtifact(
      String repository, String groupId, String artifactId, String username, String token) {
    this(repository, groupId, artifactId);
    this.username = username;
    this.token = token;
  }

  /**
   * Returns the URL-{@link String} to the artifact.
   *
   * @return the URL-{@link String} to the artifact.
   * @since 0.1.0
   */
  private String getURLFromCoordinates() {
    return repository + groupId.replace(".", "/").replace("_", "-") + "/" + artifactId;
  }

  /**
   * Returns the latest release version available in the maven repository as a {@link
   * ComparableVersion}.
   *
   * @return the latest release version available in the maven repository as a {@link
   *     ComparableVersion}.
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws SAXException if any parse errors occur.
   * @since 0.1.0
   */
  public ComparableVersion getLatestReleaseVersion()
      throws IOException, RequestException, ParserConfigurationException, SAXException {
    String latestReleaseVersion =
        getVersioning().getElementsByTagName("release").item(0).getFirstChild().getTextContent();
    return new ComparableVersion(latestReleaseVersion);
  }

  /**
   * Returns the latest version - including {@code SNAPSHOT}-versions - available in the maven
   * repository as a {@link ComparableVersion}.
   *
   * @return the latest version available in the maven repository as a {@link
   *     ComparableVersion}.
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws SAXException if any parse errors occur.
   * @since 0.1.0
   */
  public ComparableVersion getLatestVersion()
      throws IOException, RequestException, ParserConfigurationException, SAXException {
    String latestVersionString =
        getVersioning().getElementsByTagName("latest").item(0).getFirstChild().getTextContent();

    ComparableVersion latestVersion = new ComparableVersion(latestVersionString);
    if (latestVersion.isSnapshot()) latestVersion = getLatestSnapshotBuild(latestVersion);

    return latestVersion;
  }

  /**
   * Returns the latest {@code SNAPSHOT}-build (including timestamp and build number) of a specific
   * {@code SNAPSHOT}-version available in the maven repository as a {@link ComparableVersion}.
   *
   * @param snapshotVersion the {@code SNAPSHOT}-version to retrieve the {@code SNAPSHOT}-build for.
   * @return the latest {@code SNAPSHOT}-build (including timestamp and build number) for specified {@code SNAPSHOT}-version.
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws SAXException if any parse errors occur.
   * @since 0.1.0
   */
  public ComparableVersion getLatestSnapshotBuild(ComparableVersion snapshotVersion)
      throws IOException, RequestException, ParserConfigurationException, SAXException {

    Element versioning = getVersioning(getURLFromCoordinates() + "/" + snapshotVersion);
    Element snapshot = (Element) versioning.getElementsByTagName("snapshot").item(0);

    String timestamp =
        snapshot.getElementsByTagName("timestamp").item(0).getFirstChild().getTextContent();
    String buildNumber =
        snapshot.getElementsByTagName("buildNumber").item(0).getFirstChild().getTextContent();

    return new ComparableVersion(snapshotVersion + "-" + timestamp + "-" + buildNumber);
  }

  /**
   * Returns the {@code versioning} element of the main {@code maven-metadata.xml} file of the
   * artifact.
   *
   * @return the {@code versioning} element of the main {@code maven-metadata.xml} file of the
   *     artifact.
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws SAXException if any parse errors occur.
   * @since 0.1.0
   */
  private Element getVersioning()
      throws IOException, RequestException, ParserConfigurationException, SAXException {
    return getVersioning(getURLFromCoordinates());
  }

  /**
   * Returns the {@code versioning} element of the {@code maven-metadata.xml} file at specified url.
   *
   * @return the {@code versioning} element of the {@code maven-metadata.xml} file at specified url.
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws SAXException if any parse errors occur.
   * @since 0.1.0
   */
  private Element getVersioning(String url)
      throws IOException, RequestException, ParserConfigurationException, SAXException {
    String urlString = url + "/maven-metadata.xml";

    Document result = parseXML(request(urlString));
    Element metadata = result.getDocumentElement();
    NodeList versioningList = metadata.getElementsByTagName("versioning");
    return (Element) versioningList.item(0);
  }

  /**
   * Downloads a file from the maven repository based on the version, classifier, extension and the
   * target path.
   *
   * @param version the version that should be downloaded.
   * @param classifier the classifier of the file that should be downloaded (e.g. {@code javadoc} or {@code jar-with-dependencies}).
   * @param extension the extension of the file.
   * @param path the path where the file should be downloaded to (including the file name).
   * @throws IOException if an I/O error occurs e.g. while parsing the {@link InputStream}.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @since 0.1.0
   */
  public void download(ComparableVersion version, String classifier, String extension, String path)
      throws IOException, RequestException {
    String urlString =
        getURLFromCoordinates()
            + "/"
            + version.getVersionWithoutBuildInfo()
            + "/"
            + this.artifactId
            + "-"
            + version.toString().replace("-SNAPSHOT", "")
            + (!classifier.equals("") ? "-" : "")
            + classifier
            + "."
            + extension;

    try (InputStream initialStream = request(urlString)) {
      File file = new File(path);
      //noinspection ResultOfMethodCallIgnored
      file.getParentFile().mkdirs();
      Files.copy(initialStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Returns an {@link InputStream} for a given url {@link String}. If {@link #username} and {@link
   * #token} are set, they are used in the {@code Authorization} header.
   *
   * @param urlString the url {@link String} that should be queried.
   * @return the {@link InputStream} for the given url-{@link String}.
   * @throws IOException if an I/O error occurs.
   * @throws RequestException if the server returned an response code other than 4xx or 5xx.
   * @since 0.1.0
   */
  private InputStream request(String urlString) throws IOException, RequestException {
    URL server = new URL(urlString);
    HttpsURLConnection connection = (HttpsURLConnection) server.openConnection();

    String header;
    if (username != null && token != null && !username.equals("") && !token.equals("")) {
      String auth = username + ":" + token;
      byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
      header = "Basic " + new String(encodedAuth);
      connection.setRequestProperty("Authorization", header);
    }

    connection.connect();
    int responseCode;
    if ((responseCode = connection.getResponseCode()) >= 400 && responseCode < 600)
      throw new RequestException(responseCode);
    return connection.getInputStream();
  }

  /**
   * Returns a xml {@link Document} for a given {@link InputStream}.
   *
   * @param xml the {@link InputStream} that should be converted to an {@link Document}.
   * @return the xml {@link Document} fot the given {@link InputStream}.
   * @throws IOException if any I/O errors occur while parsing the {@link InputStream}.
   * @throws SAXException if any parse errors occur.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @since 0.1.0
   */
  private static Document parseXML(InputStream xml)
      throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(xml);
    doc.getDocumentElement().normalize();
    return doc;
  }
}
