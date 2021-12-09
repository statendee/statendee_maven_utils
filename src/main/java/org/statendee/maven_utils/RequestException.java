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

/**
 * An {@link Exception} that is thrown if a http server returned a response code 4xx or 5xx.
 *
 * @author jojomatik
 * @version 0.1.0
 * @since 0.1.0
 */
public class RequestException extends Exception {

  /**
   * The response code sent by the http server.
   *
   * @since 0.1.0
   */
  public final int responseCode;

  /**
   * Creates an instance of {@link RequestException} based on the response code.
   *
   * @param responseCode the response code the http server returned.
   * @since 0.1.0
   */
  public RequestException(int responseCode) {
    super("Server returned response code " + responseCode + "!");
    this.responseCode = responseCode;
  }

  /**
   * Returns the according text message for some common response codes.
   *
   * <p>Currently supported: {@code 401 (Unauthorized), 403 (Forbidden), 404 (Not Found)}
   *
   * @return the according text message for some common response codes.
   * @since 0.1.0
   */
  public String getMessage() {
    switch (responseCode) {
      case 401:
        return "Unauthorized";
      case 403:
        return "Forbidden";
      case 404:
        return "Not Found";
      default:
        return null;
    }
  }
}
