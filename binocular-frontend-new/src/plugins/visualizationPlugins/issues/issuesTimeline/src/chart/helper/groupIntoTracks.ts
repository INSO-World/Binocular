import type { Moment } from 'moment';
import type { MappedDataPluginIssue } from '../types';

export const groupIntoTracks = (issues: MappedDataPluginIssue[], maxDate: Moment) => {
  const tracks: MappedDataPluginIssue[][] = [];

  for (const issue of issues) {
    // Search for a track, that can hold the current issue.
    const openTrack = tracks.find((track) =>
      // A track is considered open if none of its issues overlaps with the new issue.
      // Two intervals [a,b] and [c,d] overlap iff a < d AND c < b.
      // Negated: they don't overlap iff newEnd <= tiStart OR tiEnd <= newStart.
      track.every((ti) => {
        const newEnd = issue.closedAt ?? maxDate;
        const tiEnd = ti.closedAt ?? maxDate;
        return !newEnd.isAfter(ti.createdAt) || !tiEnd.isAfter(issue.createdAt);
      }),
    );

    // If a track was found, append the issue.
    if (openTrack) {
      openTrack.push(issue);
      continue;
    }

    // Otherwise open a new track.
    tracks.push([issue]);
  }

  return tracks;
};
