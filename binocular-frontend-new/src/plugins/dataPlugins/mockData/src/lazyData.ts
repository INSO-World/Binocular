// Lazy loaders for the two largest mock data files (commitsFiles, commitsFilesUsers).
// These are fetched and decompressed on demand instead of being bundled statically.
import JSZip from 'jszip';
// ?url imports: Vite serves these as separate asset files at runtime so they
// are not inlined into the single-file bundle (too large for that).
import commitsFilesUrl from './data/commitsFiles.json.zip?url';
import commitsFilesUsersUrl from './data/commitsFilesUsers.json.zip?url';

interface CfEdge {
  _id: string;
  _from: string;
  _to: string;
  stats: { additions: number; deletions: number };
  action: string;
  hunks: { newLines: number; newStart: number; oldLines: number; oldStart: number }[];
}

interface CfuEdge {
  _id: string;
  _from: string;
  _to: string;
  hunks: { oc: string; lines: { from: number; to: number }[] }[];
}

// Cached on first load — these files are large so we only decompress once.
let cfCache: CfEdge[] | null = null;
let cfuCache: CfuEdge[] | null = null;

async function fetchZip<T>(url: string): Promise<T> {
  const response = await fetch(url);
  const buffer = await response.arrayBuffer();
  const zip = await JSZip.loadAsync(buffer);
  const name = Object.keys(zip.files).find((n) => n.endsWith('.json'))!;
  const json = await zip.file(name)!.async('string');
  return JSON.parse(json) as T;
}

export async function loadCommitsFiles(): Promise<CfEdge[]> {
  if (!cfCache) cfCache = await fetchZip<CfEdge[]>(commitsFilesUrl);
  return cfCache;
}

export async function loadCommitsFilesUsers(): Promise<CfuEdge[]> {
  if (!cfuCache) cfuCache = await fetchZip<CfuEdge[]>(commitsFilesUsersUrl);
  return cfuCache;
}

export function groupBy<T>(arr: T[], keyFn: (item: T) => string): Map<string, T[]> {
  const m = new Map<string, T[]>();
  for (const item of arr) {
    const k = keyFn(item);
    const list = m.get(k);
    if (list) list.push(item);
    else m.set(k, [item]);
  }
  return m;
}
