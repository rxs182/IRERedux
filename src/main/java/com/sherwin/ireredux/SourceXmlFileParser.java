package com.sherwin.ireredux;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SourceXmlFileParser {
    protected static Map<String, String> parse(File xmlFile) throws IOException {
        Map<String, String> surfaceMap = new HashMap<String, String>();

        try {
            // the surface map is a collection of key<surface name>:values<list of surface masks
            XmlMapper xmlMapper = new XmlMapper();
            long xmlTime = System.currentTimeMillis();
            Project project = xmlMapper.readValue(xmlFile, Project.class);
            if (log.isDebugEnabled()) {
                log.debug( "Time to parse xml: " + (System.currentTimeMillis() - xmlTime) + " ms" );
            }

            project.getSurfaces().forEach(surface -> {
                String surfaceName = surface.getName();
                // There should only be one mask per surface
                Optional<SurfaceMask> surfaceMask = surface.getSurfaceMask().stream().findFirst();
                surfaceMask.ifPresent(mask -> surfaceMap.put(surfaceName, mask.getString()));
            });

        } catch (IOException e) {
            log.error("Error ");
        }

        return surfaceMap;
    }
}
