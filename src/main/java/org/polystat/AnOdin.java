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

import com.jcabi.xml.XML;
import java.util.List;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.cactoos.list.ListOf;
import org.polystat.odin.interop.java.EOOdinAnalyzer;
import org.polystat.odin.interop.java.OdinAnalysisErrorInterop;

/**
 * The implementation of analysis via odin (object dependency inspector).
 *
 * @see <a href="https://github.com/polystat/odin">Github</a>
 * @since 0.3
 */
public final class AnOdin implements Analysis {

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Iterable<String> errors(final Func<String, XML> xmir,
        final String locator) throws Exception {
        final XML xml = xmir.apply(locator);
        final String str = getObjectsHierarchy(xmir, xml);
        Iterable<String> result;
        try {
            result = new EOOdinAnalyzer.EOOdinXmirAnalyzer()
                .analyze(str).stream()
                .map(OdinAnalysisErrorInterop::message)
                .collect(Collectors.toList());
        // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            result = new ListOf<>(
                String.format("Odin is not able to analyze the code, due to:%n%s", ex.getMessage())
            );
        }
        return result;
    }

    /**
     * Resolves object hierarchy for the give object represented in XMIR and
     * returns a well-formed XML.
     * @param xmir Function to retrieve XMIR by locator
     * @param xml XMIR of object to get hierarchy for
     * @return Well-formed XML containing objects that form a hierarchy in XMIR
     * @throws Exception on errors
     */
    private static String getObjectsHierarchy(final Func<String, XML> xmir,
        final XML xml) throws Exception {
        return String.format(
            "%s%n%s%n%s",
            "<objects>",
            resolveObjectHierarchy(xmir, xml),
            "</objects>"
        );
    }

    /**
     * Recursively resolves the decoratees for the given decorator object
     * represented as XMIR.
     * @param xmir Function to retrieve XMIR by locator
     * @param xml XMIR that represents an object
     * @return Concatenated XML string containing the whole object hierarchy
     * @throws Exception on errors
     */
    private static String resolveObjectHierarchy(final Func<String, XML> xmir,
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
