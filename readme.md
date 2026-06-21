
---

# Network Analyzer

## Overview

Network Analyzer is a powerful application for capturing, monitoring, and analyzing network traffic on your local machine. With both a Java-based desktop application and a modern React web interface, it provides comprehensive visibility into your network communications.

## Features

* **Real-time Packet Capture**: Monitor network traffic as it happens
* **Protocol Analysis**: Identify TCP, UDP, and other protocols
* **IP Filtering**: Filter traffic by source or destination IP address
* **Size Analysis**: View packet sizes for bandwidth usage analysis
* **Timestamp Tracking**: Monitor traffic patterns over time
* **Dual Interface**: Choose between JavaFX desktop app or React web interface
* **Database Integration**: Store captures for historical analysis

## System Architecture

### Backend

* Java-based packet capture engine
* JavaFX GUI for desktop application
* SLF4J for logging
* MySQL database for storing packet data

### Frontend

* React/TypeScript web interface
* Vite for fast development and building
* Modern, responsive UI
* Real-time updates of network data

## Getting Started

### Prerequisites

* Java 11 or higher
* Node.js 16 or higher
* Maven or similar Java build tool
* npm or yarn for frontend dependencies
* **Npcap**: Required for capturing packets over Wi-Fi and Ethernet interfaces (Windows).
* **MySQL Server**: Required for the backend database integration.

### Installation

#### Backend Setup

1. **Clone the repository**:
```bash
git clone https://github.com/SenjuroKanez/network-analyzer
cd network-analyzer

```


2. **System Dependencies**:
* **Install Npcap**: Download and install [Npcap](https://npcap.com/) to ensure the application can correctly capture traffic from your Wi-Fi and Ethernet adapters.
* **Install MySQL**: Ensure MySQL Server is installed and running on your machine. Create a new database named `netlyzer`.

In sql command line client
```bash
CREATE DATABASE netlyzer;
```

3. **Configure Database Credentials**:
Navigate to `src\main\java\com\networkanalyzer\service\DatabaseService.java` in your code editor and update the `PASSWORD` variable (and `USER` if necessary) to match your local MySQL setup:
```java
public class DatabaseService {
    private static final String URL = "jdbc:mysql://localhost:3306/netlyzer";
    private static final String USER = "root";
    private static final String PASSWORD = "asdf"; // Change "asdf" to your actual MySQL password
// ...

```


4. **Build the Java application**:
```bash
mvn clean package

```


5. **Run the Java application**:
```bash
java -jar target/network-analyzer-1.0-SNAPSHOT.jar

```



#### Frontend Setup

1. **Navigate to the frontend directory**:
```bash
cd frontend

```


2. **Install dependencies**:
```bash
npm install

```


3. **Start the development server**:
```bash
npm run dev

```


4. The application will be available at `http://localhost:5173`

## Usage

### Desktop Application

1. Launch the application using the JAR file
2. Click "Start Capture" to begin monitoring network traffic
3. Use the "Filter by IP" field to narrow down traffic sources
4. Select filters from the dropdown to view specific types of traffic

### Web Interface

1. Navigate to `http://localhost:5173` in your browser
2. Use the "Protocol" filter to select specific protocols
3. Enter IP addresses in the filter field to narrow down traffic
4. Click "Start Capture" to begin monitoring
5. View packet information in real-time in the table

## Technical Details

### Packet Capture

The application uses native Java libraries to capture network packets at the OS level. This provides high-performance packet interception without requiring elevated privileges for many basic monitoring tasks. **Note:** Ensure Npcap is installed to utilize Wi-Fi and Ethernet capture properly.

### Data Structure

Each packet is represented as a `PacketData` object containing:

* Timestamp
* Source IP
* Destination IP
* Protocol
* Size (in bytes)
* Raw packet data (when available)

### Protocol Support

* TCP: Full support with connection tracking
* UDP: Datagram monitoring
* Additional protocols can be added via the plugin system

## Development

### Build Process

The project uses Maven for Java builds and npm for frontend assets. The complete build process:

```bash
mvn clean package
cd frontend
npm run build

```

The built application will include both the JavaFX desktop application and the embedded web server for the React interface.

### Architecture

* Java backend captures packets and stores them in the local MySQL database
* JavaFX UI reads from the database for the desktop view
* Web server exposes REST API for React frontend
* React frontend consumes API and displays data

## Troubleshooting

### Common Issues

1. **Unable to access jarfile**: Ensure you're specifying the correct path to the JAR file.
2. **Database connection failed**: Check that MySQL is running, the `netlyzer` database is created, and your credentials in `DatabaseService.java` are correct.
3. **Packet capture not working / No interfaces found**: Ensure Npcap is installed with the correct options for your OS. Some operations may also require running the app with admin/root privileges.
4. **JavaFX warnings**: These can typically be ignored if the application is functioning.
5. **SLF4J warnings**: The application will default to a NOP logger if SLF4J implementation is missing.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Acknowledgments

* [pcap4j](https://github.com/kaitoy/pcap4j) - Core packet capture library
* [Npcap](https://npcap.com/) - Windows packet capture library
* [JavaFX](https://openjfx.io/) - UI framework for desktop application
* [React](https://reactjs.org/) - Frontend web framework
* [Vite](https://vitejs.dev/) - Frontend build tool