package org.polystat;

public class SourceCode implements EORepresentation<String> {

    private final EOSource src;

    public SourceCode(EOSource src) {
        this.src = src;
    }

    @Override
    public String repr(String locator) throws Exception {
        return src.sourceCode(locator);
    }
}
