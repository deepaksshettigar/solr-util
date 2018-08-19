package com.ds.solr.util;

import com.ds.solr.pojo.Instance;
import com.ds.solr.pojo.Servers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private static SolrConfigService solrConfigService;


    private static final Map<String, String> SOLR_INSTANCES = initMap();

    private static Map<String, String> initMap() {
        Map<String, String> map = new HashMap<>();
        map.put("8983", "Library");
        Map<String, String> stringStringMap = Collections.unmodifiableMap(map);
        return stringStringMap;
    }

    private static String SOLR_ADMIN_URL_FORMAT = "http://%s:%s/solr/admin/cores?wt=xml";
    private static String SOLR_CONFIG_URL_FORMAT = "http://%s:%s/solr/%s/admin/file?file=solrconfig-dummy.xml";
    private static String SOLR_QUERY_URL_FORMAT = "http://%s:%s/solr/%s/select?q=*:*&wt=xml&rows=0";
    private static String SOLR_REPLICATION_URL_FORMAT = "http://%s:%s/solr/%s/replication";
    private final static long SOLR_DUMMY_RESULT_COUNT = -1;
    private static List<String> SOLR_CORES = Arrays.asList(new String[]{"http://localhost:%s/solr/admin/cores?wt=xml"});


    public static void main(String[] args) {
        SpringApplication.run(SolrConfigMonitor.class, args);
    }

    public SolrConfigMonitor() {
        // TODO

    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {


            log.info("### Solr Config Monitor ###");
            // getSolrConfig();
            testSolrConfig();

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


    private static void getSolrConfig() {
        ObjectMapper objectMapper = new XmlMapper();
        Solr servers = null;
        try {

            servers = objectMapper.readValue(
                    StringUtils.toEncodedString(getSolrConfigBytes(), StandardCharsets.UTF_8),
                    Solr.class);
        } catch (IOException e) {
            e.printStackTrace();
        }


        log.debug(servers);

    }

    public static void testSolrConfig() {
        // Create XPathFactory object
        XPathFactory xpathFactory = XPathFactory.newInstance();

        // Create XPath object
        XPath xpath = xpathFactory.newXPath();
        boolean testResult = true;


        Servers servers = solrConfigService.getServersInfo();
        for (com.ds.solr.pojo.Server server : servers.getServer()) {
            log.info("#################### " + server.getGroup() + " ####################");
            // Loop on the each instance in the group

            long prevInstanceResultCount = -1;
            for (Instance instance : server.getInstance()) {
                String host = instance.getHost();
                String port = instance.getPort();
                String masterHost = instance.getMasterHost();
                instance.getMasterHost();

                String adminUrl = String.format(SOLR_ADMIN_URL_FORMAT, host, port);

                // Load all cores
                Document adminInfo = loadXmlByUrl(adminUrl);
                if (null != adminInfo) {
                    List<String> cores = getCoreNames(adminInfo, xpath);
                    for (String core : cores) {
                        String solrconfigUrl = String.format(SOLR_CONFIG_URL_FORMAT, host, port, core);
                        String solrQueryUrl = String.format(SOLR_QUERY_URL_FORMAT, host, port, core);
                        Document solrconfigInfo = loadXmlByUrl(solrconfigUrl);
                        Document solrQueryInfo = loadXmlByUrl(solrQueryUrl);
                        long currentInstanceResultCount = getResultCount(solrQueryInfo, xpath, core);

                        // Perform Tests
                        testReplicationConfig(solrconfigInfo, xpath, instance, core);
                        testResultCount(prevInstanceResultCount, currentInstanceResultCount);
                        prevInstanceResultCount = currentInstanceResultCount;
                        log.info("\n");
                    }
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
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
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
                log.info("#################### Core Name : " + nodes.item(i).getNodeValue());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return list;
    }


    private static boolean testReplicationConfig(Document doc, XPath xpath, Instance instance, String core) {
        boolean testResult = false;
        if(doc != null) {

            XPathExpression masterXpath = null, slaveEnableXpath = null, slaveUrlXpath = null;
            String masterEnableReplication = null, slaveEnableReplication = null, slaveUrlReplication = null;
            String masterUrl = null, masterHost = null;
            try {
                //create XPathExpression object
                masterXpath = xpath.compile("/config/requestHandler[@name='/replication']/lst[@name='master']/str[@name='enable']/text()");
                //evaluate expression result on XML document
                masterEnableReplication = ((String) masterXpath.evaluate(doc, XPathConstants.STRING)).trim();
                log.debug("Master : Replication Enable : {}", masterEnableReplication);

                slaveEnableXpath = xpath.compile("/config/requestHandler[@name='/replication']/lst[@name='slave']/str[@name='enable']/text()");
                //evaluate expression result on XML document
                slaveEnableReplication = ((String) slaveEnableXpath.evaluate(doc, XPathConstants.STRING)).trim();
                log.debug("Slave : Replication Enable : {}", slaveEnableReplication);

                slaveUrlXpath = xpath.compile("/config/requestHandler[@name='/replication']/lst[@name='slave']/str[@name='masterUrl']/text()");
                //evaluate expression result on XML document
                slaveUrlReplication = ((String) slaveUrlXpath.evaluate(doc, XPathConstants.STRING)).trim();
                log.debug("Slave : Replication URL : {}", slaveUrlReplication);

                if (masterEnableReplication != null && instance.getMaster() != null &&
                        masterEnableReplication.equalsIgnoreCase("${enable.master:" + instance.getMaster() + "}")) {
                    log.info("Master : Config is correctly configured.");
                    testResult = true;
                } else {
                    log.error("Master : Config is incorrectly disabled.");
                }

                if (instance.getSlave() != null) {

                    if (slaveEnableReplication != null && slaveEnableReplication.equalsIgnoreCase("${enable.slave:" + instance.getSlave() + "}")) {
                        log.info("Slave : Config is correctly configured.");
                        testResult = true;
                    } else {
                        log.error("Slave : Config is incorrectly disabled.");
                    }

                    String slaveReplicationUrlWithCoreName = String.format(SOLR_REPLICATION_URL_FORMAT, instance.getHost(), instance.getPort(), core);
                    String slaveReplicationUrlWithCoreVar = String.format(SOLR_REPLICATION_URL_FORMAT, instance.getHost(), instance.getPort(), "${core.name}");

                    log.debug("Slave : Master Url : {}", slaveReplicationUrlWithCoreVar);

                    if (slaveReplicationUrlWithCoreName != null && (slaveUrlReplication.equalsIgnoreCase(slaveReplicationUrlWithCoreName) ||
                            slaveUrlReplication.equalsIgnoreCase(slaveReplicationUrlWithCoreVar))) {
                        log.info("Slave : Replication Url is correctly configured.");
                        testResult = true;
                    } else {
                        log.error("Slave : Replication Url is incorrectly configured.");
                    }

                } else {
                    log.error("Slave : Error reading config.");
                }

            } catch (XPathExpressionException e) {
                log.error("XPath Error Getting Replication Details", e);
                testResult = false;
            }
        }
        return testResult;
    }

    private static boolean testResultCount(long prevInstanceCount, long currentInstanceResultCount) {
        boolean testResult = false;

        if(currentInstanceResultCount == SOLR_DUMMY_RESULT_COUNT || currentInstanceResultCount == prevInstanceCount) {
            testResult = true;
            log.info("Result Count matches");
        } else {
            log.error("Result Count does not match");
        }

        return testResult;
    }

    private static long getResultCount(Document doc, XPath xpath, String core) {
        long resultCount = 0;
        XPathExpression resultCountXpath = null;
        try {
            //create XPathExpression object
            resultCountXpath = xpath.compile("/response/result/@numFound");
            String count = (String)resultCountXpath.evaluate(doc, XPathConstants.STRING);
            log.debug("COUNT : " + count);
            //evaluate expression result on XML document
            resultCount = (Long.valueOf(count)).longValue();
            log.debug("Result Count : {}", resultCount);
        } catch (XPathExpressionException e) {
            log.error("XPath Error Getting Result Count", e);
        }

        return resultCount;
    }
}