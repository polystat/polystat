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
import com.jcabi.xml.XMLDocument;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.cactoos.Func;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.eolang.parser.Spy;
import org.eolang.parser.Syntax;
import org.eolang.parser.Xsline;

/**
 * A collection of all EO files, which are accessible as XMIR elements,
 * by their object locators.
 *
 * @since 1.0
 * @todo #1:1h The current implementation is very primitive and doesn't
 *  support the case of fetching an object with a locator longer
 *  than just something inside \Phi. Try to make an object with children
 *  and fetch a child.
 * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
 */
public final class Program implements Func<String, XML> {

    /**
     * The directory with EO files.
     */
    private final Path sources;

    /**
     * The directory with .XML files and maybe other temp.
     */
    private final Path temp;

    /**
     * Ctor.
     * @param src The dir with .eo sources
     * @param tmp Temp dir with .xml files
     */
    public Program(final Path src, final Path tmp) {
        this.sources = src;
        this.temp = tmp;
    }

    @Override
    public XML apply(final String locator) throws Exception {
        final String[] parts = locator.split("\\.");
        final String name = parts[1];
        final Path xml = this.temp.resolve(String.format("%s.xml", name));
        if (!Files.exists(xml)) {
            new Syntax(
                name,
                new InputOf(this.sources.resolve(String.format("%s.eo", name))),
                new OutputTo(xml)
            ).parse();
            new Xsline(
                new XMLDocument(xml),
                new OutputTo(xml),
                new Spy.Verbose()
            ).pass();
        }
        XML obj = new XMLDocument(xml).nodes("/program/objects").get(0);
        for (int idx = 1; idx < parts.length; ++idx) {
            final List<XML> objs = obj.nodes(
                String.format("o[@name='%s']", parts[idx])
            );
            obj = objs.get(0);
        }
        return obj;
    }
}
