import request from '@/plugins/globalRequest';

export async function getApplicationDashboard() {
  return request<API.ApplicationDashboard>('/api/application/dashboard', {
    method: 'GET',
  });
}

export async function listApplications(params: API.JobApplicationQueryParams) {
  return request<API.JobApplicationPage>('/api/application/list', {
    method: 'GET',
    params,
  });
}

export async function getApplication(id: number) {
  return request<API.JobApplication>('/api/application/get', {
    method: 'GET',
    params: { id },
  });
}

export async function getApplicationHistory(id: number) {
  return request<API.JobApplicationStatusHistory[]>('/api/application/history', {
    method: 'GET',
    params: { id },
  });
}

export async function addApplication(body: API.JobApplicationInput) {
  return request<number>('/api/application/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function updateApplication(body: API.JobApplicationUpdateInput) {
  return request<boolean>('/api/application/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
  });
}

export async function deleteApplication(id: number) {
  return request<boolean>('/api/application/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: { id },
  });
}
