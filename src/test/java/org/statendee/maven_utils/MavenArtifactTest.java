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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.statendee.maven_utils.version.ComparableVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * A class that contains {@link Test}s to test the {@link MavenArtifact} class.
 *
 * @author jojomatik
 * @version 0.2.0
 * @since 0.2.0
 */
public class MavenArtifactTest {

  /**
   * The url {@link String} to the mock repository.
   *
   * @since 0.2.0
   */
  private static final String repo = "https://localhost";

  /**
   * The groupId of the mock artifact.
   *
   * @since 0.2.0
   */
  private static final String group_id = "test";

  /**
   * The artifactId of the mock artifact.
   *
   * @since 0.2.0
   */
  private static final String artifact_id = "test";

  /**
   * The username to authenticate to the mock repository.
   *
   * @since 0.2.0
   */
  private static final String username = "test";

  /**
   * The token to authenticate to the mock repository.
   *
   * @since 0.2.0
   */
  private static final String token = "test";

  /**
   * A {@link DocumentBuilder} used to create mock {@link Document}s.
   *
   * @since 0.2.0
   */
  private static DocumentBuilder builder;

  static {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      builder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets up a mock {@link URLStreamHandlerFactory} that provides a {@link MockUrlStreamHandler}
   * with which one can programmatically mock {@link URLConnection}s.
   *
   * @since 0.2.0
   */
  @BeforeAll
  static void prepare() {
    URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
    URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);

    MockUrlStreamHandler urlStreamHandler = new MockUrlStreamHandler();
    when(urlStreamHandlerFactory.createURLStreamHandler("https")).thenReturn(urlStreamHandler);
  }

  /**
   * Tests if {@link MavenArtifact#getLatestReleaseVersion()} throws an {@link IOException} if one
   * is thrown by {@link HttpsURLConnection#getInputStream()}.
   *
   * @throws IOException if an I/O error occurs.
   * @since 0.2.0
   */
  @Test
  void testIOException() throws IOException {
    // Create mock connection.
    HttpsURLConnection mockConnectionVersion = mock(HttpsURLConnection.class);
    // Throw exception on method call.
    when(mockConnectionVersion.getInputStream()).thenThrow(new IOException());
    // Put mock connection in map.
    MockUrlStreamHandler.mockConnections.put(
        new URL(repo + "/" + group_id + "/" + artifact_id + "/maven-metadata.xml"),
        mockConnectionVersion);

    // Create an unauthenticated artifact (with trailing slash added).
    MavenArtifact artifact = new MavenArtifact(repo + "/", group_id, artifact_id);

    // Assert if IOException is thrown.
    assertThrows(
        IOException.class,
        artifact::getLatestReleaseVersion,
        "An " + IOException.class.getName() + " should be thrown, but it is not!");

    // Verify number of method calls.
    verify(
            mockConnectionVersion,
            times(0)
                .description(
                    "Request should be unauthenticated, but an authentication header was set."))
        .setRequestProperty(eq("Authorization"), Mockito.anyString());
    verify(
            mockConnectionVersion,
            times(1)
                .description(
                    "The method `connect` should be called exactly once on this connection, but was called a different amount of times."))
        .connect();
    verify(
            mockConnectionVersion,
            times(1)
                .description(
                    "The method `getResponseCode` should be called exactly once on this connection, but was called a different amount of times."))
        .getResponseCode();
  }

  /**
   * Tests if {@link MavenArtifact#getLatestReleaseVersion()} throws a {@link RequestException} if
   * {@link HttpsURLConnection#getResponseCode()} returns {@code 403}.
   *
   * @throws IOException if an I/O error occurs.
   * @since 0.2.0
   */
  @Test
  void testRequestException() throws IOException {
    // Create mock connection.
    HttpsURLConnection mockConnectionVersion = mock(HttpsURLConnection.class);
    // Return 403 on method call.
    when(mockConnectionVersion.getResponseCode()).thenReturn(403);
    // Put mock connection in map.
    MockUrlStreamHandler.mockConnections.put(
        new URL(repo + "/" + group_id + "/" + artifact_id + "/maven-metadata.xml"),
        mockConnectionVersion);

    // Create an unauthenticated artifact.
    MavenArtifact artifact = new MavenArtifact(repo, group_id, artifact_id);

    // Assert if RequestException is thrown.
    assertThrows(
        RequestException.class,
        artifact::getLatestReleaseVersion,
        "A " + RequestException.class.getName() + " should be thrown, but it is not!");

    // Verify number of method calls.
    verify(
            mockConnectionVersion,
            times(0)
                .description(
                    "Request should be unauthenticated, but an authentication header was set."))
        .setRequestProperty(eq("Authorization"), Mockito.anyString());
    verify(
            mockConnectionVersion,
            times(1)
                .description(
                    "The method `connect` should be called exactly once on this connection, but was called a different amount of times."))
        .connect();
    verify(
            mockConnectionVersion,
            times(1)
                .description(
                    "The method `getResponseCode` should be called exactly once on this connection, but was called a different amount of times."))
        .getResponseCode();
  }

