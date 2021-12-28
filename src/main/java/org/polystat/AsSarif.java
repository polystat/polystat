/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2021 Polystat.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.polystat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

/**
 * Turn list of errors into a JSON report in SARIF format.
 *
 * @since 1.0
 */
final class AsSarif implements Supplier<String> {

    /**
     * Errors.
     */
    private final Map<Analysis, List<String>> errors;

    /**
     * Ctor.
     * @param errs Errors
     */
    AsSarif(final Map<Analysis, List<String>> errs) {
        this.errors = Collections.unmodifiableMap(errs);
    }

    @Override
    public String get() {
        final JsonArrayBuilder results = Json.createArrayBuilder();
        for (final Map.Entry<Analysis, List<String>> ent : this.errors.entrySet()) {
            for (final String error : ent.getValue()) {
                results.add(
                    Json.createObjectBuilder()
                        .add("ruleId", ent.getKey().getClass().getSimpleName())
                        .add("message", error)
                );
            }
        }
        return Json.createObjectBuilder().add("results", results.build()).build().toString();
    }
}
