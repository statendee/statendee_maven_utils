# Statendee Maven Utils 
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/statendee/statendee_maven_utils?color=orange) ![GitHub](https://img.shields.io/github/license/statendee/statendee_maven_utils) ![Build with Maven and Release](https://github.com/statendee/statendee_maven_utils/actions/workflows/maven_release.yml/badge.svg) ![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=maven.statendee.org&logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAFiAAABYgFfJ9BTAAACVklEQVR4nO2bP27iQBTGv6y2DxVtUtGyN1iWHik32Eg%2BQJICChrjxoXdJAdAyZ5gI7l3wg2oXbGtm4UTsHrSsELI630Wdt6z%2FD4JyTLDvG9%2BM3j%2BeOZiv9%2Bjy%2FrU6dIbAANgAD4r8HCWpsvZNQD6IPai96p5tbYXcAV%2FAfD16PYOwGPsRQtuPq0E4Aq%2FBnD5jyRPsRfdc%2FJq6zNgUVJ40t10OfvCyaitAL4z0txwMup8L2AAFHgQlQFQ4EFUBkCBB1EZAAUeRGUAFHgQlbr1gCT1ewBGAGgyQ%2FP79WQcbJuKp6oFJKlPs7zfAH4C8AG8AdgkqX%2FbVEw1AFzh%2FYKvaNr7nKQ%2Ba3ZXVSoAuGZfVPhjPTYRW0sL4NTuVZL6rEWOKtIC4JqZrld34M53gwZAgQdRGQAFHkRVaSicZ%2BGNW5Mfuls793Zm0R%2FMGxuuNil2C8iz8NYNUYdHt2mUdkdj9jwLa%2B%2BiPkIsAHkWUj%2F9XJJk6FpG68RtAZzJSGMTliZV50Ow7F2dWnW%2BFzAACjyIygAo8CAqA6DAg6gMgAIPojIACjyIygAo8CAqA6DAg6gMgAIPotICYM1Md1h53tUVmAvglZHmxxk%2B3hmFWk3GwQEUx88LJzALQH8wp8BPJUnIPOuAQpHcFpiyRdXT%2FOn6V0n6IPaiDSc2%2By%2FQH8wpaFBQUyvaz3Pui5HJOKBa%2FVZQMMp%2FdFT7dDZo6%2FYQnbY6%2Bu1D40dm8iwcuctNfzBnka6iJPUPB6H%2Bu0Fqupz1HIxt7EXcZ8lf2dFZBR5EZQAUeBBVtwEA%2BAMm6YQiRbLyjwAAAABJRU5ErkJggg%3D%3D&metadataUrl=https%3A%2F%2Fmaven.statendee.org%2Forg%2Fstatendee%2Fstatendee_maven_utils%2Fmaven-metadata.xml)

A library that can be used to obtain version information of maven artifacts and to download them.

## Usage

Add repository and dependency in `pom.xml`.

```xml
<repository>
    <id>maven_statendee</id>
    <name>statendee maven packages</name>
    <url>https://maven.statendee.org</url>
</repository>
```

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
    ComparableVersion latestVersion = artifact.getLatestVersion(); // 1.2.3-SNAPSHOT-20211208.214238-4

    artifact.download(releaseVersion, "jar-with-dependencies", "jar", "path/to/target-file.jar"); // Downloads version 1.2.3.
  }
}
```
