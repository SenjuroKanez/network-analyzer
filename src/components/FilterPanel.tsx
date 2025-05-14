import React from 'react';
import { FilterOptions } from '../types';

interface FilterPanelProps {
  filters: FilterOptions;
  onFilterChange: (filters: FilterOptions) => void;
}

export function FilterPanel({ filters, onFilterChange }: FilterPanelProps) {
  return (
    <div className="bg-white p-4 rounded-lg shadow-md">
      <h2 className="text-lg font-medium text-gray-900 mb-4">Filter Packets</h2>
      
      <div className="space-y-4">
        <div>
          <label htmlFor="protocol" className="block text-sm font-medium text-gray-700 mb-1">
            Protocol
          </label>
          <select
            id="protocol"
            value={filters.protocol}
            onChange={(e) => onFilterChange({ ...filters, protocol: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
            <option value="">All Protocols</option>
            <option value="TCP">TCP</option>
            <option value="UDP">UDP</option>
            <option value="HTTP">HTTP</option>
            <option value="HTTPS">HTTPS</option>
          </select>
        </div>
        
        <div>
          <label htmlFor="ipAddress" className="block text-sm font-medium text-gray-700 mb-1">
            IP Address
          </label>
          <input
            type="text"
            id="ipAddress"
            value={filters.ipAddress}
            onChange={(e) => onFilterChange({ ...filters, ipAddress: e.target.value })}
            placeholder="Filter by IP address"
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          />
        </div>
      </div>
    </div>
  );
}
