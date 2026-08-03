'use strict';

export function eventComponent(event) {
  return String(event?.component || 'root').trim() || 'root';
}

export function dependencyIdentity(event, library = event?.library) {
  return `${eventComponent(event)}||${String(library || '').trim()}`;
}

export function vulnerabilityInstanceKey(event, vulnerabilityId, library = event?.library) {
  return `${dependencyIdentity(event, library)}||${String(vulnerabilityId || '').trim()}`;
}
