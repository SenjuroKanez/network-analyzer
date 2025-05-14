export interface NetworkPacket {
  id: string;
  sourceIP: string;
  destinationIP: string;
  protocol: 'TCP' | 'UDP' | 'HTTP' | 'HTTPS';
  timestamp: number;
  size: number;
}

export interface FilterOptions {
  protocol: string;
  ipAddress: string;
}
