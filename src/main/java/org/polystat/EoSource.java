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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.text.TextOf;
import org.eolang.parser.Spy;
import org.eolang.parser.Syntax;
import org.eolang.parser.Xsline;

/**
 * EO programs may come from different places: Internet, file system, XML, Json.
 * This class is used to unify all these various sources with a single interface.
 * @since 1.0
 */
public class EoSource {

    /**
     * The destination for EO files.
     */
    private final Path source;

    /**
     * The temporary directory with .XML files or some other stuff.
     */
    private final Path temp;

    /**
     * Ctor.
     *
     * @param src The dir with .eo sources
     * @param tmp Temp dir with .xml files
     */
    public EoSource(final Path src, final Path tmp) {
        this.source = src;
        this.temp = tmp;
    }

    /**
     * Returns XMIR of an EO object.
     * @param locator Used to uniquely identify EO object in the destination.
     * @return XMIR representation of the EO object.
     * @throws IOException When IO fails.
     */
    XML xmir(final String locator) throws IOException {
        final String name = getFilenameFromLocator(locator);
        final Path xml = this.temp.resolve(String.format("%s.xml", name));
        if (!Files.exists(xml)) {
            new Syntax(
                name,
                new InputOf(this.source.resolve(eoFilename(name))),
                new OutputTo(xml)
            ).parse();
            new Xsline(
                new XMLDocument(xml),
                new OutputTo(xml),
                new Spy.Verbose()
            ).pass();
        }
        return new XMLDocument(xml);
    }

    /**
     * Returns the source code for a given EO object.
     * @param locator Used to uniquely identify EO object in the destination.
     * @return EO source code of the object.
     * @throws IOException When IO fails.
     */
    String sourceCode(final String locator) throws IOException {
        return new TextOf(
            new InputOf(
                this.source.resolve(
                    eoFilename(getFilenameFromLocator(locator))
                )
            )
        ).asString();
    }

    /**
     * Retrieves the file name from the locator.
     * @param locator Carries the filename from which to get the source code.
     * @return The filename containing the code of the object identified by the locator.
     */
    private static String getFilenameFromLocator(final String locator) {
        final String[] parts = locator.split("\\.");
        return parts[1];
    }

    /**
     * Adds ".eo" extension to the name.
     * @param name Name if the file.
     * @return Name with extension ".eo"
     */
    private static String eoFilename(final String name) {
        return String.format("%s.eo", name);
    }
}
