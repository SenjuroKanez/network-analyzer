package com.networkanalyzer.controller;

import com.networkanalyzer.model.PacketData;
import com.networkanalyzer.service.DatabaseService;
import com.networkanalyzer.service.PacketCaptureService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainController {
    @FXML private TableView<PacketData> packetTable;
    @FXML private TableColumn<PacketData, String> timestampColumn;
    @FXML private TableColumn<PacketData, String> sourceIpColumn;
    @FXML private TableColumn<PacketData, String> destIpColumn;
    @FXML private TableColumn<PacketData, String> protocolColumn;
    @FXML private TableColumn<PacketData, Integer> sizeColumn;
    @FXML private ComboBox<String> protocolFilter;
    @FXML private TextField ipFilter;
    @FXML private Button captureButton;
    
    private PacketCaptureService captureService;
    private DatabaseService databaseService;
    private ObservableList<PacketData> packets;
    private boolean isCapturing = false;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupServices();
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
            "All", "TCP", "UDP", "HTTP", "HTTPS"
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
            });
        });
    }

    @FXML
    private void toggleCapture() {
        if (!isCapturing) {
            captureService.startCapture();
            captureButton.setText("Stop Capture");
        } else {
            captureService.stopCapture();
            captureButton.setText("Start Capture");
        }
        isCapturing = !isCapturing;
    }

    private void applyFilters() {
        String protocol = protocolFilter.getValue();
        String ip = ipFilter.getText().trim();
        
        packets.clear();
        packets.addAll(databaseService.getFilteredPackets(protocol, ip));
    }
}