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

import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.jcabi.manifests.Manifests;

/**
 * Turn list of errors into a JSON report in SARIF format.
 *
 * <a
 * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd01/sarif-v2.0-csprd01.html></a>
 * 
 * @since 1.0
 */
final class AsSarif implements Supplier<String> {

    /**
     * Errors.
     */
    private final Iterable<Result> errors;

    /**
     * Version of SARIF format specification.
     */
    private static final String SARIF_VERSION = "2.1.0";

    /**
     * SARIF JSON schema URL.
     */
    private static final String SARIF_SCHEMA = "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json";

    /**
     * Ctor.
     * 
     * @param errs Errors
     */
    AsSarif(final Iterable<Result> errs) {
        this.errors = errs;
    }

    // I couldn't find a better existing function for this
    /**
     * Joins an iterable of strings with a delimiter.
     * 
     * @param delim   delimiter
     * @param strings iterable of strings
     * @return strings joined with a delimiter
     */
    private static String joinStrings(String delim, Iterable<String> strings) {
        final StringJoiner result = new StringJoiner(delim);
        for (String str : strings) {
            result.add(str);
        }
        return result.toString();
    }

    /**
     * Constructs sarifLog object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127669></a>
     * 
     * @param runs JSON array of run objects
     * @return sarifLog JSON object
     */
    private static JsonObject sarifLogObject(final JsonArray runs) {
        return Json.createObjectBuilder()
                .add("version", SARIF_VERSION)
                .add("$schema", SARIF_SCHEMA)
                .add("runs", runs)
                .build();
    }

    /**
     * Extracts ruleId string from the Result object.
     * 
     * @param res Result object
     * @return ruleId string
     */
    private static String ruleId(Result res) {
        return String.join(
                "/",
                "Polystat",
                res.analysis().getSimpleName(),
                res.ruleId());
    }

    /**
     * Generates a reportingDescriptor object containing a single id field
     * which corresponds to ruleId.
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128027></a>
     * 
     * @param res Polystat result
     * @return JSON object reportingDescriptor
     */
    private static JsonObject ruleObject(Result res) {
        final String ruleId = ruleId(res);
        final JsonObject ruleObj = Json.createObjectBuilder().add("id", ruleId).build();
        return ruleObj;
    }

