const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// Change these to your MySQL credentials
const db = mysql.createConnection({
  host: 'localhost',
  user: 'root', // your MySQL username
  password: 'mateenbhaipayara', // your MySQL password
  database: 'netlyzer' // your database name
});

// Create table if it doesn't exist
db.query(`
  CREATE TABLE IF NOT EXISTS packets (
    id VARCHAR(32) PRIMARY KEY,
    sourceIP VARCHAR(32),
    destinationIP VARCHAR(32),
    protocol VARCHAR(16),
    timestamp BIGINT,
    size INT
  )
`);

app.post('/api/packets', (req, res) => {
  const { id, sourceIP, destinationIP, protocol, timestamp, size } = req.body;
  db.query(
    'INSERT INTO packets (id, sourceIP, destinationIP, protocol, timestamp, size) VALUES (?, ?, ?, ?, ?, ?)',
    [id, sourceIP, destinationIP, protocol, timestamp, size],
    (err) => {
      if (err) return res.status(500).json({ error: err });
      res.json({ success: true });
    }
  );
});

app.listen(4000, () => console.log('Server running on port 4000'));