  /**
   * Tests if the correct {@link ComparableVersion}s are parsed given a mock {@link InputStream} and
   * if the download-{@link URL} is built correctly.
   *
   * @throws IOException if an I/O error occurs.
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *     configuration requested.
   * @throws RequestException if the mock server returned an response code other than 4xx or 5xx.
   * @throws SAXException if any parse errors occur.
   * @throws TransformerException if the mock {@link InputStream} cannot be built.
   * @since 0.2.0
   */
  @Test
  void testArtifactRequests()
      throws IOException, ParserConfigurationException, RequestException, SAXException,
          TransformerException {
    // Create mock connection.
    HttpsURLConnection mockConnectionVersion = mock(HttpsURLConnection.class);
    // Put mock connection in map.
    MockUrlStreamHandler.mockConnections.put(
        new URL(repo + "/" + group_id + "/" + artifact_id + "/maven-metadata.xml"),
        mockConnectionVersion);

    // Create an authenticated artifact.
    MavenArtifact artifact = new MavenArtifact(repo, group_id, artifact_id, username, token);

    // Define some mock version parameters.
    String release = "0.4.5",
        latest = "0.4.5-SNAPSHOT",
        timestamp = "20211215.173200",
        buildNumber = "4";

    // Return mock version stream on method call.
    when(mockConnectionVersion.getInputStream()).thenReturn(getMockVersionStream(release, latest));

    // Check if expected version matches the parsed version.
    assertEquals(
        new ComparableVersion(release),
        artifact.getLatestReleaseVersion(),
        "The parsed latest release version should match the input version, but does not!");

    // Create another mock connection.
    HttpsURLConnection mockConnectionSnapshot = mock(HttpsURLConnection.class);
    // Put mock connection in map.
    MockUrlStreamHandler.mockConnections.put(
        new URL(repo + "/" + group_id + "/" + artifact_id + "/" + latest + "/maven-metadata.xml"),
        mockConnectionSnapshot);

    // Return respective mock version stream on method call.
    when(mockConnectionVersion.getInputStream()).thenReturn(getMockVersionStream(release, latest));
    when(mockConnectionSnapshot.getInputStream())
        .thenReturn(getMockSnapshotStream(timestamp, buildNumber));

    // Check if expected version matches the parsed version and store version.
    ComparableVersion latestVersion;
    assertEquals(
        new ComparableVersion(latest + "-" + timestamp + "-" + buildNumber),
        latestVersion = artifact.getLatestVersion(),
        "The parsed latest version should match the input version, but does not!");

    // Create yet another mock connection.
    HttpsURLConnection mockConnectionDownload = mock(HttpsURLConnection.class);

    // Define some mock file parameters.
    String classifier = "jar-with-dependencies", extension = "jar";

    // Put mock connection in map.
    MockUrlStreamHandler.mockConnections.put(
        new URL(
            repo
                + "/"
                + group_id
                + "/"
                + artifact_id
                + "/"
                + latest
                + "/"
                + artifact_id
                + "-"
                + latest.replace("-SNAPSHOT", "")
                + "-"
                + timestamp
                + "-"
                + buildNumber
                + "-"
                + classifier
                + "."
                + extension),
        mockConnectionDownload);

    // Assert that the correct exception is thrown (NullPointerException as path is null as no files
    // should be written during the tests).
    assertThrows(
        NullPointerException.class,
        () -> artifact.download(latestVersion, classifier, extension, null),
        "A " + NullPointerException.class.getName() + " should be thrown, but it is not!");

    // Build auth header for verification purposes.
    String authHeader =
        "Basic "
            + new String(
                Base64.encodeBase64((username + ":" + token).getBytes(StandardCharsets.UTF_8)));

    // Verify number of method calls.
    verify(
            mockConnectionVersion,
            times(2)
                .description(
                    "Two requests to this URL should be authenticated, but the authentication header has not been set twice (correctly)."))
        .setRequestProperty("Authorization", authHeader);
    verify(
            mockConnectionVersion,
            times(2)
                .description(
                    "The method `connect` should be called exactly twice on this connection, but was called a different amount of times."))
        .connect();
    verify(
            mockConnectionVersion,
            times(2)
                .description(
                    "The method `getResponseCode` should be called exactly twice on this connection, but was called a different amount of times."))
        .getResponseCode();
    verify(
            mockConnectionSnapshot,
            times(1)
                .description(
                    "The request to this URL should be authenticated, but the authentication header was not set (correctly)."))
        .setRequestProperty("Authorization", authHeader);
    verify(
            mockConnectionSnapshot,
            times(1)
                .description(
                    "The method `connect` should be called exactly once on this connection, but was called a different amount of times."))
        .connect();
    verify(
            mockConnectionSnapshot,
            times(1)
                .description(
                    "The method `getResponseCode` should be called exactly once on this connection, but was called a different amount of times."))
        .getResponseCode();
    verify(
            mockConnectionDownload,
            times(1)
                .description(
                    "The request to this URL should be authenticated, but the authentication header was not set (correctly)."))
        .setRequestProperty("Authorization", authHeader);
    verify(
            mockConnectionDownload,
            times(1)
                .description(
                    "The method `connect` should be called exactly once on this connection, but was called a different amount of times."))
        .connect();
    verify(
            mockConnectionDownload,
            times(1)
                .description(
                    "The method `getResponseCode` should be called exactly once on this connection, but was called a different amount of times."))
        .getResponseCode();
  }

