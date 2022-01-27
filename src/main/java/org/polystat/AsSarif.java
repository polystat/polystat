/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2022 Polystat.org
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

import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

/**
 * Turn list of errors into a JSON report in SARIF format.
 *
 * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd01/sarif-v2.0-csprd01.html></a>
 * @since 1.0
 */
final class AsSarif implements Supplier<String> {

    /**
     * Errors.
     */
    private final Iterable<Result> errors;

    /**
     * Ctor.
     * @param errs Errors
     */
    AsSarif(final Iterable<Result> errs) {
        this.errors = errs;
    }

    @Override
    public String get() {
        final JsonArrayBuilder results = Json.createArrayBuilder();
        for (final Result res : this.errors) {
            if (res.failure().isPresent()) {
                results.add(
                    this.asJson(res.analysis(), "exception", res.failure().toString())
                );
            } else {
                for (final String error : res) {
                    results.add(this.asJson(res.analysis(), "message", error));
                }
            }
        }
        return Json
            .createObjectBuilder()
            .add("results", results.build()).build().toString();
    }

    /**
     * Convert to Json.
     * @param type Analysis type.
     * @param field Field.
     * @param value Vlue.
     * @return Json object.
     */
    private static JsonObject asJson(
        final Class<? extends Analysis> type,
        final String field,
        final String value
    ) {
        return Json.createObjectBuilder()
            .add("ruleId", type.getSimpleName())
            .add(field, value)
            .build();
    }
}
