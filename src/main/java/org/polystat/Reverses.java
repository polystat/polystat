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

import com.jcabi.xml.ClasspathSources;
import com.jcabi.xml.XML;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import org.cactoos.Func;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;

/**
 * Finding bugs via reverses.
 *
 * @since 1.0
 */
public final class Reverses {

    /**
     * The XMIR of the code.
     */
    private final UncheckedFunc<String, XML> xmir;

    /**
     * Ctor.
     * @param src The XMIR
     */
    public Reverses(final Func<String, XML> src) {
        this.xmir = new UncheckedFunc<>(src);
    }

    /**
     * Find all errors.
     * @param locator The locator of the object to analyze
     * @return List of errors
     * @throws IOException If fails
     */
    public Collection<String> errors(final String locator) throws IOException {
        final Collection<String> bugs = new LinkedList<>();
        final XML obj = this.xmir.apply(locator);
        final XSL xsl = new XSLDocument(
            new TextOf(new ResourceOf("org/polystat/reverses.xsl")).asString()
        ).with(new ClasspathSources());
        xsl.with("out", "\\perp").transform(obj);
        if (obj.nodes("o").size() > 2) {
            bugs.add("Too many attributes in the object");
        }
        return bugs;
    }

}
