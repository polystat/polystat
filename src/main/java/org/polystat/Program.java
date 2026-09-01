/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */

package org.polystat;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.eolang.parser.Spy;
import org.eolang.parser.Syntax;
import org.eolang.parser.Xsline;

/**
 * A collection of all EO files, which are accessible as XMIR elements,
 * by their object locators.
 * @since 1.0
 */
public final class Program implements Func<String, XML> {

    /**
     * Boundary between the parts of an object locator.
     */
    private static final Pattern DOT = Pattern.compile("\\.");

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
        final List<String> parts = Program.DOT.splitAsStream(locator)
            .collect(Collectors.toList());
        final String name = parts.get(1);
        final Path xml = this.temp.resolve(String.format("%s.xml", name));
        final Path src = this.sources.resolve(String.format("%s.eo", name));
        if (
            !xml.toFile().exists()
                || xml.toFile().lastModified() < src.toFile().lastModified()
        ) {
            new Syntax(
                name,
                new InputOf(src),
                new OutputTo(xml)
            ).parse();
            new Xsline(
                new XMLDocument(xml),
                new OutputTo(xml),
                new Spy.Verbose()
            ).pass();
        }
        XML obj = new XMLDocument(xml).nodes("/program/objects").get(0);
        for (final String part : parts.subList(1, parts.size())) {
            obj = obj.nodes(String.format("o[@name='%s']", part)).get(0);
        }
        return obj;
    }
}
