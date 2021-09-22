package org.polystat;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.cactoos.io.InputOf;
import org.cactoos.io.OutputTo;
import org.cactoos.text.TextOf;
import org.eolang.parser.Spy;
import org.eolang.parser.Syntax;
import org.eolang.parser.Xsline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EOSource {
    /**
     * The directory with EO files.
     */
    private final Path source;

    /**
     * The directory with .XML files and maybe other temp.
     */
    private final Path temp;

    /**
     * Ctor.
     *
     * @param src The dir with .eo sources
     * @param tmp Temp dir with .xml files
     */
    public EOSource(final Path src, final Path tmp) {
        this.source = src;
        this.temp = tmp;
    }

    private static String getFilenameFromLocator(String locator) {
        final String[] parts = locator.split("\\.");
        return parts[1];
    }

    XML xmir(String locator) throws IOException {
        final String name = getFilenameFromLocator(locator);
        final Path xml = this.temp.resolve(String.format("%s.xml", name));
        if (!Files.exists(xml)) {
            new Syntax(
                name,
                new InputOf(this.source.resolve(String.format("%s.eo", name))),
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


    String sourceCode(String locator) throws IOException {
        return new TextOf(
            new InputOf(
                this.source.resolve(
                    String.format("%s.eo", getFilenameFromLocator(locator))
                )
            )
        ).asString();
    }
}
