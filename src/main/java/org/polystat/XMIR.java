package org.polystat;

import com.jcabi.xml.XML;

import java.util.List;

public class XMIR implements EORepresentation<XML> {

    private final String objectLocator;

    public XMIR(String objectLocator) {
        this.objectLocator = objectLocator;
    }

    @Override
    public XML apply(EOSource src) throws Exception {

        XML obj = src.xmir().nodes("/program/objects").get(0);
        final String[] parts = objectLocator.split("\\.");
        for (int idx = 1; idx < parts.length; ++idx) {
            final List<XML> objs = obj.nodes(
                String.format("o[@name='%s']", parts[idx])
            );
            obj = objs.get(0);
        }
        return obj;
    }
}
