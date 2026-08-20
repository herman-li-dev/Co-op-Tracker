import moment from 'moment';

export const reminderFor = (date?: string, kind?: 'deadline' | 'followUp') => {
  if (!date) return undefined;
  const today = moment().startOf('day');
  const target = moment(date, 'YYYY-MM-DD').startOf('day');
  if (kind === 'deadline') {
    if (target.isBefore(today)) return { text: 'Overdue', color: 'red' };
    if (target.diff(today, 'days') <= 7) return { text: 'Due soon', color: 'orange' };
  }
  if (kind === 'followUp' && target.isSameOrBefore(today, 'day')) {
    return { text: 'Follow-up due', color: 'magenta' };
  }
  return undefined;
};
