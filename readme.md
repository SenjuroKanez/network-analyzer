# Network Analyzer

![Network Analyzer](https://github.com/SenjuroKanez/network-analyzer)

## Overview

Network Analyzer is a powerful application for capturing, monitoring, and analyzing network traffic on your local machine. With both a Java-based desktop application and a modern React web interface, it provides comprehensive visibility into your network communications.

## Features

- **Real-time Packet Capture**: Monitor network traffic as it happens
- **Protocol Analysis**: Identify TCP, UDP, and other protocols
- **IP Filtering**: Filter traffic by source or destination IP address
- **Size Analysis**: View packet sizes for bandwidth usage analysis
- **Timestamp Tracking**: Monitor traffic patterns over time
- **Dual Interface**: Choose between JavaFX desktop app or React web interface
- **Database Integration**: Store captures for historical analysis

## System Architecture

### Backend
- Java-based packet capture engine
- JavaFX GUI for desktop application
- SLF4J for logging
- Embedded database for storing packet data

### Frontend
- React/TypeScript web interface
- Vite for fast development and building
- Modern, responsive UI
- Real-time updates of network data

## Getting Started

### Prerequisites
- Java 11 or higher
- Node.js 16 or higher
- Maven or similar Java build tool
- npm or yarn for frontend dependencies

### Installation

#### Backend Setup

1. Clone the repository:
   ```
   git clone https://github.com/SenjuroKanez/network-analyzer
   cd network-analyzer
   ```

2. Build the Java application:
   ```
   mvn clean package
   ```

3. Run the Java application:
   ```
   java -jar target/network-analyzer-1.0-SNAPSHOT.jar
   ```

#### Frontend Setup

1. Navigate to the frontend directory:
   ```
   cd frontend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the development server:
   ```
   npm run dev
   ```

4. The application will be available at http://localhost:5173

## Usage

### Desktop Application

1. Launch the application using the JAR file
2. Click "Start Capture" to begin monitoring network traffic
3. Use the "Filter by IP" field to narrow down traffic sources
4. Select filters from the dropdown to view specific types of traffic

### Web Interface

1. Navigate to http://localhost:5173 in your browser
2. Use the "Protocol" filter to select specific protocols
3. Enter IP addresses in the filter field to narrow down traffic
4. Click "Start Capture" to begin monitoring
5. View packet information in real-time in the table

## Technical Details

### Packet Capture
The application uses native Java libraries to capture network packets at the OS level. This provides high-performance packet interception without requiring elevated privileges for many basic monitoring tasks.

### Data Structure
Each packet is represented as a `PacketData` object containing:
- Timestamp
- Source IP
- Destination IP
- Protocol
- Size (in bytes)
- Raw packet data (when available)

### Protocol Support
- TCP: Full support with connection tracking
- UDP: Datagram monitoring
- Additional protocols can be added via the plugin system

## Development

### Build Process
The project uses Maven for Java builds and npm for frontend assets. The complete build process:

```
mvn clean package
cd frontend
npm run build
```

The built application will include both the JavaFX desktop application and the embedded web server for the React interface.

### Architecture
- Java backend captures packets and stores in local database
- JavaFX UI reads from database for desktop view
- Web server exposes REST API for React frontend
- React frontend consumes API and displays data

## Troubleshooting

### Common Issues

1. **Unable to access jarfile**: Ensure you're specifying the correct path to the JAR file
2. **Database connection failed**: Check that no other application is using the database port
3. **Packet capture not working**: Some operations may require admin/root privileges
4. **JavaFX warnings**: These can typically be ignored if the application is functioning
5. **SLF4J warnings**: The application will default to a NOP logger if SLF4J implementation is missing

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Acknowledgments

- [pcap4j](https://github.com/kaitoy/pcap4j) - Core packet capture library
- [JavaFX](https://openjfx.io/) - UI framework for desktop application
- [React](https://reactjs.org/) - Frontend web framework
- [Vite](https://vitejs.dev/) - Frontend build tool
