package com.ds.solr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XmlObjectMapping {
    private static final Logger log = LogManager.getLogger(XmlObjectMapping.class);
    private URI getSolrConfigFilePath() {
        //File file = new File(getClass().getResource("solr-server-config.xml").getFile());
        ClassPathResource res = new ClassPathResource("solr-server-config.xml");

        File file = null;
        URI uri = null;
        try {
            file = res.getFile();
            uri = res.getURI();
            log.info(uri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = file.getAbsolutePath();
        log.info("filePath:" +filePath );
        return uri;
    }

    public static void main(String[] args) {
    // D:\projects\spring-junit\src\main\resources\solr-server-config.xml
        XmlObjectMapping obj = new XmlObjectMapping();

        ObjectMapper objectMapper = new XmlMapper();
        Solr servers = null;
        try {

            servers = objectMapper.readValue(
                    StringUtils.toEncodedString(Files.readAllBytes(Paths.get(obj.getSolrConfigFilePath())), StandardCharsets.UTF_8),
                    Solr.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Server server : servers.getServer()) {
            log.info(server.getType());
        }
        log.info(servers);
    }
}
