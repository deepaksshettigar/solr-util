package com.ds.solr.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

public class Instance implements Serializable {

    public Instance() {

    }

    public Instance(String master, String slave, String reference, String host, String port, String masterHost, String masterPort) {
        this.master = master;
        this.slave = slave;
        this.reference = reference;
        this.host = host;
        this.port = port;
        this.masterHost = masterHost;
        this.masterPort = masterPort;

    }

    @JacksonXmlProperty(isAttribute = true)
    private String master;

    @JacksonXmlProperty(isAttribute = true)
    private String slave;

    @JacksonXmlProperty(isAttribute = true)
    private String reference;

    @JacksonXmlProperty(localName = "host")
    private String host;

    @JacksonXmlProperty(localName = "port")
    private String port;

    @XmlElement(required = false)
    @JacksonXmlProperty(localName = "master-host")
    private String masterHost;

    @XmlElement(required = false)
    @JacksonXmlProperty(localName = "master-port")
    private String masterPort;


    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSlave() {
        return slave;
    }

    public void setSlave(String slave) {
        this.slave = slave;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    @Override
    public String toString() {
        return "Instance [master = " + master + ", slave = " + slave + ", reference = " + reference + ", host = " + host + ", port = " + port + ", masterHost = " + masterHost + ", masterPort = " + masterPort +  "]";
    }
}
