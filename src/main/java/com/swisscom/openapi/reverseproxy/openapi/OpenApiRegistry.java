/*
 * Copyright 2024-2099 Swisscom (Schweiz) AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swisscom.openapi.reverseproxy.openapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ConditionalOnMissingBean(value = OpenApiRegistry.class, ignored = OpenApiRegistry.class)
@Component
public class OpenApiRegistry {

    private List<Listener> listeners = new ArrayList<>();

    private Map<String, List<OpenAPI>> registry = new HashMap<>();

    public OpenAPI get(String prefix) {
        return Optional.ofNullable(prefix)
            .map((pfx) -> pfx.trim())
            .map(this.registry::get)
            .filter((openApis) -> openApis.size() == 1)
            .map((openApis) -> openApis.get(0))
            .orElseThrow(() -> new IllegalArgumentException("No OpenAPI instance found for prefix " + prefix));
    }

    public List<OpenAPI> getUnprefixed() {
        return this.registry.get("");
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    protected void add(String prefix, OpenAPI openApi) {
        var key = Optional.ofNullable(prefix).map((pfx) -> pfx.trim()).orElse("");
        this.registry.computeIfAbsent((key), (k) -> new ArrayList<OpenAPI>()).add(openApi);
        this.listeners.forEach((listener) -> listener.onOpenApiRegistration(prefix, openApi));
    }

    public interface Listener {

        void onOpenApiRegistration(String prefix, OpenAPI openApi);

    }

}