  /**
   * A mock for an {@link URLStreamHandler} that returns predefined {@link URLConnection}s based on
   * the {@link URL}.
   *
   * @since 0.2.0
   */
  private static class MockUrlStreamHandler extends URLStreamHandler {

    /**
     * A {@link HashMap} that contains the respective {@link URLConnection}s for the corresponding
     * {@link URL}s.
     *
     * @since 0.2.0
     */
    private static final HashMap<URL, URLConnection> mockConnections = new HashMap<>();

    /**
     * Returns the respective {@link URLConnection} for the corresponding {@link URL}.
     *
     * @param url the {@link URL}.
     * @return the respective {@link URLConnection} for the corresponding {@link URL}.
     * @since 0.2.0
     */
    @Override
    protected URLConnection openConnection(URL url) {
      return mockConnections.get(url);
    }
  }

  /**
   * Returns a mock {@link InputStream} containing a release version and a latest version, similar
   * to what a request to {@code maven-metadata.xml} would return.
   *
   * @param releaseString the version that should be used as the latest release version.
   * @param latestString the version that should be used as the latest version.
   * @return a mock {@link InputStream} containing given versions.
   * @throws TransformerException if it is not possible to convert the {@link Document}.
   * @since 0.2.0
   */
  private static InputStream getMockVersionStream(String releaseString, String latestString)
      throws TransformerException {
    Document doc = builder.newDocument();

    Element metadata = doc.createElement("metadata");
    doc.appendChild(metadata);

    Element versioning = doc.createElement("versioning");
    metadata.appendChild(versioning);

    Element release = doc.createElement("release");
    release.setTextContent(releaseString);
    versioning.appendChild(release);

    Element latest = doc.createElement("latest");
    latest.setTextContent(latestString);
    versioning.appendChild(latest);
    return toInputStream(doc);
  }

  /**
   * Returns a mock {@link InputStream} containing a snapshot build, similar to what a request to
   * {@code maven-metadata.xml} in a {@code SNAPSHOT} directory would return.
   *
   * @param snapshotTimestamp the build timestamp that should be used for the latest snapshot build.
   * @param snapshotBuildNumber the build number that should be used for the latest snapshot build.
   * @return a mock {@link InputStream} containing given snapshot build.
   * @throws TransformerException if it is not possible to convert the {@link Document}.
   * @since 0.2.0
   */
  private static InputStream getMockSnapshotStream(
      String snapshotTimestamp, String snapshotBuildNumber) throws TransformerException {
    Document doc = builder.newDocument();

    Element metadata = doc.createElement("metadata");
    doc.appendChild(metadata);

    Element versioning = doc.createElement("versioning");
    metadata.appendChild(versioning);

    Element snapshot = doc.createElement("snapshot");
    versioning.appendChild(snapshot);

    Element timestamp = doc.createElement("timestamp");
    timestamp.setTextContent(snapshotTimestamp);
    snapshot.appendChild(timestamp);

    Element buildNumber = doc.createElement("buildNumber");
    buildNumber.setTextContent(snapshotBuildNumber);
    snapshot.appendChild(buildNumber);

    return toInputStream(doc);
  }

  /**
   * Converts a {@link Document} to an {@link InputStream}.
   *
   * @param doc the {@link Document} to convert.
   * @return the resulting {@link InputStream}.
   * @throws TransformerException if it is not possible to convert the {@link Document}.
   * @since 0.2.0
   */
  private static InputStream toInputStream(Document doc) throws TransformerException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Source xmlSource = new DOMSource(doc);
    Result outputTarget = new StreamResult(outputStream);
    TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);

    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
