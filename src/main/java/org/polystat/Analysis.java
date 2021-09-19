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

import com.jcabi.xml.XML;
import org.cactoos.Func;

/**
 * An interface every analysis method has to implement.
 *
 * @since 0.2
 */
public interface Analysis {

    /**
     * Analyse the specified object in the provided XMIR
     * and return the list of errors found.
     * @param xmir The XMIR
     * @param locator The locator of the object, for example "\\Phi.foo"
     * @return List of exeptions
     * @throws Exception If fails
     */
    Iterable<String> errors(Func<String, XML> xmir,
        String locator) throws Exception;

    /**
     * Name of the analysis.
     * @return Name of the analysis.
     */
    String name();
}
