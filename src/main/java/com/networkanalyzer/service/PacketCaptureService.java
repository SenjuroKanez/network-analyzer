package com.networkanalyzer.service;

import com.networkanalyzer.model.PacketData;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PacketCaptureService {
    private PcapHandle handle;
    private ExecutorService executor;
    private volatile boolean running;
    private final Consumer<PacketData> packetConsumer;

    public PacketCaptureService(Consumer<PacketData> packetConsumer) {
        this.packetConsumer = packetConsumer;
    }

    public void startCapture() {
        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(findFirstDevice());
            handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);
            running = true;
            executor = Executors.newSingleThreadExecutor();
            executor.execute(this::capturePackets);
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }

    private String findFirstDevice() {
        try {
            return Pcaps.findAllDevs().get(0).getName();
        } catch (PcapNativeException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void capturePackets() {
        while (running) {
            try {
                Packet packet = handle.getNextPacket();
                if (packet != null) {
                    processPacket(packet);
                }
            } catch (NotOpenException e) {
                e.printStackTrace();
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
            packetConsumer.accept(data);
        }
    }

    private String determineProtocol(Packet packet) {
        // Simplified protocol detection
        if (packet.toString().contains("TCP")) return "TCP";
        if (packet.toString().contains("UDP")) return "UDP";
        if (packet.toString().contains("HTTP")) return "HTTP";
        if (packet.toString().contains("HTTPS")) return "HTTPS";
        return "OTHER";
    }

    public void stopCapture() {
        running = false;
        if (handle != null) {
            handle.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
}