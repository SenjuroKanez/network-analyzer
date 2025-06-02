package com.networkanalyzer.service;

import com.networkanalyzer.model.PacketData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String URL = "jdbc:mysql://localhost:3306/netlyzer";
    private static final String USER = "root";
    private static final String PASSWORD = "mateenbhaipayara";

    public DatabaseService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String createTable = """
                CREATE TABLE IF NOT EXISTS traffic_data (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    timestamp DATETIME,
                    source_ip VARCHAR(45),
                    destination_ip VARCHAR(45),
                    protocol VARCHAR(10),
                    size INT
                )
            """;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTable);
            }
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePacket(PacketData packet) {
        String sql = "INSERT INTO traffic_data (timestamp, source_ip, destination_ip, protocol, size) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(packet.getTimestamp()));
            pstmt.setString(2, packet.getSourceIp());
            pstmt.setString(3, packet.getDestinationIp());
            pstmt.setString(4, packet.getProtocol());
            pstmt.setInt(5, packet.getSize());
            
            pstmt.executeUpdate();
            // Debugging output
        System.out.println("Packet saved: " + packet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PacketData> getFilteredPackets(String protocol, String ip) {
        List<PacketData> packets = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM traffic_data WHERE 1=1");
        
        if (protocol != null && !protocol.equals("All")) {
            sql.append(" AND protocol = ?");
        }
        if (ip != null && !ip.isEmpty()) {
            sql.append(" AND (source_ip LIKE ? OR destination_ip LIKE ?)");
        }
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (protocol != null && !protocol.equals("All")) {
                pstmt.setString(paramIndex++, protocol);
            }
            if (ip != null && !ip.isEmpty()) {
                String ipPattern = "%" + ip + "%";
                pstmt.setString(paramIndex++, ipPattern);
                pstmt.setString(paramIndex, ipPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                packets.add(new PacketData(
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getString("source_ip"),
                    rs.getString("destination_ip"),
                    rs.getString("protocol"),
                    rs.getInt("size")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return packets;
    }
}