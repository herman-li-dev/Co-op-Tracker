import moment from 'moment';
import { toRequestBody } from './applicationForm';
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

describe('application request payload', () => {
  test('serializes selected dates without leaking Moment objects', () => {
    expect(
      toRequestBody({
        companyName: 'Northstar Labs',
        jobTitle: 'Software Developer Intern',
        status: 'APPLIED',
        appliedDate: moment('2026-08-04'),
        deadline: moment('2026-08-20'),
        nextFollowUpDate: moment('2026-08-16'),
      }),
    ).toMatchObject({
      appliedDate: '2026-08-04',
      deadline: '2026-08-20',
      nextFollowUpDate: '2026-08-16',
    });
  });

  test('keeps optional dates undefined when they are not selected', () => {
    expect(
      toRequestBody({
        companyName: 'Maple Analytics',
        jobTitle: 'Data Engineering Co-op',
        status: 'SAVED',
      }),
    ).toMatchObject({
      appliedDate: undefined,
      deadline: undefined,
      nextFollowUpDate: undefined,
    });
  });
});
