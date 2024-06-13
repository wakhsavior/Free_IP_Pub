package ru.afkgroupnn.FreeIP.model;




public class Range {

    private final IPClass startIP;
    private final IPClass endIP;

    public Range (IPClass startIP, IPClass endIP){
        this.startIP = startIP;
        this.endIP = endIP;
    }
    @Override
    public String toString() {
        return "range " + startIP + " " + endIP;
    }
}
