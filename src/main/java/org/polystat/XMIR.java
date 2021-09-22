package org.polystat;

import com.jcabi.xml.XML;

import java.util.List;

public class XMIR implements EORepresentation<XML> {

    private final EOSource src;

    public XMIR(EOSource src) {
        this.src = src;
    }

    @Override
    public XML repr(String objectLocator) throws Exception {

        XML obj = src.xmir(objectLocator).nodes("/program/objects").get(0);
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
