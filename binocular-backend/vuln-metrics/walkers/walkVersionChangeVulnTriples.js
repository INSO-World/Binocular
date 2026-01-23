'use strict';

import VersionChangeEventVulnerabilityConnection from '../../models/VersionChangeEventVulnerabilityConnection.js';

/**
 * Async generator: yields { event, conn, vuln, eventDate }.
 */
export async function* walkVersionChangeVulnTriples(branch, opts = {}) {
  const cursor = await VersionChangeEventVulnerabilityConnection.walkTriples({ branch, ...opts });

  for await (const row of cursor) {
    const event = row.event;
    const conn = row.conn;
    const vuln = row.vuln;

    const eventDate = new Date(Number(event.timestamp) * 1000);

    yield { event, conn, vuln, eventDate };
  }
}

export default walkVersionChangeVulnTriples;
