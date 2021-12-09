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

import org.junit.jupiter.api.Test;
import org.statendee.maven_utils.version.ComparableVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A class that contains {@link Test}s for to test the {@link ComparableVersion} class.
 *
 * @author jojomatik
 * @version 0.1.0
 * @since 0.1.0
 */
class ComparableVersionTest {

  /**
   * A {@link Test} that compares different versions and compares the results with the expected
   * values.
   *
   * @since 0.1.0
   */
  @Test
  void compareVersion() {
    // Different minor version.
    ComparableVersion version1 = new ComparableVersion("0.4.5");
    ComparableVersion version2 = new ComparableVersion("0.4.6");
    assertEquals(-1, version1.compareTo(version2));

    // SNAPSHOT and newer non-SNAPSHOT.
    version1 = new ComparableVersion("0.4.5-SNAPSHOT");
    version2 = new ComparableVersion("0.4.5");
    assertEquals(-1, version1.compareTo(version2));

    // non-SNAPSHOT and newer SNAPSHOT.
    version1 = new ComparableVersion("0.4.4");
    version2 = new ComparableVersion("0.4.5-SNAPSHOT");
    assertEquals(-1, version1.compareTo(version2));

    // different timestamp
    version1 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    version2 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182236");
    assertEquals(-1, version1.compareTo(version2));

    // different minor in SNAPSHOT versions
    version1 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    version2 = new ComparableVersion("0.4.6-SNAPSHOT-20211208.182236");
    assertEquals(-1, version1.compareTo(version2));

    // different minor in SNAPSHOT versions, with same timestamp (should never happen)
    version1 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    version2 = new ComparableVersion("0.4.6-SNAPSHOT-20211208.182235");
    assertEquals(-1, version1.compareTo(version2));

    // same version
    version1 = new ComparableVersion("0.4.5");
    version2 = new ComparableVersion("0.4.5");
    assertEquals(0, version1.compareTo(version2));

    // same SNAPSHOT version
    version1 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    version2 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    assertEquals(0, version1.compareTo(version2));

    // different build number (should be treated as synonymous, if timestamp equals)
    version1 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235");
    version2 = new ComparableVersion("0.4.5-SNAPSHOT-20211208.182235-1");
    assertEquals(0, version1.compareTo(version2));
  }
}