    /**
     * Generates a tool object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127720></a>
     * 
     * @param results Polystat results
     * @return JSON object tool
     */
    private static JsonObject toolObject(Iterable<Result> results) {
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
     * Generates a run object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127681></a>
     * 
     * @param tool        tool JSON object
     * @param results     results JSON array
     * @param invocations invocations JSON array
     * @return JSON object run
     */
    private static JsonObject runObject(final JsonObject tool, final JsonArray results, final JsonArray invocations) {
        return Json.createObjectBuilder()
                .add("tool", tool)
                .add("results", results)
                .add("invocations", invocations)
                .build();
    }

    /**
     * Generates a result object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127829></a>
     * 
     * @param res a Polystat result object
     * @return JSON object result
     */
    private static Optional<JsonObject> resultObject(Result res) {
        final Optional<JsonObject> result;
        if (!res.failure().isPresent()) {
            final String ruleId = ruleId(res);
            final JsonObjectBuilder resultObj = Json.createObjectBuilder();
            final String kind = res.iterator().hasNext() ? "fail" : "pass";
            final String level = res.iterator().hasNext() ? "error" : "none";
            final String messageText = res.iterator().hasNext() ? joinStrings("\n", res) : "No errors were found.";
            final JsonObject messageObj = Json.createObjectBuilder().add("text", messageText).build();
            resultObj.add("ruleId", ruleId);
            resultObj.add("level", level);
            resultObj.add("kind", kind);
            resultObj.add("message", messageObj);
            result = Optional.of(resultObj.build());
        } else {
            result = Optional.empty();
            // if the analyzer run failed, don't generate the result object
        }
        return result;
    }

    /**
     * Generates a results property of a run object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127698></a>
     * 
     * @param results Iterable of Polystat results
     * @return JSON array of result objects
     */
    private static JsonArray resultsArray(Iterable<Result> results) {
        final JsonArrayBuilder resultsArr = Json.createArrayBuilder();
        for (final Result res : results) {
            final Optional<JsonObject> resultObj = resultObject(res);
            if (resultObj.isPresent()) {
                resultsArr.add(resultObj.get());
            }
        }
        return resultsArr.build();
    }

    /**
     * Creates a message object to be used in notification object.
     * <a href=
     * "https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128090"></a>
     * 
     * @param res Polystat result object
     * @return JSON object message
     */
    private static JsonObject messageObjectForNotification(Result res) {
        final JsonObjectBuilder messageObj = Json.createObjectBuilder();
        final String messageText;
        final String messageSuccessPrefix = String.format("Analyzer \"%s\" completed successfully. ", ruleId(res));
        if (res.failure().isPresent()) {
            messageText = res.failure().get().getMessage();
        } else if (res.iterator().hasNext()) {
            messageText = messageSuccessPrefix + "Some errors were found.";
        } else {
            messageText = messageSuccessPrefix + "No errors were found";
        }
        messageObj.add("text", messageText);
        return messageObj.build();
    }

    /**
     * Generates a notification object
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10128085></a>
     * 
     * @param res Polystat result object
     * @return JSON object notification
     */
    private static JsonObject notificationObject(Result res) {
        final JsonObjectBuilder notificationObj = Json.createObjectBuilder();
        final JsonObject associatedRule = ruleObject(res);
        final JsonObject messageObj = messageObjectForNotification(res);
        if (res.failure().isPresent()) {
            final Throwable exception = res.failure().get();
            final JsonObject exceptionObj = Json.createObjectBuilder()
                    .add("kind", exception.getClass().getName())
                    .add("message", exception.getMessage())
                    .build();
            notificationObj.add("exception", exceptionObj);
            notificationObj.add("level", "error");
        }
        notificationObj.add("message", messageObj);
        notificationObj.add("associatedRule", associatedRule);
        return notificationObj.build();
    }

    /**
     * Generates the toolExecutionNotifications property of invocation object,
     * which is an array of notification objects.
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127779></a>
     * 
     * @param res Polystat result object
     * @return JSON array of notification objects
     */
    private static JsonArray toolExecutionNotificationsArray(final Result res) {
        final JsonArrayBuilder toolExecutionNotificationsArr = Json.createArrayBuilder();
        final JsonObject notificationObj = notificationObject(res);
        toolExecutionNotificationsArr.add(notificationObj);
        return toolExecutionNotificationsArr.build();
    }

    /**
     * Generates the invocations property of a run object,
     * which is an array of invocation objects.
     * <a
     * href=https://docs.oasis-open.org/sarif/sarif/v2.0/csprd02/sarif-v2.0-csprd02.html#_Toc10127686></a>
     * 
     * @param results Iterable of Polystat result objects
     * @return JSON array of invocation objects
     */
    private static JsonArray invocationsArray(Iterable<Result> results) {
        final JsonArrayBuilder invocationsArr = Json.createArrayBuilder();
        for (final Result res : results) {
            final JsonObjectBuilder invocationObj = Json.createObjectBuilder();
            final Boolean executionSuccessful = !res.failure().isPresent();
            final JsonArray toolExecutionNotificationsArr = toolExecutionNotificationsArray(res);
            invocationObj.add("toolExecutionNotifications", toolExecutionNotificationsArr);
            invocationObj.add("executionSuccessful", executionSuccessful);
            invocationsArr.add(invocationObj);
        }
        return invocationsArr.build();
    }

    @Override
    public String get() {
        final JsonArray runs = Json.createArrayBuilder().add(
                runObject(toolObject(errors), resultsArray(errors), invocationsArray(errors))).build();
        return sarifLogObject(runs).toString();
    }

}
