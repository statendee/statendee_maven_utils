# Statendee Maven Tools ![Java CI with Maven](https://github.com/statendee/statendee_maven_tools/workflows/Java%20CI%20with%20Maven/badge.svg)

A library that can be used to obtain version information of maven artifacts and to download them.

## Usage

Add dependency in `pom.xml`.

```xml
<dependency>
    <groupId>org.statendee</groupId>
    <artifactId>statendee_maven_utils</artifactId>
    <version>x.y.z</version>
</dependency>
```

Get latest (release) version and download it:

```java
import org.statendee.maven_utils.MavenArtifact;
import org.statendee.maven_utils.version.ComparableVersion;

public class Main {
  public static void main(String[] args) {
    String repo = args[0], groupId = args[1], artifactID = args[2], username = args[3], token = args[4];
    MavenArtifact artifact = new MavenArtifact(repo, groupId, artifactID, username, token);
    ComparableVersion releaseVersion = artifact.getLatestReleaseVersion(); // 1.2.3
    ComparableVersion latestVersion = artifact.getLatestVersion(); // 1.2.3-SNAPSHOT-20211208.2142384

    artifact.download(releaseVersion, "jar-with-dependencies", "jar", "path/to/target-file.jar"); // Downloads version 1.2.3.
  }
}
```
