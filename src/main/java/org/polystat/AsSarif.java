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

import com.jcabi.manifests.Manifests;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Turn list of errors into a JSON report in SARIF format.
 * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd01/sarif-v2.0-csprd01.html></a>
 * @since 1.0
 */
final class AsSarif implements Supplier<String> {

    /**
     * SARIF property "kind".
     */
    private static final String PROPERTY_KIND = "kind";

    /**
     * SARIF property "text".
     */
    private static final String PROPERTY_TEXT = "text";

    /**
     * SARIF property "level".
     */
    private static final String PROPERTY_LEVEL = "level";

    /**
     * SARIF property "message".
     */
    private static final String PROPERTY_MESSAGE = "message";

    /**
     * SARIF severity level "error".
     */
    private static final String LEVEL_ERROR = "error";

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
        return sarifLogObject(
            Json.createArrayBuilder().add(
                runObject(
                    toolObject(),
                    resultsArray(this.errors),
                    invocationsArray(this.errors)
                )
            ).build()
        ).toString();
    }

    private static String joinStrings(final String delim, final Iterable<String> strings) {
        final StringJoiner result = new StringJoiner(delim);
        for (final String str : strings) {
            result.add(str);
        }
        return result.toString();
    }

    private static JsonObject sarifLogObject(final JsonArray runs) {
        // @checkstyle LineLengthCheck (5 lines)
        return Json.createObjectBuilder()
            .add("version", "2.1.0")
            .add("$schema", "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json")
            .add("runs", runs)
            .build();
    }

    private static String ruleId(final Result res) {
        return String.join(
            "/",
            res.analysis().getSimpleName(),
            res.ruleId()
        );
    }

    private static JsonObject ruleObject(final Result res) {
        return Json.createObjectBuilder().add("id", ruleId(res)).build();
    }

    private static JsonObject toolObject() {
        return Json.createObjectBuilder().add(
            "driver",
            Json.createObjectBuilder()
                .add("name", "Polystat")
                .add("informationUri", "https://www.polystat.org/")
                .add("semanticVersion", Manifests.read("Polystat-Version"))
        ).build();
    }

    private static JsonObject runObject(
        final JsonObject tool,
        final JsonArray results,
        final JsonArray invocations
    ) {
        return Json.createObjectBuilder()
            .add("tool", tool)
            .add("results", results)
            .add("invocations", invocations)
            .build();
    }

    private static Optional<JsonObject> resultObject(final Result res) {
        final Optional<JsonObject> result;
        if (res.failure().isPresent()) {
            result = Optional.empty();
        } else {
            final String kind;
            final String level;
            final String text;
            if (res.iterator().hasNext()) {
                kind = "fail";
                level = AsSarif.LEVEL_ERROR;
                text = joinStrings(System.lineSeparator(), res);
            } else {
                kind = "pass";
                level = "none";
                text = "No errors were found.";
            }
            final JsonObject message =
                Json.createObjectBuilder().add(AsSarif.PROPERTY_TEXT, text).build();
            final JsonObjectBuilder resultobj = Json.createObjectBuilder();
            resultobj.add("ruleId", ruleId(res));
            resultobj.add(AsSarif.PROPERTY_LEVEL, level);
            resultobj.add(AsSarif.PROPERTY_KIND, kind);
            resultobj.add(AsSarif.PROPERTY_MESSAGE, message);
            result = Optional.of(resultobj.build());
        }
        return result;
    }

    private static JsonArray resultsArray(final Iterable<Result> results) {
        final JsonArrayBuilder resultsarr = Json.createArrayBuilder();
        for (final Result res : results) {
            final Optional<JsonObject> resultobj = resultObject(res);
            if (resultobj.isPresent()) {
                resultsarr.add(resultobj.get());
            }
        }
        return resultsarr.build();
    }

    private static JsonObject messageObjectForNotification(final Result res) {
        final String prefix =
            String.format("Analyzer \"%s\" completed successfully. ", ruleId(res));
        final String text;
        if (res.failure().isPresent()) {
            text = res.failure().get().getMessage();
        } else if (res.iterator().hasNext()) {
            text = String.format("%sSome errors were found.", prefix);
        } else {
            text = String.format("%sNo errors were found", prefix);
        }
        return Json.createObjectBuilder().add(AsSarif.PROPERTY_TEXT, text).build();
    }

    private static JsonObject notificationObject(final Result res) {
        final JsonObjectBuilder notification = Json.createObjectBuilder();
        if (res.failure().isPresent()) {
            final Throwable exc = res.failure().get();
            notification.add(
                "exception",
                Json.createObjectBuilder()
                    .add(AsSarif.PROPERTY_KIND, exc.getClass().getName())
                    .add(AsSarif.PROPERTY_MESSAGE, exc.getMessage())
            );
            notification.add(AsSarif.PROPERTY_LEVEL, AsSarif.LEVEL_ERROR);
        }
        notification.add(AsSarif.PROPERTY_MESSAGE, messageObjectForNotification(res));
        notification.add("associatedRule", ruleObject(res));
        return notification.build();
    }

    private static JsonArray toolExecutionNotificationsArray(final Result res) {
        return Json.createArrayBuilder().add(notificationObject(res)).build();
    }

    private static JsonArray invocationsArray(final Iterable<Result> results) {
        final JsonArrayBuilder invocations = Json.createArrayBuilder();
        for (final Result res : results) {
            final JsonObjectBuilder invocation = Json.createObjectBuilder();
            invocation.add(
                "toolExecutionNotifications",
                toolExecutionNotificationsArray(res)
            );
            invocation.add("executionSuccessful", !res.failure().isPresent());
            invocations.add(invocation);
        }
        return invocations.build();
    }
}
