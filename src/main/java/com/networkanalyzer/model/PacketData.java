package com.networkanalyzer.model;

import java.time.LocalDateTime;

public class PacketData {
    private LocalDateTime timestamp;
    private String sourceIp;
    private String destinationIp;
    private String protocol;
    private int size;

    public PacketData(LocalDateTime timestamp, String sourceIp, String destinationIp, String protocol, int size) {
        this.timestamp = timestamp;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.protocol = protocol;
        this.size = size;
    }

    // Getters and setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
    
    public String getDestinationIp() { return destinationIp; }
    public void setDestinationIp(String destinationIp) { this.destinationIp = destinationIp; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}