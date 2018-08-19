package com.ds.solr.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Server {

    @JacksonXmlProperty(isAttribute = true)
    private String type;

    @JacksonXmlProperty(isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "host")
    private String host;

    @JacksonXmlProperty(localName = "port")
    private String port;

    @JacksonXmlProperty(localName = "master-host")
    private String masterHost;

    @JacksonXmlProperty(localName = "master-port")
    private String masterPort;

    public Server() {
    }
    public Server(String type, String name, String host, String port, String masterHost, String masterPort) {
        this.type = type;
        this.name = name;
        this.host = host;
        this.port = port;
        this.masterHost = masterHost;
        this.masterPort = masterPort;

    }

    @Override public String toString() {
        return "Server{" +
                "type : " + type + " , " +
                "name : " + name + " , " +
                "host : " + host + " , " +
                "port : " + port + " , " +
                "masterHost : " + masterHost + " , " +
                "masterPort : " + masterPort +
                "}";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMasterHost() {
        return masterHost;
    }

    public void setMasterHost(String masterHost) {
        this.masterHost = masterHost;
    }

    public String getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(String masterPort) {
        this.masterPort = masterPort;
    }
}



