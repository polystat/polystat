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

    XML xmir() throws IOException {
        final String eo = this.source.toFile().getAbsolutePath();
        final String[] nameParts = Paths.get(eo).getFileName().toString().split("\\.");
        final String filename = nameParts[0];
        final Path xml = this.temp.resolve(String.format("%s.xml", filename));
        if (!Files.exists(xml)) {
            new Syntax(
                filename,
                new InputOf(this.source.resolve(eo)),
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


    String sourceCode() throws IOException {
        return new TextOf(
            new InputOf(
                this.source.resolve(this.source.toFile().getAbsolutePath())
            )
        ).asString();
    }
}
