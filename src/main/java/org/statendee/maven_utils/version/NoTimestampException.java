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
 * An {@link Exception} that is thrown if a {@link ComparableVersion} is a {@code SNAPSHOT}-version
 * but has no build timestamp.
 *
 * @author jojomatik
 * @version 0.1.0
 * @since 0.1.0
 */
public class NoTimestampException extends Exception {

  /**
   * Creates an instance of {@link NoTimestampException} based on the version that caused it.
   *
   * @param version the version that caused the {@link NoTimestampException}.
   * @since 0.1.0
   */
  public NoTimestampException(ComparableVersion version) {
    super("Version " + version + " has no build timestamp!");
  }
}
