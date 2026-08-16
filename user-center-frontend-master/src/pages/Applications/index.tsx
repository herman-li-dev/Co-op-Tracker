import { useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, DatePicker, Form, Input, message, Modal, Popconfirm, Select, Space, Tag } from 'antd';
import type { Moment } from 'moment';
import moment from 'moment';
import { reminderFor } from './reminder';
import {
  addApplication,
  deleteApplication,
  getApplicationHistory,
  listApplications,
  updateApplication,
} from '@/services/application';

type ApplicationFormValues = {
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

const statusOptions = [
  { label: 'Saved', value: 'SAVED' },
  { label: 'Applied', value: 'APPLIED' },
  { label: 'Assessment', value: 'ASSESSMENT' },
  { label: 'Interview', value: 'INTERVIEW' },
  { label: 'Offer', value: 'OFFER' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'Withdrawn', value: 'WITHDRAWN' },
];

const statusValueEnum = {
  SAVED: { text: 'Saved', status: 'Default' },
  APPLIED: { text: 'Applied', status: 'Processing' },
  ASSESSMENT: { text: 'Assessment', status: 'Warning' },
  INTERVIEW: { text: 'Interview', status: 'Processing' },
  OFFER: { text: 'Offer', status: 'Success' },
  REJECTED: { text: 'Rejected', status: 'Error' },
  WITHDRAWN: { text: 'Withdrawn', status: 'Default' },
};

const formatDate = (value?: Moment) => value?.format('YYYY-MM-DD');

const toRequestBody = (values: ApplicationFormValues): API.JobApplicationInput => ({
  ...values,
  appliedDate: formatDate(values.appliedDate),
  deadline: formatDate(values.deadline),
  nextFollowUpDate: formatDate(values.nextFollowUpDate),
});

export { toRequestBody };

export default function ApplicationsPage() {
  const actionRef = useRef<ActionType>();
  const [form] = Form.useForm<ApplicationFormValues>();
  const [modalVisible, setModalVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingApplication, setEditingApplication] = useState<API.JobApplication>();
  const [historyVisible, setHistoryVisible] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [history, setHistory] = useState<API.JobApplicationStatusHistory[]>([]);

  const openCreateModal = () => {
    setEditingApplication(undefined);
    form.resetFields();
    form.setFieldsValue({ status: 'SAVED' });
    setModalVisible(true);
  };

  const openEditModal = (application: API.JobApplication) => {
    setEditingApplication(application);
    form.setFieldsValue({
      ...application,
      appliedDate: application.appliedDate ? moment(application.appliedDate) : undefined,
      deadline: application.deadline ? moment(application.deadline) : undefined,
      nextFollowUpDate: application.nextFollowUpDate
        ? moment(application.nextFollowUpDate)
        : undefined,
    });
    setModalVisible(true);
  };

  const submitApplication = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      const body = toRequestBody(values);
      const success = editingApplication
        ? await updateApplication({ ...body, id: editingApplication.id })
        : (await addApplication(body)) > 0;
      if (success) {
        message.success(editingApplication ? 'Application updated' : 'Application added');
        setModalVisible(false);
        actionRef.current?.reload();
      }
    } catch (error) {
      if (error instanceof Error) {
        message.error('Unable to save the application');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const removeApplication = async (id: number) => {
    const success = await deleteApplication(id);
    if (success) {
      message.success('Application deleted');
      actionRef.current?.reload();
    }
  };

  const changeStatus = async (application: API.JobApplication, status: API.JobApplicationStatus) => {
    if (status === application.status) return;
    try {
      await updateApplication({ id: application.id, status } as API.JobApplicationUpdateInput);
      message.success('Status updated');
      window.dispatchEvent(new Event('applications-updated'));
      actionRef.current?.reload();
    } catch (error) {
      message.error('Unable to update status');
    }
  };

  const openHistory = async (application: API.JobApplication) => {
    setHistoryVisible(true);
    setHistoryLoading(true);
    try {
      setHistory((await getApplicationHistory(application.id)) ?? []);
    } catch (error) {
      message.error('Unable to load status history');
    } finally {
      setHistoryLoading(false);
    }
  };

  const columns: ProColumns<API.JobApplication>[] = [
    {
      title: 'Company',
      dataIndex: 'companyName',
      copyable: true,
      sorter: true,
    },
    {
      title: 'Job title',
      dataIndex: 'jobTitle',
      sorter: true,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: statusValueEnum,
      sorter: true,
      render: (_, record) => (
        <Select
          value={record.status}
          options={statusOptions}
          size="small"
          onChange={(value: API.JobApplicationStatus) => changeStatus(record, value)}
          style={{ minWidth: 120 }}
        />
      ),
    },
    {
      title: 'Applied date',
      dataIndex: 'appliedDateRange',
      valueType: 'dateRange',
      hideInTable: true,
      search: {
        transform: (value: string[]) => ({
          appliedDateStart: value?.[0],
          appliedDateEnd: value?.[1],
        }),
      },
    },
    {
      title: 'Deadline',
      dataIndex: 'deadlineRange',
      valueType: 'dateRange',
      hideInTable: true,
      search: {
        transform: (value: string[]) => ({
          deadlineStart: value?.[0],
          deadlineEnd: value?.[1],
        }),
      },
    },
    {
      title: 'Location',
      dataIndex: 'location',
      hideInSearch: true,
    },
    {
      title: 'Work mode',
      dataIndex: 'workMode',
      valueEnum: {
        ON_SITE: { text: 'On-site' },
        HYBRID: { text: 'Hybrid' },
        REMOTE: { text: 'Remote' },
      },
      hideInSearch: true,
    },
    {
      title: 'Applied',
      dataIndex: 'appliedDate',
      valueType: 'date',
      hideInSearch: true,
      sorter: true,
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      valueType: 'date',
      hideInSearch: true,
      sorter: true,
      render: (_, record) => {
        const reminder = reminderFor(record.deadline, 'deadline');
        return (
          <Space size={6}>
            <span>{record.deadline || '-'}</span>
            {reminder && <Tag color={reminder.color}>{reminder.text}</Tag>}
          </Space>
        );
      },
    },
    {
      title: 'Next follow-up',
      dataIndex: 'nextFollowUpDate',
      valueType: 'date',
      hideInSearch: true,
      render: (_, record) => {
        const reminder = reminderFor(record.nextFollowUpDate, 'followUp');
        return (
          <Space size={6}>
            <span>{record.nextFollowUpDate || '-'}</span>
            {reminder && <Tag color={reminder.color}>{reminder.text}</Tag>}
          </Space>
        );
      },
    },
    {
      title: 'Next step',
      dataIndex: 'nextStep',
      ellipsis: true,
      hideInSearch: true,
    },
    {
      title: 'Link',
      dataIndex: 'jobUrl',
      hideInSearch: true,
      render: (_, record) =>
        record.jobUrl ? (
          <a href={record.jobUrl} target="_blank" rel="noreferrer">
            Open
          </a>
        ) : (
          '-'
        ),
    },
    {
      title: 'Actions',
      valueType: 'option',
      render: (_, record) => [
        <a key="edit" onClick={() => openEditModal(record)}>
          Edit
        </a>,
        <a key="history" onClick={() => openHistory(record)}>
          History
        </a>,
        <Popconfirm
          key="delete"
          title="Delete this application?"
          okText="Delete"
          cancelText="Cancel"
          onConfirm={() => removeApplication(record.id)}
        >
          <a>Delete</a>
        </Popconfirm>,
      ],
    },
  ];

  return (
    <>
      <ProTable<API.JobApplication, API.JobApplicationQueryParams>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        headerTitle="Job applications"
        toolBarRender={() => [
          <Button key="create" type="primary" onClick={openCreateModal}>
            Add application
          </Button>,
        ]}
        request={async (params, sorter) => {
          const sortEntry = Object.entries(sorter ?? {})[0] as
            | [string, 'ascend' | 'descend']
            | undefined;
          const page = await listApplications({
            current: params.current,
            pageSize: params.pageSize,
            companyName: params.companyName,
            jobTitle: params.jobTitle,
            status: params.status,
            appliedDateStart: params.appliedDateStart,
            appliedDateEnd: params.appliedDateEnd,
            deadlineStart: params.deadlineStart,
            deadlineEnd: params.deadlineEnd,
            sortField: sortEntry?.[0],
            sortOrder: sortEntry?.[1],
          });
          return {
            data: page?.records ?? [],
            total: page?.total ?? 0,
            success: Boolean(page),
          };
        }}
        pagination={{
          defaultPageSize: 10,
          showSizeChanger: true,
        }}
      search={{ labelWidth: 96, span: 4 }}
      />

      <Modal
        title={editingApplication ? 'Edit application' : 'Add application'}
        visible={modalVisible}
        confirmLoading={submitting}
        okText={editingApplication ? 'Save' : 'Add'}
        onOk={submitApplication}
        onCancel={() => setModalVisible(false)}
        destroyOnClose
        width={720}
      >
        <Form form={form} layout="vertical" preserve={false}>
          <Space size="large" style={{ display: 'flex' }} align="start">
            <Form.Item
              name="companyName"
              label="Company"
              rules={[
                { required: true, whitespace: true, message: 'Enter the company name' },
                { max: 128, message: 'Company name cannot exceed 128 characters' },
              ]}
              style={{ flex: 1 }}
            >
              <Input maxLength={128} />
            </Form.Item>
            <Form.Item
              name="jobTitle"
              label="Job title"
              rules={[
                { required: true, whitespace: true, message: 'Enter the job title' },
                { max: 128, message: 'Job title cannot exceed 128 characters' },
              ]}
              style={{ flex: 1 }}
            >
              <Input maxLength={128} />
            </Form.Item>
          </Space>

          <Space size="large" style={{ display: 'flex' }} align="start">
            <Form.Item name="status" label="Status" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
            <Form.Item name="workMode" label="Work mode" style={{ flex: 1 }}>
              <Select
                allowClear
                options={[
                  { label: 'On-site', value: 'ON_SITE' },
                  { label: 'Hybrid', value: 'HYBRID' },
                  { label: 'Remote', value: 'REMOTE' },
                ]}
              />
            </Form.Item>
          </Space>

          <Form.Item
            name="location"
            label="Location"
            rules={[{ max: 128, message: 'Location cannot exceed 128 characters' }]}
          >
            <Input maxLength={128} placeholder="Vancouver, BC" />
          </Form.Item>
          <Form.Item
            name="jobUrl"
            label="Job posting URL"
            rules={[
              { type: 'url', message: 'Enter a valid URL' },
              { pattern: /^https?:\/\//i, message: 'URL must start with http:// or https://' },
              { max: 1024, message: 'URL cannot exceed 1024 characters' },
            ]}
          >
            <Input maxLength={1024} placeholder="https://..." />
          </Form.Item>

          <Space size="large" style={{ display: 'flex' }} align="start">
            <Form.Item name="appliedDate" label="Applied date" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item
              name="deadline"
              label="Deadline"
              dependencies={['appliedDate']}
              rules={[
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    const appliedDate = getFieldValue('appliedDate');
                    if (!value || !appliedDate || !value.isBefore(appliedDate, 'day')) {
                      return Promise.resolve();
                    }
                    return Promise.reject(
                      new Error('Deadline cannot be earlier than the applied date'),
                    );
                  },
                }),
              ]}
              style={{ flex: 1 }}
            >
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="nextFollowUpDate" label="Next follow-up" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Form.Item
            name="nextStep"
            label="Next step"
            rules={[{ max: 255, message: 'Next step cannot exceed 255 characters' }]}
          >
            <Input maxLength={255} placeholder="Prepare for the technical interview" />
          </Form.Item>
          <Form.Item
            name="notes"
            label="Notes"
            rules={[{ max: 2000, message: 'Notes cannot exceed 2000 characters' }]}
          >
            <Input.TextArea maxLength={2000} rows={4} showCount />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Status history"
        visible={historyVisible}
        footer={null}
        onCancel={() => setHistoryVisible(false)}
      >
        <ProTable<API.JobApplicationStatusHistory>
          rowKey="id"
          search={false}
          options={false}
          pagination={false}
          loading={historyLoading}
          dataSource={history}
          columns={[
            { title: 'From', dataIndex: 'fromStatus', render: (value) => value || 'Created' },
            { title: 'To', dataIndex: 'toStatus' },
            { title: 'Changed at', dataIndex: 'changedAt' },
          ]}
        />
      </Modal>
    </>
  );
}
