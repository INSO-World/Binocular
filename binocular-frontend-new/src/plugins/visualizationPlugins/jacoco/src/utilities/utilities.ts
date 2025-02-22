/**
 * Formats a date string to a human-readable date string.
 * @param date - The date string to format.
 */
export function formatDate(date: string): string {
  return new Date(date).toLocaleString('de-DE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}
