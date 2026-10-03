const $ = (id) => document.getElementById(id);

function setText(id, value) { $(id).textContent = value || '--'; }

async function loadStatus() {
  try {
    const response = await fetch('/api/status');
    const data = await response.json();
    setText('platform', data.platform?.toUpperCase());
    setText('gateway', data.gateway || 'Gateway not found');
    const first = data.interfaces?.[0];
    setText('localIp', first?.address);
    setText('interface', first?.name);
  } catch { setText('gateway', 'Agent unavailable'); }
}

async function loadIsp() {
  try {
    const response = await fetch('/api/isp');
    const data = await response.json();
    if (!response.ok) throw new Error(data.error);
    setText('publicIp', data.ip);
    setText('organization', data.org || 'Organization unavailable');
    setText('location', [data.city, data.region, data.country].filter(Boolean).join(', ') || 'Location unavailable');
    setText('asn', data.asn || 'ASN unavailable');
  } catch { setText('publicIp', 'Lookup failed'); setText('organization', 'Check your internet connection'); }
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
    $('auditSummary').textContent = `Gateway ${data.gateway} · ${data.ports.filter((port) => port.status === 'OPEN').length} open ports found`;
    $('auditBadge').textContent = 'COMPLETE';
    $('ports').innerHTML = data.ports.map((port) => `<div class="port ${port.status.toLowerCase()}"><strong>${port.status}</strong><span>PORT ${port.port}</span></div>`).join('');
  } catch (error) {
    $('auditBadge').textContent = 'ERROR';
    $('auditSummary').textContent = error.message;
  } finally { button.disabled = false; button.firstChild.textContent = 'Run audit '; }
}

$('auditButton').addEventListener('click', runAudit);
loadStatus();
loadIsp();