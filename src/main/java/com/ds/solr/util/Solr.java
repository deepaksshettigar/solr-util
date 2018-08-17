package com.ds.solr.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Arrays;

@JacksonXmlRootElement (localName = "solr")
public class Solr {

    @JacksonXmlElementWrapper(localName = "server", useWrapping = false)
    private Server[] server;

    public Solr() {
    }

    public Solr(Server[] server) {
        this.server = server;
    }

    @Override public String toString() {
        return "Solr{" +
                "Server=" + Arrays.toString(server) +
                '}';
    }

    public Server[] getServer() {
        return server;
    }

    public void setServer(Server[] server) {
        this.server = server;
    }
}


