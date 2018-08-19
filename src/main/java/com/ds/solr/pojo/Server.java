package com.ds.solr.pojo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.Serializable;
import java.util.Arrays;

public class Server implements Serializable {

    public Server() {

    }

    public Server(Instance[] instance) {
        this.instance = instance;

    }
    @JacksonXmlProperty(isAttribute = true)
    private String group;

    @JacksonXmlProperty(localName = "instance")
    private Instance[] instance;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Instance[] getInstance() {
        return instance;
    }

    public void setInstance(Instance[] instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "Server [group = " + group + ", instance = " + Arrays.toString(instance) + "]";
    }
}


