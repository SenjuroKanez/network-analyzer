import React, { useState, useEffect } from 'react';
import { Activity, Play, Square } from 'lucide-react';
import { NetworkPacket, FilterOptions } from './types';
import { PacketTable } from './components/PacketTable';
import { FilterPanel } from './components/FilterPanel';

function App() {
  const [isCapturing, setIsCapturing] = useState(false);
  const [packets, setPackets] = useState<NetworkPacket[]>([]);
  const [filters, setFilters] = useState<FilterOptions>({
    protocol: '',
    ipAddress: '',
  });

  // Simulate packet capture
  useEffect(() => {
    if (!isCapturing) return;

    const protocols: Array<NetworkPacket['protocol']> = ['TCP', 'UDP', 'HTTP', 'HTTPS'];
    const interval = setInterval(() => {
      const newPacket: NetworkPacket = {
        id: Math.random().toString(36).substr(2, 9),
        sourceIP: `192.168.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
        destinationIP: `10.0.${Math.floor(Math.random() * 255)}.${Math.floor(Math.random() * 255)}`,
        protocol: protocols[Math.floor(Math.random() * protocols.length)],
        timestamp: Date.now(),
        size: Math.floor(Math.random() * 1500) + 64,
      };
      setPackets((prev) => [...prev.slice(-100), newPacket]);
      // Send to backend API to save in MySQL
      fetch('http://localhost:4000/api/packets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newPacket),
      }).catch((err) => {
        // Optionally log error
        console.error('Failed to save packet:', err);
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [isCapturing]);

  const filteredPackets = packets.filter((packet) => {
    if (filters.protocol && packet.protocol !== filters.protocol) return false;
    if (filters.ipAddress &&
        !packet.sourceIP.includes(filters.ipAddress) &&
        !packet.destinationIP.includes(filters.ipAddress)) return false;
    return true;
  });

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-8">
          <Activity className="w-8 h-8 text-indigo-600" />
          <h1 className="text-3xl font-bold text-gray-900">Netlyzer</h1>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
          <div className="md:col-span-1">
            <FilterPanel filters={filters} onFilterChange={setFilters} />
            <div className="mt-4 bg-white p-4 rounded-lg shadow-md">
              <button
                onClick={() => setIsCapturing(!isCapturing)}
                className={`w-full flex items-center justify-center gap-2 px-4 py-2 rounded-md text-white font-medium ${
                  isCapturing ? 'bg-red-600 hover:bg-red-700' : 'bg-green-600 hover:bg-green-700'
                }`}
              >
                {isCapturing ? (
                  <>
                    <Square className="w-4 h-4" />
                    Stop Capture
                  </>
                ) : (
                  <>
                    <Play className="w-4 h-4" />
                    Start Capture
                  </>
                )}
              </button>
              <div className="mt-4 text-sm text-gray-600">
                <p>Packets captured: {packets.length}</p>
                <p>Filtered packets: {filteredPackets.length}</p>
              </div>
            </div>
          </div>
          
          <div className="md:col-span-3">
            <PacketTable packets={filteredPackets} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;