package com.networkanalyzer.controller;

import com.networkanalyzer.model.PacketData;
import com.networkanalyzer.service.DatabaseService;
import com.networkanalyzer.service.PacketCaptureService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class MainController {
    // Existing FXML components
    @FXML private TableView<PacketData> packetTable;
    @FXML private TableColumn<PacketData, String> timestampColumn;
    @FXML private TableColumn<PacketData, String> sourceIpColumn;
    @FXML private TableColumn<PacketData, String> destIpColumn;
    @FXML private TableColumn<PacketData, String> protocolColumn;
    @FXML private TableColumn<PacketData, Integer> sizeColumn;
    @FXML private ComboBox<String> protocolFilter;
    @FXML private TextField ipFilter;
    @FXML private Button captureButton;
    
    // New chart components
    @FXML private PieChart protocolChart;
    @FXML private Label totalPacketsLabel;
    @FXML private Label tcpPacketsLabel;
    @FXML private Label udpPacketsLabel;
    @FXML private Label otherPacketsLabel;
    @FXML private Label captureRateLabel;
    
    // Existing fields
    private PacketCaptureService captureService;
    private DatabaseService databaseService;
    private ObservableList<PacketData> packets;
    private boolean isCapturing = false;
    
    // New fields for chart functionality
    private final Map<String, Integer> protocolCounts = new HashMap<>();
    private final ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
    private long captureStartTime = 0;
    private int totalPacketCount = 0;
    
    // Animation fields
    private Timeline captureButtonPulse;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupServices();
        setupChart(); // New method for chart setup
    }

    private void setupTable() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        sourceIpColumn.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));
        destIpColumn.setCellValueFactory(new PropertyValueFactory<>("destinationIp"));
        protocolColumn.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        
        packets = FXCollections.observableArrayList();
        packetTable.setItems(packets);
    }

    private void setupFilters() {
        protocolFilter.setItems(FXCollections.observableArrayList(
            "All", "TCP", "UDP", "HTTP", "HTTPS", "ICMP"
        ));
        protocolFilter.setValue("All");
        
        protocolFilter.setOnAction(e -> applyFilters());
        ipFilter.textProperty().addListener((obs, old, newValue) -> applyFilters());
    }

    private void setupServices() {
        databaseService = new DatabaseService();
        captureService = new PacketCaptureService(packet -> {
            Platform.runLater(() -> {
                packets.add(packet);
                databaseService.savePacket(packet);
                
                // Update chart with new packet data
                updatePacketStatistics(packet);
            });
        });
    }
    
    // New method to setup chart
    private void setupChart() {
        // Initialize chart
        if (protocolChart != null) {
            protocolChart.setData(chartData);
            protocolChart.setAnimated(true);
            protocolChart.setStyle("-fx-background-color: transparent;");
        }
        
        // Initialize protocol counts
        protocolCounts.put("TCP", 0);
        protocolCounts.put("UDP", 0);
        protocolCounts.put("HTTP", 0);
        protocolCounts.put("HTTPS", 0);
        protocolCounts.put("ICMP", 0);
        protocolCounts.put("OTHER", 0);
        
        // Initialize chart and statistics
        updateChart();
        updateStatisticsLabels();
        
        // Set capture start time
        captureStartTime = System.currentTimeMillis();
    }
    
    // New method to update packet statistics
    private void updatePacketStatistics(PacketData packet) {
        // Update protocol counts
        String protocol = packet.getProtocol().toUpperCase();
        
        // Map HTTP/HTTPS to TCP if they're detected as such
        if (protocol.equals("HTTP") || protocol.equals("HTTPS")) {
            // Keep HTTP/HTTPS separate if you want, or map to TCP
            if (protocolCounts.containsKey(protocol)) {
                protocolCounts.put(protocol, protocolCounts.get(protocol) + 1);
            } else {
                protocolCounts.put("OTHER", protocolCounts.get("OTHER") + 1);
            }
        } else if (protocolCounts.containsKey(protocol)) {
            protocolCounts.put(protocol, protocolCounts.get(protocol) + 1);
        } else {
            protocolCounts.put("OTHER", protocolCounts.get("OTHER") + 1);
        }
        
        totalPacketCount++;
        
        // Add subtle flash animation to table when new packet arrives
        animateNewPacketArrival();
        
        // Update chart and statistics
        updateChart();
        updateStatisticsLabels();
    }
    
    // Animation for new packet arrival
    private void animateNewPacketArrival() {
        if (packetTable != null && isCapturing) {
            // Quick flash effect
            FadeTransition flash = new FadeTransition(Duration.millis(100), packetTable);
            flash.setFromValue(1.0);
            flash.setToValue(0.7);
            flash.setCycleCount(2);
            flash.setAutoReverse(true);
            flash.play();
            
            // Scroll to bottom to show latest packet
            Platform.runLater(() -> {
                if (!packets.isEmpty()) {
                    packetTable.scrollTo(packets.size() - 1);
                }
            });
        }
    }
    
    // Method to update the pie chart
    private void updateChart() {
        if (protocolChart == null) return;
        
        chartData.clear();
        
        for (Map.Entry<String, Integer> entry : protocolCounts.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
                chartData.add(data);
            }
        }
        
        // Apply custom colors to chart slices
        Platform.runLater(() -> {
            for (PieChart.Data data : chartData) {
                if (data.getNode() != null) {
                    switch (data.getName()) {
                        case "TCP"   -> data.getNode().setStyle("-fx-pie-color: #81c784;");
                        case "UDP"   -> data.getNode().setStyle("-fx-pie-color: #ffb74d;");
                        case "HTTP"  -> data.getNode().setStyle("-fx-pie-color: #4fc3f7;");
                        case "HTTPS" -> data.getNode().setStyle("-fx-pie-color: #ba68c8;");
                        case "ICMP"  -> data.getNode().setStyle("-fx-pie-color: #64b5f6;");
                        default      -> data.getNode().setStyle("-fx-pie-color: #f06292;");
                    }
                }
            }
        });
    }
    
    // Method to update statistics labels
    private void updateStatisticsLabels() {
        if (totalPacketsLabel != null) {
            totalPacketsLabel.setText(String.valueOf(totalPacketCount));
        }
        if (tcpPacketsLabel != null) {
            int tcpCount = protocolCounts.get("TCP") + protocolCounts.get("HTTP") + protocolCounts.get("HTTPS");
            tcpPacketsLabel.setText(String.valueOf(tcpCount));
        }
        if (udpPacketsLabel != null) {
            udpPacketsLabel.setText(String.valueOf(protocolCounts.get("UDP")));
        }
        if (otherPacketsLabel != null) {
            int otherCount = protocolCounts.get("ICMP") + protocolCounts.get("OTHER");
            otherPacketsLabel.setText(String.valueOf(otherCount));
        }
        
        // Calculate and display capture rate
        if (captureRateLabel != null) {
            long elapsedTime = System.currentTimeMillis() - captureStartTime;
            double ratePerSecond = 0;
            if (elapsedTime > 0) {
                ratePerSecond = (totalPacketCount * 1000.0) / elapsedTime;
            }
            captureRateLabel.setText(String.format("%.1f pkt/s", ratePerSecond));
        }
    }
    
    // Method to reset statistics
    private void resetStatistics() {
        protocolCounts.replaceAll((k, v) -> 0);
        totalPacketCount = 0;
        captureStartTime = System.currentTimeMillis();
        
        Platform.runLater(() -> {
            updateChart();
            updateStatisticsLabels();
        });
    }

    @FXML
    private void toggleCapture() {
        if (!isCapturing) {
            // Reset statistics when starting new capture
            resetStatistics();
            packets.clear(); // Clear existing packets from table
            
            captureService.startCapture();
            captureButton.setText("⏹ Stop Capture");
            captureButton.setStyle("-fx-background-color: linear-gradient(to bottom, #f44336, #d32f2f); -fx-background-radius: 20; -fx-text-fill: white; -fx-padding: 12 24; -fx-font-size: 14; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(244, 67, 54, 0.6), 10, 0, 3, 3); -fx-cursor: hand;");
            
            // Start pulsing animation
            startCaptureButtonPulse();
            
        } else {
            captureService.stopCapture();
            captureButton.setText("▶ Start Capture");
            captureButton.setStyle("-fx-background-color: linear-gradient(to bottom, #4caf50, #388e3c); -fx-background-radius: 20; -fx-text-fill: white; -fx-padding: 12 24; -fx-font-size: 14; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.6), 10, 0, 3, 3); -fx-cursor: hand;");
            
            // Stop pulsing animation
            stopCaptureButtonPulse();
        }
        isCapturing = !isCapturing;
    }
    
    // Dynamic button hover effects
    @FXML
    // This method is intended to be used as an event handler in the FXML file.
    // Example FXML usage:
    // <Button fx:id="captureButton" onMouseEntered="#onCaptureButtonHover" ... />
    private void onCaptureButtonHover() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), captureButton);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        scaleIn.play();
        
        // Add glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(isCapturing ? Color.RED : Color.LIME);
        glow.setRadius(15);
        captureButton.setEffect(glow);
    }
    
    @FXML
    private void onCaptureButtonExit() {
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), captureButton);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.play();
        
        // Reset to normal shadow
        DropShadow normalShadow = new DropShadow();
        normalShadow.setColor(isCapturing ? Color.web("rgba(244, 67, 54, 0.6)") : Color.web("rgba(76, 175, 80, 0.6)"));
        normalShadow.setRadius(10);
        normalShadow.setOffsetX(3);
        normalShadow.setOffsetY(3);
        captureButton.setEffect(normalShadow);
    }
    
    // Table hover effects
    @FXML
    private void onTableHover() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), packetTable);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);
        scaleIn.play();
        
        // Enhanced glow effect
        DropShadow tableGlow = new DropShadow();
        tableGlow.setColor(Color.CYAN);
        tableGlow.setRadius(12);
        packetTable.setEffect(tableGlow);
    }
    
    @FXML
    private void onTableExit() {
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), packetTable);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.play();
        
        // Reset to normal shadow
        DropShadow normalShadow = new DropShadow();
        normalShadow.setColor(Color.web("rgba(0,0,0,0.6)"));
        normalShadow.setRadius(8);
        normalShadow.setOffsetX(4);
        normalShadow.setOffsetY(4);
        packetTable.setEffect(normalShadow);
    }
    
    // Capture button pulse animation
    private void startCaptureButtonPulse() {
        if (captureButtonPulse != null) {
            captureButtonPulse.stop();
        }
        
        captureButtonPulse = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(captureButton.scaleXProperty(), 1.0),
                new KeyValue(captureButton.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000), 
                new KeyValue(captureButton.scaleXProperty(), 1.1),
                new KeyValue(captureButton.scaleYProperty(), 1.1)),
            new KeyFrame(Duration.millis(2000), 
                new KeyValue(captureButton.scaleXProperty(), 1.0),
                new KeyValue(captureButton.scaleYProperty(), 1.0))
        );
        captureButtonPulse.setCycleCount(Timeline.INDEFINITE);
        captureButtonPulse.play();
    }
    
    private void stopCaptureButtonPulse() {
        if (captureButtonPulse != null) {
            captureButtonPulse.stop();
            
            // Reset scale smoothly
            ScaleTransition resetScale = new ScaleTransition(Duration.millis(300), captureButton);
            resetScale.setToX(1.0);
            resetScale.setToY(1.0);
            resetScale.play();
        }
    }

    private void applyFilters() {
        String protocol = protocolFilter.getValue();
        String ip = ipFilter.getText().trim();
        
        // Get filtered packets from database
        ObservableList<PacketData> filteredPackets = FXCollections.observableArrayList(databaseService.getFilteredPackets(protocol, ip));
        
        // Update table
        packets.clear();
        packets.addAll(filteredPackets);
        
        // Update chart based on filtered data
        updateChartFromFilteredData(filteredPackets);
    }
    
    // New method to update chart based on filtered data
    private void updateChartFromFilteredData(ObservableList<PacketData> filteredPackets) {
        if (protocolChart == null) return;
        
        // Count protocols in filtered data
        Map<String, Integer> filteredCounts = new HashMap<>();
        filteredCounts.put("TCP", 0);
        filteredCounts.put("UDP", 0);
        filteredCounts.put("HTTP", 0);
        filteredCounts.put("HTTPS", 0);
        filteredCounts.put("ICMP", 0);
        filteredCounts.put("OTHER", 0);
        
        for (PacketData packet : filteredPackets) {
            String protocol = packet.getProtocol().toUpperCase();
            if (filteredCounts.containsKey(protocol)) {
                filteredCounts.put(protocol, filteredCounts.get(protocol) + 1);
            } else {
                filteredCounts.put("OTHER", filteredCounts.get("OTHER") + 1);
            }
        }
        
        // Update chart with filtered data
        chartData.clear();
        for (Map.Entry<String, Integer> entry : filteredCounts.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
                chartData.add(data);
            }
        }
        
        // Update statistics labels for filtered view
        if (totalPacketsLabel != null) {
            totalPacketsLabel.setText(String.valueOf(filteredPackets.size()));
        }
        if (tcpPacketsLabel != null) {
            int tcpCount = filteredCounts.get("TCP") + filteredCounts.get("HTTP") + filteredCounts.get("HTTPS");
            tcpPacketsLabel.setText(String.valueOf(tcpCount));
        }
        if (udpPacketsLabel != null) {
            udpPacketsLabel.setText(String.valueOf(filteredCounts.get("UDP")));
        }
        if (otherPacketsLabel != null) {
            int otherCount = filteredCounts.get("ICMP") + filteredCounts.get("OTHER");
            otherPacketsLabel.setText(String.valueOf(otherCount));
        }
    }
}