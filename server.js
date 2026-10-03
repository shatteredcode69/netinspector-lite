const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const net = require('net');
const { execFile } = require('child_process');
const { promisify } = require('util');

const execFileAsync = promisify(execFile);
const root = path.join(__dirname, 'public');
const port = Number(process.env.PORT || 4173);
const probePorts = [53, 22, 80, 443, 8080, 8443];

function localInterfaces() {
  return Object.entries(os.networkInterfaces()).flatMap(([name, entries]) =>
    entries.filter((entry) => entry.family === 'IPv4' && !entry.internal).map((entry) => ({ name, address: entry.address }))
  );
}

async function gatewayAddress() {
  try {
    if (process.platform === 'win32') {
      const { stdout } = await execFileAsync('ipconfig', []);
      const match = stdout.match(/Default Gateway[^\r\n]*:\s*((?:\d{1,3}\.){3}\d{1,3})/i);
      return match?.[1] || null;
    }
    const { stdout } = await execFileAsync('sh', ['-c', "ip route | awk '/default/ {print $3; exit}'"]);
    return stdout.trim() || null;
  } catch {
    return null;
  }
}

function probe(host, targetPort, timeoutMs = 1200) {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    let settled = false;
    const finish = (status) => {
      if (settled) return;
      settled = true;
      socket.destroy();
      resolve({ port: targetPort, status });
    };
    socket.setTimeout(timeoutMs, () => finish('FILTERED'));
    socket.once('connect', () => finish('OPEN'));
    socket.once('error', (error) => finish(error.code === 'ECONNREFUSED' ? 'CLOSED' : 'UNAVAILABLE'));
    socket.connect(targetPort, host);
  });
}

async function jsonResponse(response, body, status = 200) {
  response.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
  response.end(JSON.stringify(body));
}

async function handleApi(request, response, pathname) {
  if (pathname === '/api/status') {
    return jsonResponse(response, { platform: process.platform, interfaces: localInterfaces(), gateway: await gatewayAddress() });
  }
  if (pathname === '/api/isp') {
    try {
      const result = await fetch('https://ipinfo.io/json');
      if (!result.ok) throw new Error(`Provider returned ${result.status}`);
      return jsonResponse(response, await result.json());
    } catch (error) {
      return jsonResponse(response, { error: error.message }, 502);
    }
  }
  if (pathname === '/api/audit') {
    const gateway = await gatewayAddress();
    if (!gateway) return jsonResponse(response, { error: 'No default gateway was detected.' }, 503);
    const ports = await Promise.all(probePorts.map((targetPort) => probe(gateway, targetPort)));
    return jsonResponse(response, { gateway, ports });
  }
  return jsonResponse(response, { error: 'Not found' }, 404);
}

function serveStatic(response, pathname) {
  const requested = pathname === '/' ? '/index.html' : pathname;
  const filePath = path.normalize(path.join(root, requested));
  if (!filePath.startsWith(root)) return jsonResponse(response, { error: 'Forbidden' }, 403);
  fs.readFile(filePath, (error, data) => {
    if (error) return jsonResponse(response, { error: 'Not found' }, 404);
    const contentTypes = { '.html': 'text/html', '.css': 'text/css', '.js': 'text/javascript' };
    response.writeHead(200, { 'Content-Type': `${contentTypes[path.extname(filePath)] || 'application/octet-stream'}; charset=utf-8` });
    response.end(data);
  });
}

http.createServer(async (request, response) => {
  const pathname = new URL(request.url, `http://${request.headers.host}`).pathname;
  if (pathname.startsWith('/api/')) return handleApi(request, response, pathname);
  serveStatic(response, pathname);
}).listen(port, () => console.log(`NetInspector Lite running at http://localhost:${port}`));