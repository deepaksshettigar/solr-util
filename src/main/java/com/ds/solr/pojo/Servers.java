package com.ds.solr.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.Arrays;

@JacksonXmlRootElement(localName = "servers")
public class Servers implements Serializable {


    public Servers() {
    }
    public Servers(Server[] server) {
        this.server = server;
    }

    @JacksonXmlElementWrapper(localName = "server", useWrapping = false)
    private Server[] server;

    public Server[] getServer() {
        return server;
    }

    public void setServer(Server[] server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return "Servers [server = " + Arrays.toString(server) + "]";
    }
}



