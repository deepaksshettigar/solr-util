package com.ds.solr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootApplication
public class SolrConfigMonitor {
    private static final Logger log = LogManager.getLogger(SolrConfigMonitor.class);
    // LoggerContext logctx = (LoggerContext) LogManager.getContext();

    private static final Map<String, String> SOLR_INSTANCES = initMap();

    public SolrConfigMonitor() {


    }

    private static Map<String, String> initMap() {
        Map<String, String> map = new HashMap<>();
        map.put("8983", "Library");
        Map<String, String> stringStringMap = Collections.unmodifiableMap(map);
        return stringStringMap;
    }

    private String SOLR_ADMIN_URL_FORMAT="http://%s:%s/solr/admin/cores?wt=xml";
    private static List<String> SOLR_CORES= Arrays.asList(new String[]{"http://localhost:%s/solr/admin/cores?wt=xml"});

    private static String SOLR_CONFIG_URL = "http://localhost:%S/solr/%s/admin/file?file=solrconfig-dummy.xml";

    public static void main(String[] args) {
        SpringApplication.run(SolrConfigMonitor.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            /*ConfigurationBuilder<BuiltConfiguration> builder
                    = ConfigurationBuilderFactory.newConfigurationBuilder();
            RootLoggerComponentBuilder rootLogger
                    = builder.newRootLogger(Level.DEBUG);
            Configurator.initialize(builder.build());
            builder.add(rootLogger);*/

            /*
            LoggerConfig logconfig = logctx.getConfiguration().getRootLogger();

            log.info(logconfig.getLevel()); // INFO


            logconfig.setLevel(Level.DEBUG);
            logconfig.setLevel(Level.DEBUG);

            log.info(logconfig.getLevel()); // DEBUG

            logctx.updateLoggers();
            */


            log.info("### Solr Config Monitor ###");
            getSolrConfig();
            loadSolrConfig();

            System.exit(0);
        };
    }

    private static byte[] getSolrConfigBytes() {
        ClassPathResource res = new ClassPathResource("solr-server-config.xml");
        byte[] bytes = null;

        try {
            // file = res.getFile();
            InputStream is = res.getInputStream();
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


    private  static void getSolrConfig() {
        ObjectMapper objectMapper = new XmlMapper();
        Solr servers = null;
        try {

            servers = objectMapper.readValue(
                    StringUtils.toEncodedString(getSolrConfigBytes(), StandardCharsets.UTF_8),
                    Solr.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Server server : servers.getServer()) {
            log.info(server.getType());
        }
        log.info(servers);

    }

    public static void loadSolrConfig() {
        // Create XPathFactory object
        XPathFactory xpathFactory = XPathFactory.newInstance();

        // Create XPath object
        XPath xpath = xpathFactory.newXPath();
        boolean testResult = true;




        for (Map.Entry<String, String> entry : SOLR_INSTANCES.entrySet()) {
            String solrInstancePort = entry.getKey();
            log.info("solrInstancePort" + solrInstancePort);
            for (String coreUrl : SOLR_CORES) {
                coreUrl = String.format(coreUrl, solrInstancePort);
                log.info("############### Core Url ############### :" + coreUrl);
                List<String> solrCoreNames = getCoreNames(loadXmlByUrl(coreUrl), xpath);
                for (String solrCoreName : solrCoreNames) {
                    log.info("Solr Core name : {}", solrCoreName);

                }
            }
        }
    }
    private static Document loadXmlByUrl(String urlString) {
        // if you prefer DOM:
        Document doc = null;
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(conn.getInputStream());
            TransformerFactory transformerFactory= TransformerFactory.newInstance();
            Transformer xform = transformerFactory.newTransformer();

            // that’s the default xform; use a stylesheet to get a real one
            // xform.transform(new DOMSource(doc), new StreamResult(System.out));

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
        // log.info("### DOCUMENT ### " + doc.getDocumentURI());
        return doc;
    }


    private static List<String> getCoreNames(Document doc, XPath xpath) {
        List<String> list = new ArrayList<>();
        try {
            //create XPathExpression object
            XPathExpression expr =
                    xpath.compile("/response/lst[@name='status']/lst/@name");
            //evaluate expression result on XML document
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(nodes.item(i).getNodeValue());
                log.info("############### Core Name ############### : "+ nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return list;
    }


    private static boolean getReplicationConfig(Document doc, XPath xpath, String solrNodeType) {
        boolean testResult = true;
        Boolean replication = true;
        // solrNodeType = "master";
        XPathExpression expr = null;
        String replicationFlag = null;
        String masterUrl = null;

        try {
            //create XPathExpression object
            expr =
                    xpath.compile("/config/requestHandler[@name='/replication']/lst[@name='"+ solrNodeType
                            + "']/str[@name='enable']/text()");
            //evaluate expression result on XML document
            replicationFlag = (String) expr.evaluate(doc, XPathConstants.STRING);


            expr =
                    xpath.compile("/config/requestHandler[@name='/replication']/lst[@name='"+ solrNodeType
                            + "']/str[@name='masterUrl']/text()");
            //evaluate expression result on XML document
            replicationFlag = (String) expr.evaluate(doc, XPathConstants.STRING);

            if(solrNodeType.equalsIgnoreCase("master") ) {
                if (replicationFlag!= null && replicationFlag.equalsIgnoreCase("${enable."+ solrNodeType +":true}")) {
                    // do nothing
                } else {
                    testResult = false;
                    log.info("############### Master Config is incorrectly disabled.");
                }
            }
            if(solrNodeType.equalsIgnoreCase("slave") ) {
                if (replicationFlag!= null && replicationFlag.equalsIgnoreCase("${enable."+ solrNodeType +":true}")) {
                    log.info("############### Master : true");
                } else {
                    testResult = false;
                    log.info("############### ERROR : " + solrNodeType  + " is incorrectly configured.");
                }

                if (replicationFlag!= null && replicationFlag.equalsIgnoreCase("${enable."+ solrNodeType +":true}")) {
                    log.info("############### Master : true");
                } else {
                    testResult = false;
                    log.info("############### ERROR : " + solrNodeType  + " is incorrectly configured.");
                }

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return testResult;
    }


}
