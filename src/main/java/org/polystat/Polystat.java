/*
 * SPDX-FileCopyrightText: Copyright (c) 2020-2022 Polystat.org
 * SPDX-License-Identifier: MIT
 */
package org.polystat;

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cactoos.Func;
import org.cactoos.io.OutputTo;
import org.cactoos.io.Stdin;
import org.cactoos.io.TeeInput;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.LengthOf;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;

/**
 * Main entrance.
 * @since 1.0
 * @todo #1:1h Let's use some library for command line arguments parsing.
 *  The current implementation in this class is super primitive and must
 *  be replaced by something decent.
 */
@CommandLine.Command(
    name = "polystat",
    helpCommand = true,
    description = "Read our README in GitHub",
    mixinStandardHelpOptions = true,
    versionProvider = Version.class
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
     * Either "include" list or "exclude" list.
     */
    @ArgGroup(exclusive = true)
    private IncludeExclude inex;

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
     * Ctor.
     */
    public Polystat() {
        // nothing
    }

    /**
     * Main entrance for Java command line.
     * @param cmdargs The args from the command line
     */
    public static void main(final String... cmdargs) {
        final List<String> confargs = new ListOf<>(
            new Config(Paths.get(".polystat"))
        );
        confargs.addAll(new ListOf<>(cmdargs));
        new CommandLine(new Polystat()).execute(
            confargs.toArray(new String[0])
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
            sources = Polystat.stdin();
        } else {
            sources = this.source;
        }
        if (sources.toFile().list() == null) {
            throw new IOException(
                String.format("Provided directory doesn't have any files: %s", sources)
            );
        }
        final Iterable<Result> errors = this.scan(sources, tempdir);
        final Supplier<String> out;
        if (this.sarif) {
            out = new AsSarif(errors);
        } else {
            out = new AsConsole(errors);
        }
        Logger.info(this, "%s%n", out.get());
        return 0;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Iterable<Result> scan(final Path src, final Path tmp) {
        final Func<String, XML> xmir = new Program(src, tmp);
        final Collection<Result> errors = new ArrayList<>(Polystat.ALL.length);
        final String extension = ".eo";
        for (final Analysis analysis : Polystat.ALL) {
            try {
                for (final String file : src.toFile().list()) {
                    if (file.endsWith(extension)) {
                        errors.addAll(
                            new ListOf<>(
                                analysis.errors(
                                    xmir,
                                    String.format(
                                        "\\Phi.%s",
                                        file.substring(
                                            0, file.length() - extension.length()
                                        )
                                    )
                                )
                            )
                        );
                    }
                }
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                errors.add(
                    new Result.Failed(
                        analysis.getClass(), ex, analysis.getClass().getName()
                    )
                );
            }
        }
        final Collection<Result> filtered;
        if (this.inex == null) {
            filtered = errors;
        } else {
            filtered = errors.stream()
                .filter(this.inex::allows)
                .collect(Collectors.toList());
        }
        return filtered;
    }

    private static Path stdin() throws Exception {
        final Path tmpdir = Files.createTempDirectory("polystat_stdin");
        new LengthOf(
            new TeeInput(
                new Stdin(),
                new OutputTo(Files.createFile(tmpdir.resolve("test.eo")))
            )
        ).value();
        return tmpdir;
    }
}
