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
@SuppressWarnings("PMD.TooManyMethods")
final class AsSarif implements Supplier<String> {

    /**
     * Version of SARIF format specification.
     */
    private static final String SARIF_VERSION = "2.1.0";

    // @checkstyle LineLengthCheck (5 lines)
    /**
     * SARIF JSON schema URL.
     */
    private static final String SARIF_SCHEMA =
        "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json";

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
        final JsonArray runs = Json.createArrayBuilder().add(
            runObject(
                toolObject(),
                resultsArray(this.errors),
                invocationsArray(this.errors)
            )
        ).build();
        return sarifLogObject(runs).toString();
    }

    // I couldn't find a better existing function for this
    /**
     * Joins an iterable of strings with a delimiter.
     * @param delim A delimiter
     * @param strings An iterable of strings
     * @return Strings joined with the delimiter
     */
    private static String joinStrings(final String delim, final Iterable<String> strings) {
        final StringJoiner result = new StringJoiner(delim);
        for (final String str : strings) {
            result.add(str);
        }
        return result.toString();
    }

    /**
     * Constructs sarifLog object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127669></a>
     * @param runs JSON array of run objects
     * @return JSON object sarifLog
     */
    private static JsonObject sarifLogObject(final JsonArray runs) {
        return Json.createObjectBuilder()
            .add("version", AsSarif.SARIF_VERSION)
            .add("$schema", AsSarif.SARIF_SCHEMA)
            .add("runs", runs)
            .build();
    }

    /**
     * Extracts ruleId string from the Result object.
     * @param res Result object
     * @return The ruleId string
     */
    private static String ruleId(final Result res) {
        return String.join(
            "/",
            res.analysis().getSimpleName(),
            res.ruleId()
        );
    }

    /**
     * Generates a reportingDescriptor object containing a single id field
     * which corresponds to ruleId.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128027></a>
     * @param res Polystat result
     * @return JSON object reportingDescriptor
     */
    private static JsonObject ruleObject(final Result res) {
        return Json.createObjectBuilder().add("id", ruleId(res)).build();
    }

    /**
     * Generates a tool object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127720></a>
     * @return JSON object tool
     */
    private static JsonObject toolObject() {
        final JsonObjectBuilder driver = Json.createObjectBuilder()
            .add("name", "Polystat")
            .add("informationUri", "https://www.polystat.org/")
            .add("semanticVersion", Manifests.read("Polystat-Version"));
        final JsonObject tool = Json.createObjectBuilder()
            .add("driver", driver)
            .build();
        return tool;
    }

    /**
     * Generates a run object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127681></a>
     * @param tool JSON object tool
     * @param results JSON array results
     * @param invocations JSON array invocations
     * @return JSON object run
     */
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

    /**
     * Generates a result object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127829></a>
     * @param res Polystat result object
     * @return JSON object result
     */
    private static Optional<JsonObject> resultObject(final Result res) {
        final Optional<JsonObject> result;
        if (res.failure().isPresent()) {
            result = Optional.empty();
        } else {
            final JsonObjectBuilder resultobj = Json.createObjectBuilder();
            final String kind;
            final String level;
            final String text;
            if (res.iterator().hasNext()) {
                kind = "fail";
                level = AsSarif.LEVEL_ERROR;
                text = joinStrings("\n", res);
            } else {
                kind = "pass";
                level = "none";
                text = "No errors were found.";
            }
            final JsonObject message =
                Json.createObjectBuilder().add(AsSarif.PROPERTY_TEXT, text).build();
            resultobj.add("ruleId", ruleId(res));
            resultobj.add(AsSarif.PROPERTY_LEVEL, level);
            resultobj.add(AsSarif.PROPERTY_KIND, kind);
            resultobj.add(AsSarif.PROPERTY_MESSAGE, message);
            result = Optional.of(resultobj.build());
        }
        return result;
    }

    /**
     * Generates a results property of a run object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127698></a>
     * @param results Iterable of Polystat results
     * @return JSON array of result objects
     */
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

    /**
     * Creates a message object to be used in notification object.
     * <a href="https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128090"></a>
     * @param res Polystat result object
     * @return JSON object message
     */
    private static JsonObject messageObjectForNotification(final Result res) {
        final JsonObjectBuilder message = Json.createObjectBuilder();
        final String text;
        final String prefix =
            String.format("Analyzer \"%s\" completed successfully. ", ruleId(res));
        if (res.failure().isPresent()) {
            text = res.failure().get().getMessage();
        } else if (res.iterator().hasNext()) {
            text = String.format("%sSome errors were found.", prefix);
        } else {
            text = String.format("%sNo errors were found", prefix);
        }
        message.add(AsSarif.PROPERTY_TEXT, text);
        return message.build();
    }

    /**
     * Generates a notification object.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128085></a>
     * @param res Polystat result object
     * @return JSON object notification
     */
    private static JsonObject notificationObject(final Result res) {
        final JsonObjectBuilder notification = Json.createObjectBuilder();
        final JsonObject rule = ruleObject(res);
        final JsonObject message = messageObjectForNotification(res);
        if (res.failure().isPresent()) {
            final Throwable exc = res.failure().get();
            final JsonObject exception = Json.createObjectBuilder()
                .add(AsSarif.PROPERTY_KIND, exc.getClass().getName())
                .add(AsSarif.PROPERTY_MESSAGE, exc.getMessage())
                .build();
            notification.add("exception", exception);
            notification.add(AsSarif.PROPERTY_LEVEL, AsSarif.LEVEL_ERROR);
        }
        notification.add(AsSarif.PROPERTY_MESSAGE, message);
        notification.add("associatedRule", rule);
        return notification.build();
    }

    /**
     * Generates the toolExecutionNotifications property of invocation object,
     * which is an array of notification objects.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127779></a>
     * @param res Polystat result object
     * @return JSON array of notification objects
     */
    private static JsonArray toolExecutionNotificationsArray(final Result res) {
        final JsonArrayBuilder notifs = Json.createArrayBuilder();
        final JsonObject notification = notificationObject(res);
        notifs.add(notification);
        return notifs.build();
    }

    /**
     * Generates the invocations property of a run object,
     * which is an array of invocation objects.
     * <a href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127686></a>
     * @param results Iterable of Polystat result objects
     * @return JSON array of invocation objects
     */
    private static JsonArray invocationsArray(final Iterable<Result> results) {
        final JsonArrayBuilder invocations = Json.createArrayBuilder();
        for (final Result res : results) {
            final JsonObjectBuilder invocation = Json.createObjectBuilder();
            final Boolean successful = !res.failure().isPresent();
            final JsonArray notifs = toolExecutionNotificationsArray(res);
            invocation.add("toolExecutionNotifications", notifs);
            invocation.add("executionSuccessful", successful);
            invocations.add(invocation);
        }
        return invocations.build();
    }

}
