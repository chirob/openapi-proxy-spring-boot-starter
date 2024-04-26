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

package com.swisscom.openapi.reverseproxy.config;

import java.util.List;
import java.util.Optional;

import com.swisscom.openapi.reverseproxy.util.SpelExpressionEvaluator;

public abstract class SpelEvaluatedProxyOptions implements ProxyOptions {

    private SpelExpressionEvaluator evaluator;

    public final String getSpecification() {
        return this.evaluator.evaluate(specification());
    }

    public final String getPrefix() {
        return this.evaluator.evaluate(prefix());
    }

    public final String getTarget() {
        return this.evaluator.evaluate(target());
    }

    public final List<String> getIgnoredRequestHeaders() {
        return Optional.ofNullable(ignoredRequestHeaders())
            .map((headers) -> headers.stream().map(this.evaluator::evaluate).toList())
            .orElse(List.of());
    }

    public final List<String> getIgnoredResponseHeaders() {
        return Optional.ofNullable(ignoredResponseHeaders())
            .map((headers) -> headers.stream().map(this.evaluator::evaluate).toList())
            .orElse(List.of());
    }

    protected SpelEvaluatedProxyOptions(SpelExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    protected abstract String specification();

    protected abstract String prefix();

    protected abstract String target();

    protected abstract List<String> ignoredRequestHeaders();

    protected abstract List<String> ignoredResponseHeaders();

}
