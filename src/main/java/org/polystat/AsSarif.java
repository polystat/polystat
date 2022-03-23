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


import java.util.StringJoiner;
import java.util.function.Supplier;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
     * Ctor.
     * 
     * @param errs Errors
     */
    AsSarif(final Iterable<Result> errs) {
        this.errors = errs;
    }

    private static JsonObject sarifLog(final JsonArray runs) {
        return Json.createObjectBuilder()
                .add("version", SARIF_VERSION)
                .add("runs", runs)
                .build();
    }

    private static JsonObject extractRuleObj(Result res) {
        final String ruleId = String.join(
                "/", 
                "Polystat",
                res.analysis().getSimpleName(), 
                res.ruleId()
            );
        final JsonObject ruleObj = Json.createObjectBuilder().add("id", ruleId).build();
        return ruleObj;
    }

    private static JsonObject tool(Iterable<Result> results) {
        JsonObjectBuilder driver = Json.createObjectBuilder()
            .add("name", "Polystat");
        JsonArrayBuilder rulesArr = Json.createArrayBuilder();

        for (final Result res : results) {
            final JsonObject ruleObj = extractRuleObj(res);
            rulesArr.add(ruleObj);
        }
        driver.add("rules", rulesArr);
        JsonObject tool = Json.createObjectBuilder()
            .add("driver", driver)
            .build();
        return tool;
    }

    private static JsonObject runObj(final JsonObject tool, final JsonArray results, final JsonArray invocations) {
        return Json.createObjectBuilder()
            .add("tool", tool)
            .add("results", results)
            .add("invocations", invocations)
            .build();
    }

    // I couldn't find a better existing function for this
    private static String joinStrings(String delim, Iterable<String> strings) {
        final StringJoiner result = new StringJoiner(delim);
        for (String str: strings) {
            result.add(str);
        }
        return result.toString();
    }

    private static JsonArray results(Iterable<Result> results) {
        JsonArrayBuilder resultsArr = Json.createArrayBuilder();
        for (final Result res : results) {
            final JsonObjectBuilder resultObj = Json.createObjectBuilder();
            if (!res.failure().isPresent()) {
                final JsonObject ruleObj = extractRuleObj(res);
                final String kind = res.iterator().hasNext() ? "fail" : "pass";
                final String level = res.iterator().hasNext() ? "error" : "none";
                final String messageText = res.iterator().hasNext() ? joinStrings("\n", res): "No errors were found.";
                final JsonObject messageObj = Json.createObjectBuilder().add("text", messageText).build();
                resultObj.add("rule", ruleObj);
                resultObj.add("level", level);
                resultObj.add("kind", kind);   
                resultObj.add("message", messageObj); 
                resultsArr.add(resultObj);
            } else {
                // if the analyzer run failed, don't include it into results
            }
        }
        return resultsArr.build();
    }

    private static JsonArray invocations(Iterable<Result> results) {
        JsonArrayBuilder invocationsArr = Json.createArrayBuilder();
        for (final Result res : results) {
            final JsonObjectBuilder invocationObj = Json.createObjectBuilder();
            final Boolean executionSuccessful = !res.failure().isPresent();
            if (res.failure().isPresent()) {
                final JsonArrayBuilder toolExecutionNotificationsArr = Json.createArrayBuilder();
                final JsonObjectBuilder notificationObj = Json.createObjectBuilder();
                final Throwable exception = res.failure().get();
                final JsonObject exceptionObj = Json.createObjectBuilder()
                    .add("kind", exception.getClass().getName())
                    .add("message", exception.getMessage())
                    .build();
                notificationObj.add("exception", exceptionObj);
                notificationObj.add("level", "error");
                toolExecutionNotificationsArr.add(notificationObj);
                invocationObj.add("toolExecutionNotifications", toolExecutionNotificationsArr);
            }
            invocationObj.add("executionSuccessful", executionSuccessful);
            invocationsArr.add(invocationObj);
        }
        return invocationsArr.build();
    }


    @Override
    public String get() {
        final JsonArray runs = Json.createArrayBuilder().add(
            runObj(tool(errors), results(errors), invocations(errors))
        ).build();
        return sarifLog(runs).toString();
    }

}
