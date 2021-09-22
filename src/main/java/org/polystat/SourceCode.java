package org.polystat;

public class SourceCode implements EORepresentation<String> {
    @Override
    public String apply(EOSource eoSource) throws Exception {
        return eoSource.sourceCode();
    }
}
