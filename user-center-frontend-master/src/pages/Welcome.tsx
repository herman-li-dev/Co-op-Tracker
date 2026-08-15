import {
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ProjectOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-layout';
import { getApplicationDashboard } from '@/services/application';
import { Button, Card, Col, message, Row, Statistic, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/lib/table';
import React, { useEffect, useState } from 'react';
import { history } from 'umi';

const { Paragraph, Title } = Typography;

const statusColors: Record<API.JobApplicationStatus, string> = {
  SAVED: 'default',
  APPLIED: 'blue',
  ASSESSMENT: 'cyan',
  INTERVIEW: 'purple',
  OFFER: 'green',
  REJECTED: 'red',
  WITHDRAWN: 'default',
};

const recentColumns: ColumnsType<API.JobApplication> = [
  {
    title: 'Company',
    dataIndex: 'companyName',
    key: 'companyName',
  },
  {
    title: 'Job title',
    dataIndex: 'jobTitle',
    key: 'jobTitle',
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    render: (status: API.JobApplicationStatus) => (
      <Tag color={statusColors[status]}>{status.charAt(0) + status.slice(1).toLowerCase()}</Tag>
    ),
  },
  {
    title: 'Applied',
    dataIndex: 'appliedDate',
    key: 'appliedDate',
    render: (value?: string) => value || '-',
  },
  {
    title: 'Deadline',
    dataIndex: 'deadline',
    key: 'deadline',
    render: (value?: string) => value || '-',
  },
];

const Welcome: React.FC = () => {
  const [dashboard, setDashboard] = useState<API.ApplicationDashboard | null>(null);
  const [loading, setLoading] = useState(true);

  const loadDashboard = async () => {
    setLoading(true);
    try {
      const result = await getApplicationDashboard();
      if (result) {
        setDashboard(result);
      }
    } catch (error) {
      message.error('Unable to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  return (
    <PageContainer title={false}>
      <Card bordered={false} style={{ marginBottom: 24 }}>
        <Title level={2}>Application Dashboard</Title>
        <Paragraph type="secondary" style={{ maxWidth: 680, fontSize: 16 }}>
          Track your application pipeline, upcoming deadlines, and follow-up tasks.
        </Paragraph>
        <Button type="primary" onClick={() => history.push('/applications')}>
          Manage applications
        </Button>
      </Card>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={5}>
          <Card loading={loading}>
            <Statistic
              title="Total Applications"
              value={dashboard?.total ?? 0}
              prefix={<ProjectOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={5}>
          <Card loading={loading}>
            <Statistic
              title="Applied"
              value={dashboard?.applied ?? 0}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={5}>
          <Card loading={loading}>
            <Statistic
              title="Interviews"
              value={dashboard?.interviews ?? 0}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={5}>
          <Card loading={loading}>
            <Statistic
              title="Offers"
              value={dashboard?.offers ?? 0}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={4}>
          <Card loading={loading}>
            <Statistic
              title="Rejected"
              value={dashboard?.rejected ?? 0}
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} md={12}>
          <Card loading={loading}>
            <Statistic
              title="Upcoming Deadlines (7 days)"
              value={dashboard?.upcomingDeadlines ?? 0}
              prefix={<CalendarOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card loading={loading}>
            <Statistic
              title="Follow-ups Due"
              value={dashboard?.followUpsDue ?? 0}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="Recent Applications"
        extra={
          <Button type="link" onClick={() => history.push('/applications')}>
            View all
          </Button>
        }
      >
        <Table<API.JobApplication>
          rowKey="id"
          loading={loading}
          columns={recentColumns}
          dataSource={dashboard?.recentApplications ?? []}
          pagination={false}
          scroll={{ x: 720 }}
        />
      </Card>
    </PageContainer>
  );
};

export default Welcome;
