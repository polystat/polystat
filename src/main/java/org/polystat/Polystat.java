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

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.xml.XML;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.cactoos.Func;
import org.cactoos.io.OutputTo;
import org.cactoos.io.Stdin;
import org.cactoos.io.TeeInput;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.LengthOf;
import picocli.CommandLine;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;

/**
 * Main entrance.
 *
 * @since 1.0
 * @todo #1:1h Let's use some library for command line arguments parsing.
 *  The current implementation in this class is super primitive and must
 *  be replaced by something decent.
 * @todo #1:1h For some reason, the Logger.info() doesn't print anything
 *  to the console when the JAR is run via command line. It does print
 *  during testing, but doesn't show anything when being run as
 *  a JAR-with-dependencies. I didn't manage to find the reason, that's
 *  why added these "printf" instructions. Let's fix it, make sure
 *  Logger works in the JAR-with-dependencies, and remove this.stdout
 *  from this class at all.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@CommandLine.Command(
    name = "polystat",
    helpCommand = true,
    description = "Read our README in GitHub",
    mixinStandardHelpOptions = true,
    versionProvider = Polystat.Version.class,
    defaultValueProvider = Polystat.ConfigProvider.class
)
public final class Polystat implements Callable<Integer> {

    /**
     * Analyzers.
     */
    private static final Analysis[] ALL = {
        new AnFaR(),
        new AnOdin(),
    };

    /**
     * Source directory. If not specified, defaults to reading code from standard input.
     */
    @CommandLine.Option(
        names = "--files",
        description = "The directory with EO files."
    )
    private Path source;

    /**
     * Output directoty. If not specified, defaults to a temporary directory.
     */
    @CommandLine.Option(
        names = "--tmp",
        description = "The directory with .XML files and maybe other temp."
    )
    private Path temp;

    /**
     * Output directoty.
     */
    @CommandLine.Option(
        names = "--sarif",
        description = "Print JSON output in SARIF 2.0 format"
    )
    private boolean sarif;

    /**
     * Main entrance for Java command line.
     * @param args The args
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public static void main(final String... args) {
        System.exit(
            new CommandLine(new Polystat()).execute(args)
        );
    }

    @Override
    public Integer call() throws Exception {
        final Path tempdir;
        if (this.temp == null) {
            tempdir = Files.createTempDirectory("polystat-temp");
        } else {
            tempdir = this.temp;
        }
        final Path sources;
        if (this.source == null) {
            sources = readCodeFromStdin();
        } else {
            sources = this.source;
        }
        final Iterable<Result> errors =
            Polystat.scan(sources, tempdir);
        final Supplier<String> out;
        if (this.sarif) {
            out = new AsSarif(errors);
        } else {
            out = new AsConsole(errors);
        }
        Logger.info(this, "%s\n", out.get());
        return 0;
    }

    /**
     * Scan.
     * @param src Path with sources
     * @param tmp Path with temp files
     * @return Errors
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static Iterable<Result> scan(final Path src, final Path tmp) {
        final Func<String, XML> xmir = new Program(src, tmp);
        final Collection<Result> errors = new ArrayList<>(Polystat.ALL.length);
        for (final Analysis analysis : Polystat.ALL) {
            try {
                errors.addAll(new ListOf<>(analysis.errors(xmir, "\\Phi.test")));
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                errors.add(
                    new Result.Failed(
                        analysis.getClass(),
                        ex,
                        analysis.getClass().getName()
                    )
                );
            }
        }
        return errors;
    }

    /**
     * Reads the EO code from standard input,
     * creates a temporary directory and
     * writes the code to a new file in this directory called "test.eo".
     * @return Path object of "{tmpdir}/test.eo" files.
     * @throws Exception When IO fails.
     */
    private static Path readCodeFromStdin() throws Exception {
        final Path tmpdir = Files.createTempDirectory("polystat_stdin");
        final String name = "test.eo";
        final Path fullpath = tmpdir.resolve(Paths.get(name));
        final Path tmpfile = Files.createFile(fullpath);
        new LengthOf(
            new TeeInput(
                new Stdin(),
                new OutputTo(tmpfile)
            )
        ).value();
        return tmpdir;
    }

    /**
     * Version.
     * @since 1.0
     */
    static final class Version implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{
                Manifests.read("Polystat-Version"),
                Manifests.read("EO-Version"),
            };
        }
    }

    /**
     * Used to provide values for cmdline options via a config file '.polystat'.
     * @since 1.0
     */
    static final class ConfigProvider implements CommandLine.IDefaultValueProvider {

        /**
         * Configuration for Polystat.
         */
        private final Config config = new Config(Paths.get(".polystat"));

        @Override
        @Nullable
        public String defaultValue(final ArgSpec arg) throws Exception {
            final String result;
            if (arg.isOption()) {
                final OptionSpec opt = (OptionSpec) arg;
                final String key = opt.longestName();
                final String value = this.config.get(key);
                result = value;
            } else {
                result = null;
            }
            return result;
        }
    }
}
