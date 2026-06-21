package com.networkanalyzer.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import com.networkanalyzer.model.PacketData;

public class PacketCaptureService {
    private final List<PcapHandle> handles = new ArrayList<>();
    private ExecutorService executor;
    private volatile boolean running;
    private final Consumer<PacketData> packetConsumer;

    public PacketCaptureService(Consumer<PacketData> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }

    public void startCapture() {
        try {
            List<PcapNetworkInterface> validInterfaces = findPhysicalInterfaces();
            
            if (validInterfaces.isEmpty()) {
                System.out.println("ERROR: No physical Wi-Fi or Ethernet adapters found!");
                return;
            }

            running = true;
            // Create a thread pool large enough to handle all valid adapters simultaneously
            executor = Executors.newFixedThreadPool(validInterfaces.size());

            for (PcapNetworkInterface nif : validInterfaces) {
                System.out.println("Binding to adapter: " + nif.getDescription());
                PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);
                handles.add(handle);
                
                // Launch a dedicated capture loop for this specific adapter
                executor.execute(() -> capturePackets(handle));
            }
            
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }

    private List<PcapNetworkInterface> findPhysicalInterfaces() {
        List<PcapNetworkInterface> physicalNifs = new ArrayList<>();
        try {
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            System.out.println("--- SCANNING FOR PHYSICAL ADAPTERS ---");
            
            for (PcapNetworkInterface nif : allDevs) {
                String desc = nif.getDescription() != null ? nif.getDescription().toLowerCase() : "";
                
                // Exclude obvious virtual, loopback, and hyper-v adapters
                if (desc.contains("virtual") || desc.contains("hyper-v") || desc.contains("loopback") || nif.isLoopBack()) {
                    continue; 
                }
                
                // If it looks like a real Wi-Fi or Ethernet card, add it to our list
                if (desc.contains("wi-fi") || desc.contains("wireless") || desc.contains("802.11") || 
                    desc.contains("ethernet") || desc.contains("gigabit") || desc.contains("pcie")) {
                    
                    physicalNifs.add(nif);
                    System.out.println("FOUND: " + nif.getDescription());
                }
            }
            
            // Fallback: If we aggressively filtered out everything, just try to grab the first non-loopback device
            if (physicalNifs.isEmpty() && !allDevs.isEmpty()) {
                for (PcapNetworkInterface nif : allDevs) {
                    if (!nif.isLoopBack()) {
                        System.out.println("WARNING: Using fallback adapter: " + nif.getDescription());
                        physicalNifs.add(nif);
                        break;
                    }
                }
            }
            System.out.println("--------------------------------------");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return physicalNifs;
    }

    private void capturePackets(PcapHandle handle) {
        while (running && handle != null && handle.isOpen()) {
            try {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    processPacket(packet);
                }
            } catch (NotOpenException e) {
                // Handle was closed, exit the loop cleanly
                break;
            } catch (Exception e) {
                // Catch timeouts or other minor packet issues without killing the thread
            }
        }
    }

    private void processPacket(Packet packet) {
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        if (ipV4Packet != null) {
            PacketData data = new PacketData(
                LocalDateTime.now(),
                ipV4Packet.getHeader().getSrcAddr().getHostAddress(),
                ipV4Packet.getHeader().getDstAddr().getHostAddress(),
                determineProtocol(packet),
                packet.length()
            );
            
            // Hand the packet back to the MainController
            packetConsumer.accept(data);
        }
    }

    private String determineProtocol(Packet packet) {
        String packetString = packet.toString();
        if (packetString.contains("TCP")) return "TCP";
        if (packetString.contains("UDP")) return "UDP";
        if (packetString.contains("HTTP")) return "HTTP";
        if (packetString.contains("HTTPS")) return "HTTPS";
        return "OTHER";
    }

    public void stopCapture() {
        running = false;
        
        // Close every open handle
        for (PcapHandle handle : handles) {
            if (handle != null && handle.isOpen()) {
                handle.close();
            }
        }
        handles.clear();
        
        if (executor != null) {
            executor.shutdown();
        }
    }
}