// @ts-ignore
/* eslint-disable */

declare namespace API {
  type CurrentUser = {
    id: number;
    userName?: string;
    userAccount: string;
    avatarUrl?: string;
    gender: number;
    phone: string;
    email: string;
    userStatus: number;
    userRole: number;
    planetCode: string;
    createTime: Date;
  };

  type PageParams = {
    current?: number;
    pageSize?: number;
  };

  type JobApplicationStatus =
    | 'SAVED'
    | 'APPLIED'
    | 'ASSESSMENT'
    | 'INTERVIEW'
    | 'OFFER'
    | 'REJECTED'
    | 'WITHDRAWN';

  type JobApplication = {
    id: number;
    userId: number;
    companyName: string;
    jobTitle: string;
    location?: string;
    jobUrl?: string;
    status: JobApplicationStatus;
    workMode?: string;
    appliedDate?: string;
    deadline?: string;
    nextFollowUpDate?: string;
    nextStep?: string;
    notes?: string;
    createdAt?: string;
    updatedAt?: string;
  };

  type JobApplicationStatusHistory = {
    id: number;
    applicationId: number;
    userId: number;
    fromStatus?: JobApplicationStatus;
    toStatus: JobApplicationStatus;
    changedAt: string;
  };

  type JobApplicationInput = {
    companyName: string;
    jobTitle: string;
    location?: string;
    jobUrl?: string;
    status?: JobApplicationStatus;
    workMode?: string;
    appliedDate?: string;
    deadline?: string;
    nextFollowUpDate?: string;
    nextStep?: string;
    notes?: string;
  };

  type JobApplicationUpdateInput = JobApplicationInput & {
    id: number;
  };

  type JobApplicationQueryParams = PageParams & {
    companyName?: string;
    jobTitle?: string;
    status?: JobApplicationStatus;
    appliedDateStart?: string;
    appliedDateEnd?: string;
    deadlineStart?: string;
    deadlineEnd?: string;
    sortField?: string;
    sortOrder?: 'ascend' | 'descend';
  };

  type JobApplicationPage = {
    records: JobApplication[];
    total: number;
    size: number;
    current: number;
    pages: number;
  };

  type ApplicationDashboard = {
    total: number;
    applied: number;
    interviews: number;
    offers: number;
    rejected: number;
    interviewRate: number;
    offerRate: number;
    upcomingDeadlines: number;
    followUpsDue: number;
    recentApplications: JobApplication[];
    statusDistribution: ApplicationStatusCount[];
    weeklyTrend: WeeklyApplicationCount[];
  };

  type ApplicationStatusCount = {
    status: JobApplicationStatus;
    count: number;
  };

  type WeeklyApplicationCount = {
    weekStart: string;
    count: number;
  };

  type BaseResponse<T> = {
    code: number;
    data: T;
    message: string;
    description: string;
  };

  type LoginParams = {
    userAccount?: string;
    userPassword?: string;
    autoLogin?: boolean;
    type?: string;
  };

  type RegisterParams = {
    userAccount?: string;
    userPassword?: string;
    checkPassword?: string;
    planetCode?: string;
    type?: string;
  };

}
