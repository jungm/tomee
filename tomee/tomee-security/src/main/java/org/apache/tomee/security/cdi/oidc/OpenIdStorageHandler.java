/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.cdi.oidc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class OpenIdStorageHandler {
    public static final String STATE_KEY = "STATE";
    public static final String NONCE_KEY = "NONCE";

    public abstract String get(HttpServletRequest request, HttpServletResponse response, String key);
    public abstract void set(HttpServletRequest request, HttpServletResponse response, String key, Object value);

    public String getStoredState(HttpServletRequest request, HttpServletResponse response) {
        return get(request, response, STATE_KEY);
    }

    public String createNewState(HttpServletRequest request, HttpServletResponse response) {
        String state = RandomStringUtils.random(10, true, true);
        set(request, response, STATE_KEY, state);

        return state;
    }

    public String getStoredNonce(HttpServletRequest request, HttpServletResponse response) {
        return get(request, response, NONCE_KEY);
    }

    public String createNewNonce(HttpServletRequest request, HttpServletResponse response) {
        String nonce = RandomStringUtils.random(10, true, true);
        set(request, response, NONCE_KEY, nonce);

        return nonce;
    }

    public static OpenIdStorageHandler get(boolean useSession) {
        return useSession ? new SessionBased() : new CookieBased();
    }

    private static class SessionBased extends OpenIdStorageHandler {
        @Override
        public String get(HttpServletRequest request, HttpServletResponse response, String key) {
            return (String) request.getSession().getAttribute(translateKey(key));
        }

        @Override
        public void set(HttpServletRequest request, HttpServletResponse response, String key, Object value) {
            request.getSession().setAttribute(translateKey(key), value);
        }

        private String translateKey(final String key) {
            return OpenIdStorageHandler.class.getName() + "." + key;
        }
    }

    private static class CookieBased extends OpenIdStorageHandler {
        @Override
        public String get(HttpServletRequest request, HttpServletResponse response, String key) {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public void set(HttpServletRequest request, HttpServletResponse response, String key, Object value) {
            throw new UnsupportedOperationException(); // TODO
        }
    }
}
