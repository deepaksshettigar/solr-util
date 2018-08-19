package com.ds.solr.util;

import com.ds.solr.pojo.Servers;
import com.ds.solr.pojo.Server;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
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


public class SolrConfigService {

    private static final Logger log = LogManager.getLogger(XmlObjectMapping.class);


    private static URI getSolrConfigFilePath() {
        //File file = new File(getClass().getResource("solr-server-config.xml").getFile());
        ClassPathResource res = new ClassPathResource("solr-nodes-config.xml");

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
        log.debug("filePath:" +filePath );
        return uri;
    }

    public static Servers getServersInfo() {

        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        XmlMapper objectMapper = new XmlMapper(module);

        //ObjectMapper objectMapper = new XmlMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        Servers servers = null;
        try {

            String xmlStr = StringUtils.toEncodedString(Files.readAllBytes(Paths.get(getSolrConfigFilePath())), StandardCharsets.UTF_8);
            log.debug("xmlStr : " + xmlStr );

            servers = objectMapper.readValue(xmlStr, Servers.class);

        } catch (Exception e) {
            log.error("Exception occurred ", e);
        }

        /*for(Server server : servers.getServer()) {
            log.info(server.getGroup());
        }*/
        log.debug(servers);

        return servers;
    }

    public static void main(String[] args) {
        getServersInfo();
    }
}
