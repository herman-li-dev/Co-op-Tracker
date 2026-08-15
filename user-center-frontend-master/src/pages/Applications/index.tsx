import { useRef, useState } from 'react';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { Button, DatePicker, Form, Input, message, Modal, Popconfirm, Select, Space } from 'antd';
import type { Moment } from 'moment';
import moment from 'moment';
import {
  addApplication,
  deleteApplication,
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

export default function ApplicationsPage() {
  const actionRef = useRef<ActionType>();
  const [form] = Form.useForm<ApplicationFormValues>();
  const [modalVisible, setModalVisible] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingApplication, setEditingApplication] = useState<API.JobApplication>();

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

  const columns: ProColumns<API.JobApplication>[] = [
    {
      title: 'Company',
      dataIndex: 'companyName',
      copyable: true,
    },
    {
      title: 'Job title',
      dataIndex: 'jobTitle',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: statusValueEnum,
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
    },
    {
      title: 'Deadline',
      dataIndex: 'deadline',
      valueType: 'date',
      hideInSearch: true,
    },
    {
      title: 'Next follow-up',
      dataIndex: 'nextFollowUpDate',
      valueType: 'date',
      hideInSearch: true,
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
      <ProTable<API.JobApplication>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        headerTitle="Job applications"
        toolBarRender={() => [
          <Button key="create" type="primary" onClick={openCreateModal}>
            Add application
          </Button>,
        ]}
        request={async (params) => {
          const page = await listApplications({
            current: params.current,
            pageSize: params.pageSize,
            companyName: params.companyName,
            jobTitle: params.jobTitle,
            status: params.status,
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
        search={{ labelWidth: 'auto' }}
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
              rules={[{ required: true, message: 'Enter the company name' }]}
              style={{ flex: 1 }}
            >
              <Input maxLength={100} />
            </Form.Item>
            <Form.Item
              name="jobTitle"
              label="Job title"
              rules={[{ required: true, message: 'Enter the job title' }]}
              style={{ flex: 1 }}
            >
              <Input maxLength={100} />
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

          <Form.Item name="location" label="Location">
            <Input maxLength={100} placeholder="Vancouver, BC" />
          </Form.Item>
          <Form.Item
            name="jobUrl"
            label="Job posting URL"
            rules={[{ type: 'url', message: 'Enter a valid URL' }]}
          >
            <Input maxLength={500} placeholder="https://..." />
          </Form.Item>

          <Space size="large" style={{ display: 'flex' }} align="start">
            <Form.Item name="appliedDate" label="Applied date" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="deadline" label="Deadline" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="nextFollowUpDate" label="Next follow-up" style={{ flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Form.Item name="nextStep" label="Next step">
            <Input maxLength={255} placeholder="Prepare for the technical interview" />
          </Form.Item>
          <Form.Item name="notes" label="Notes">
            <Input.TextArea maxLength={2000} rows={4} showCount />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
