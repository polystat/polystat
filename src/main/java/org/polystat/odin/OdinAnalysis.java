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

package org.polystat.odin;

import com.jcabi.xml.XML;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.eolang.parser.XMIR;
import org.polystat.Analysis;
import org.polystat.SourceCode;
import org.polystat.odin.interop.java.EOOdinAnalyzer;
import org.polystat.odin.interop.java.OdinAnalysisErrorInterop;

/**
 * The implementation of analysis via odin (object dependency inspector).
 *
 * @see <a href="https://github.com/polystat/odin">Github</a>
 * @since 0.3
 */
public final class OdinAnalysis implements Analysis {
    /**
     * Odin analyzer that performs analysis.
     */
    private final EOOdinAnalyzer analyzer;
    private final SourceCode src;

    /**
     * Ctor.
     */
    public OdinAnalysis(SourceCode src) {
        this.src = src;
        this.analyzer = new EOOdinAnalyzer.EOOdinAnalyzerImpl();
    }

    @Override
    public Iterable<String> errors(String locator) throws Exception {
        return this.analyzer.analyzeSourceCode(src.repr(locator)).stream()
            .map(OdinAnalysisErrorInterop::message)
            .collect(Collectors.toList());
    }

}
