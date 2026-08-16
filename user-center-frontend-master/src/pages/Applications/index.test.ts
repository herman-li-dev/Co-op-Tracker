import { reminderFor } from './reminder';

describe('application reminders', () => {
  test('marks overdue and upcoming deadlines', () => {
    const today = new Date();
    const date = (days: number) => {
      const value = new Date(today);
      value.setDate(value.getDate() + days);
      return value.toISOString().slice(0, 10);
    };
    expect(reminderFor(date(-1), 'deadline')).toEqual({ text: 'Overdue', color: 'red' });
    expect(reminderFor(date(3), 'deadline')).toEqual({ text: 'Due soon', color: 'orange' });
    expect(reminderFor(date(10), 'deadline')).toBeUndefined();
  });

  test('marks follow-ups due today or earlier', () => {
    const today = new Date();
    const date = (days: number) => {
      const value = new Date(today);
      value.setDate(value.getDate() + days);
      return value.toISOString().slice(0, 10);
    };
    expect(reminderFor(date(0), 'followUp')).toEqual({ text: 'Follow-up due', color: 'magenta' });
    expect(reminderFor(date(1), 'followUp')).toBeUndefined();
  });
});
