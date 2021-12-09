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
package org.statendee.maven_utils.version;

/**
 * A custom implementation of {@link org.apache.maven.artifact.versioning.ComparableVersion} that
 * simplifies the analysis and comparison of {@code SNAPSHOT}-versions.
 *
 * @author jojomatik
 * @version 0.1.0
 * @since 0.1.0
 */
public class ComparableVersion extends org.apache.maven.artifact.versioning.ComparableVersion {

  /**
   * Creates an instance of {@link ComparableVersion} based on the version-{@link String}.
   *
   * @param version the version string.
   * @since 0.1.0
   */
  public ComparableVersion(String version) {
    super(version);
  }

  /**
   * Returns {@code true} if the version is a {@code SNAPSHOT}-version.
   *
   * <p>Checks if the second part of the version-{@link String} (split by hyphens) {@code
   * equals("SNAPSHOT")}
   *
   * @return {@code true} if the version is a {@code SNAPSHOT}-version.
   * @since 0.1.0
   */
  public boolean isSnapshot() {
    String[] splitVersion = getSplitVersion();

    if (splitVersion.length == 1) return false;
    return getSplitVersion()[1].equals("SNAPSHOT");
  }

  /**
   * Returns the timestamp of a {@code SNAPSHOT}-build if the version is a {@code SNAPSHOT}-version.
   *
   * <p>If the version is not a {@code SNAPSHOT}-version or the original version-{@link String} did
   * not include a timestamp, an {@link Exception} is thrown.
   *
   * @return the timestamp of a {@code SNAPSHOT}-build.
   * @throws NoSnapshotVersionException if the version is no {@code SNAPSHOT}-version.
   * @throws NoTimestampException if the version has no build timestamp.
   * @since 0.1.0
   */
  public String getTimestamp() throws NoSnapshotVersionException, NoTimestampException {
    if (!isSnapshot()) throw new NoSnapshotVersionException(this);
    String[] splitVersion = getSplitVersion();
    if (splitVersion.length >= 3) {
      return splitVersion[2];
    }
    throw new NoTimestampException(this);
  }

  /**
   * Returns the version-{@link String} split by hyphens.
   *
   * @return the version-{@link String} split by hyphens.
   * @since 0.1.0
   */
  private String[] getSplitVersion() {
    return this.toString().split("-");
  }

  /**
   * Returns the version without build info, such as the timestamp as well as the build number.
   *
   * <p>Returns itself for non-{@code SNAPSHOT}-versions and everything before the second hyphen for
   * {@code SNAPSHOT}-versions.
   *
   * @return the version without build info.
   * @since 0.1.0
   */
  public ComparableVersion getVersionWithoutBuildInfo() {
    return this.isSnapshot() ? new ComparableVersion(getSplitVersion()[0] + "-SNAPSHOT") : this;
  }

  /**
   * Compares two {@link ComparableVersion}s.
   *
   * <p>If both versions are {@code SNAPSHOT}-versions and their major, minor and patch versions
   * match, as well as the timestamp, they are assumed to equal each other. This is done to match
   * versions independent of the presence of the build number at the end of the version-{@link
   * String}, which should match each other anyway given the same the timestamp.
   *
   * @param compare the version to compare this {@link ComparableVersion} to.
   * @return {@code -1}, if this {@link ComparableVersion} is lower than {@code compare}, {@code 0}
   *     if they equal each other and {@code 1} if this {@link ComparableVersion} is higher.
   * @since 0.1.0
   */
  public int compareTo(ComparableVersion compare) {
    if (this.isSnapshot() && compare.isSnapshot()) {
      ComparableVersion versionA = new ComparableVersion(this.getSplitVersion()[0]);
      ComparableVersion versionB = new ComparableVersion(compare.getSplitVersion()[0]);
      try {
        if (versionA.compareTo(versionB) == 0 && getTimestamp().equals(compare.getTimestamp()))
          return 0;
      } catch (NoTimestampException | NoSnapshotVersionException ignored) {
      }
    }

    return super.compareTo(compare);
  }
}
