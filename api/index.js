const express = require('express');
const path = require('path');
const fs = require('fs');
const cors = require('cors');

const app = express();
app.use(cors());

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

// Skip metadata endpoint
app.get('/skip/:imdbId/:season/:episode', (req, res) => {
  const { imdbId, season, episode } = req.params;
  const filePath = path.join(__dirname, 'skip', imdbId, `s${season}e${episode}.json`);

  try {
    if (fs.existsSync(filePath)) {
      const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
      res.json(data);
    } else {
      res.status(404).json({ error: 'Skip metadata not found' });
    }
  } catch (error) {
    res.status(500).json({ error: 'Internal server error' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Skip API server running on port ${PORT}`);
});

module.exports = app;
