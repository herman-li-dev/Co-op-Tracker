import type { Moment } from 'moment';

export type ApplicationFormValues = {
  companyName: string;
  jobTitle: string;
  location?: string;
  jobUrl?: string;
  status: API.JobApplicationStatus;
  workMode?: string;
  appliedDate?: Moment;
  deadline?: Moment;
  nextFollowUpDate?: Moment;
  nextStep?: string;
  notes?: string;
};

const formatDate = (value?: Moment) => value?.format('YYYY-MM-DD');

export const toRequestBody = (values: ApplicationFormValues): API.JobApplicationInput => ({
  ...values,
  appliedDate: formatDate(values.appliedDate),
  deadline: formatDate(values.deadline),
  nextFollowUpDate: formatDate(values.nextFollowUpDate),
});
