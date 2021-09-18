package org.polystat.odin;

import com.jcabi.xml.XML;
import org.cactoos.Func;
import org.eolang.parser.XMIR;
import org.polystat.Analysis;
import org.polystat.odin.interop.java.EOOdinAnalyzer;

import java.util.stream.Collectors;

public class OdinAnalysis implements Analysis {
    private final EOOdinAnalyzer odinAnalyzer;

    public OdinAnalysis() {
        this.odinAnalyzer = new EOOdinAnalyzer.EOOdinAnalyzerImpl();
    }

    @Override
    public Iterable<String> errors(Func<String, XML> xmir, String locator) throws Exception {
        final XMIR sourceCodeXmir = new XMIR(xmir.apply("\\Phi"));
        final String sourceCodeToAnalyze = sourceCodeXmir.toEO();

        return odinAnalyzer.analyzeSourceCode(sourceCodeToAnalyze).stream()
            .map(error -> error.message())
            .collect(Collectors.toList());
    }

    @Override
    public String analysisName() {
        return "Odin (object dependency inspector)";
    }
}
