const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

function shortDuration(ms: number) {
  if (ms < MINUTE) return "under a minute";
  if (ms < HOUR) return `${Math.floor(ms / MINUTE)}m`;
  if (ms < DAY) return `${Math.floor(ms / HOUR)}h`;
  return `${Math.floor(ms / DAY)}d`;
}

export const timeLeft = (expiresAt: string) =>
  `${shortDuration(new Date(expiresAt).getTime() - Date.now())} left`;

export const timeAgo = (date: string) =>
  `${shortDuration(Date.now() - new Date(date).getTime())} ago`;
