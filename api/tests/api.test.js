const request = require('supertest');
const app = require('../index');

describe('Skip API', () => {
  test('GET /health returns ok', async () => {
    const response = await request(app).get('/health');
    expect(response.status).toBe(200);
    expect(response.body.status).toBe('ok');
  });

  test('GET /skip existing file returns JSON', async () => {
    const response = await request(app).get('/skip/tt9999999/1/1');
    expect(response.status).toBe(200);
    expect(response.body.imdbId).toBe('tt9999999');
    expect(Array.isArray(response.body.skips)).toBe(true);
  });
});
