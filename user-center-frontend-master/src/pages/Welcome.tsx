import { CalendarOutlined, CheckCircleOutlined, ProjectOutlined } from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, Card, Col, Row, Space, Typography } from 'antd';
import { history } from 'umi';

const { Paragraph, Text, Title } = Typography;

const Welcome = () => (
  <PageContainer title={false}>
    <Card bordered={false} style={{ marginBottom: 24 }}>
      <Title level={2}>Welcome to Co-op Tracker</Title>
      <Paragraph type="secondary" style={{ maxWidth: 680, fontSize: 16 }}>
        Keep every co-op and internship opportunity organized from the first application to the
        final decision.
      </Paragraph>
      <Button type="primary" size="large" onClick={() => history.push('/applications')}>
        View applications
      </Button>
    </Card>

    <Row gutter={[16, 16]}>
      <Col xs={24} md={8}>
        <Card>
          <Space align="start">
            <ProjectOutlined style={{ color: '#1677ff', fontSize: 24 }} />
            <div>
              <Text strong>Track opportunities</Text>
              <Paragraph type="secondary">
                Store the company, role, posting link, location, and work arrangement.
              </Paragraph>
            </div>
          </Space>
        </Card>
      </Col>
      <Col xs={24} md={8}>
        <Card>
          <Space align="start">
            <CalendarOutlined style={{ color: '#13c2c2', fontSize: 24 }} />
            <div>
              <Text strong>Never miss a follow-up</Text>
              <Paragraph type="secondary">
                Record deadlines, interview dates, follow-ups, and your next action.
              </Paragraph>
            </div>
          </Space>
        </Card>
      </Col>
      <Col xs={24} md={8}>
        <Card>
          <Space align="start">
            <CheckCircleOutlined style={{ color: '#52c41a', fontSize: 24 }} />
            <div>
              <Text strong>See your progress</Text>
              <Paragraph type="secondary">
                Move applications through assessment, interview, offer, and final outcomes.
              </Paragraph>
            </div>
          </Space>
        </Card>
      </Col>
    </Row>
  </PageContainer>
);

export default Welcome;
