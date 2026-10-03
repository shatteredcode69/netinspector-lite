const $ = (id) => document.getElementById(id);
const latencySamples = [];
const events = [];

function setText(id, value) { $(id).textContent = value || '--'; }

function addEvent(message, tone = '') {
  events.unshift({ message, tone, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
  events.splice(4);
  $('events').innerHTML = events.map((event) => `<div class="event"><i class="event-dot ${event.tone}"></i><span>${event.message}</span><time>${event.time}</time></div>`).join('');
}

function drawPulse() {
  const canvas = $('pulseChart');
  const context = canvas.getContext('2d');
  const width = canvas.clientWidth * devicePixelRatio;
  const height = 150 * devicePixelRatio;
  canvas.width = width;
  canvas.height = height;
  context.clearRect(0, 0, width, height);
  context.strokeStyle = 'rgba(23,33,31,.1)';
  context.lineWidth = devicePixelRatio;
  [35, 75, 115].forEach((line) => { context.beginPath(); context.moveTo(0, line * devicePixelRatio); context.lineTo(width, line * devicePixelRatio); context.stroke(); });
  if (latencySamples.length < 2) return;
  const max = Math.max(...latencySamples, 20);
  const min = Math.min(...latencySamples, 0);
  context.beginPath();
  latencySamples.forEach((sample, index) => {
    const x = (index / Math.max(latencySamples.length - 1, 1)) * width;
    const y = (height - 18 * devicePixelRatio) - ((sample - min) / Math.max(max - min, 1)) * (height - 34 * devicePixelRatio);
    index ? context.lineTo(x, y) : context.moveTo(x, y);
  });
  context.strokeStyle = '#00796b';
  context.lineWidth = 2 * devicePixelRatio;
  context.stroke();
}

async function loadStatus() {
  const started = performance.now();
  try {
    const response = await fetch('/api/status');
    const data = await response.json();
    const elapsed = Math.round(performance.now() - started);
    latencySamples.push(elapsed);
    if (latencySamples.length > 18) latencySamples.shift();
    setText('latency', `${elapsed} ms`); setText('pulseValue', elapsed); setText('pulseState', `${latencySamples.length} samples / last ${elapsed} ms`); drawPulse();
    setText('platform', data.platform?.toUpperCase());
    setText('gateway', data.gateway || 'Gateway not found');
    setText('gatewayMini', data.gateway || 'NO ROUTE'); setText('agentState', 'ONLINE');
    const first = data.interfaces?.[0];
    setText('localIp', first?.address);
    setText('interface', first?.name);
    addEvent(`Route confirmed through ${data.gateway || 'an unknown gateway'}.`, 'good');
    setText('lastUpdated', `UPDATED ${new Date().toLocaleTimeString()}`);
  } catch {
    setText('gateway', 'Local agent required'); setText('gatewayMini', 'LOCAL ONLY'); setText('agentState', 'OFFLINE');
    $('auditButton').disabled = true;
    $('auditSummary').textContent = 'Open the local Node agent to scan your private gateway.';
    $('auditBadge').textContent = 'LOCAL ONLY';
    addEvent('Hosted mode detected. Private gateway probes are unavailable here.', 'warn');
  }
}

async function loadIsp() {
  try {
    const response = await fetch('/api/isp');
    const data = await response.json();
    if (!response.ok) throw new Error(data.error);
    setText('publicIp', data.ip);
    setText('publicMini', data.ip);
    setText('organization', data.org || 'Organization unavailable');
    setText('location', [data.city, data.region, data.country].filter(Boolean).join(', ') || 'Location unavailable');
    setText('asn', data.asn || 'ASN unavailable');
    addEvent(`Public identity resolved through ${data.org || 'an unknown provider'}.`, 'good');
  } catch { setText('publicIp', 'Lookup failed'); setText('publicMini', 'NO DATA'); setText('organization', 'Check your internet connection'); addEvent('Public identity lookup failed.', 'bad'); }
}

async function runAudit() {
  const button = $('auditButton');
  button.disabled = true;
  button.firstChild.textContent = 'Scanning... ';
  $('auditBadge').textContent = 'WORKING';
  $('auditSummary').textContent = 'Testing TCP connections to the detected gateway.';
  try {
    const response = await fetch('/api/audit');
    const data = await response.json();
    if (!response.ok) throw new Error(data.error);
    const open = data.ports.filter((port) => port.status === 'OPEN').length;
    const filtered = data.ports.filter((port) => port.status === 'FILTERED').length;
    const posture = open === 0 ? 'QUIET' : open < 3 ? 'WATCH' : 'EXPOSED';
    $('auditSummary').textContent = `Gateway ${data.gateway} · ${open} open / ${filtered} filtered`;
    $('auditBadge').textContent = 'COMPLETE';
    $('score').className = `score ${posture.toLowerCase()}`; $('score strong').textContent = posture;
    $('ports').innerHTML = data.ports.map((port) => `<div class="port ${port.status.toLowerCase()}"><strong>${port.status}</strong><span>PORT ${port.port}</span></div>`).join('');
    addEvent(`Exposure map complete: posture is ${posture.toLowerCase()}.`, open ? 'warn' : 'good');
  } catch (error) {
    $('auditBadge').textContent = 'ERROR';
    $('auditSummary').textContent = 'Local agent unavailable. Start Node and open localhost:4173.';
    addEvent(error.message || 'Gateway audit could not start.', 'bad');
  } finally { button.disabled = false; button.firstChild.textContent = 'Run audit '; }
}

$('auditButton').addEventListener('click', runAudit);
window.addEventListener('resize', drawPulse);
setText('sessionId', Math.random().toString(36).slice(2, 6).toUpperCase());
loadStatus();
loadIsp();
setInterval(loadStatus, 8000);