/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import com.jcabi.xml.XML;
import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.cactoos.list.ListOf;
import org.polystat.odin.interop.java.EOOdinAnalyzer;
import org.polystat.odin.interop.java.OdinAnalysisResultInterop;

/**
 * The implementation of analysis via odin (object dependency inspector).
 * @see <a href="https://github.com/polystat/odin">Github</a>
 * @since 0.3
 */
public final class AnOdin implements Analysis {

    /**
     * Ctor.
     */
    public AnOdin() {
        // nothing
    }

    @Override
    public Iterable<Result> errors(final Func<String, XML> xmir,
        final String locator) throws Exception {
        return new EOOdinAnalyzer.EOOdinXmirAnalyzer()
            .analyze(AnOdin.hierarchy(xmir, xmir.apply(locator)))
            .stream()
            .map(AnOdin::extracted)
            .collect(Collectors.toList());
    }

    private static Result extracted(final OdinAnalysisResultInterop res) {
        final Result result;
        if (res.analyzerFailure().isPresent()) {
            result = new Result.Failed(
                AnOdin.class,
                res.analyzerFailure().get(),
                res.ruleId()
            );
        } else if (res.detectedDefect().isPresent()) {
            result = new Result.Completed(
                AnOdin.class,
                new ListOf<>(res.detectedDefect().get()),
                res.ruleId()
            );
        } else {
            result = new Result.Completed(
                AnOdin.class,
                new ListOf<>(),
                res.ruleId()
            );
        }
        return result;
    }

    private static String hierarchy(final Func<String, XML> xmir,
        final XML xml) throws Exception {
        return String.format(
            "%s%n%s%n%s",
            "<objects>",
            AnOdin.decoratees(xmir, xml),
            "</objects>"
        );
    }

    private static String decoratees(final Func<String, XML> xmir,
        final XML xml) throws Exception {
        String result = xml.toString();
        for (final String decoratee : xml.xpath("o[@name='@']/@base")) {
            if (decoratee.charAt(0) != '.') {
                final List<String> split = new ListOf<>(decoratee.split("\\."));
                final String name = split.get(split.size() - 1);
                result = String.format(
                    "%s%s",
                    xmir.apply(String.format("\\Phi.%s", name)),
                    result
                );
            }
        }
        return result;
    }
}
