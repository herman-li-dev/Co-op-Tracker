import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { message } from 'antd';
import React from 'react';
import { history, Link } from 'umi';
import { SYSTEM_LOGO } from '@/constants';
import Footer from '@/components/Footer';
import { register } from '@/services/ant-design-pro/api';
import styles from './index.less';
import { LoginForm, ProFormText } from '@ant-design/pro-form';

const Register: React.FC = () => {
  const handleSubmit = async (values: API.RegisterParams) => {
    const { userPassword, checkPassword } = values;
    // 校验
    if (userPassword !== checkPassword) {
      message.error('The passwords do not match');
      return;
    }

    try {
      // 注册
      const id = await register(values);
      if (id) {
        const defaultLoginSuccessMessage = 'Account created successfully';
        message.success(defaultLoginSuccessMessage);

        /** 此方法会跳转到 redirect 参数所在的位置 */
        if (!history) return;
        const { query } = history.location;
        history.push({
          pathname: '/user/login',
          query,
        });
        return;
      }
    } catch (error) {
      const defaultLoginFailureMessage = 'Unable to create the account. Please try again.';
      message.error(defaultLoginFailureMessage);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.content}>
        <LoginForm
          submitter={{
            searchConfig: {
              submitText: 'Create account',
            },
          }}
          logo={<img alt="logo" src={SYSTEM_LOGO} />}
          title="Co-op Application Tracker"
          subTitle="Start organizing your co-op and internship search."
          initialValues={{
            autoLogin: true,
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.RegisterParams);
          }}
        >
          <>
            <ProFormText
              name="userAccount"
              fieldProps={{
                size: 'large',
                prefix: <UserOutlined className={styles.prefixIcon} />,
              }}
              placeholder="Account"
              rules={[
                {
                  required: true,
                  message: 'Enter an account name',
                },
              ]}
            />
            <ProFormText.Password
              name="userPassword"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined className={styles.prefixIcon} />,
              }}
              placeholder="Password"
              rules={[
                {
                  required: true,
                  message: 'Enter a password',
                },
                {
                  min: 8,
                  type: 'string',
                  message: 'Password must be at least 8 characters',
                },
              ]}
            />
            <ProFormText.Password
              name="checkPassword"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined className={styles.prefixIcon} />,
              }}
              placeholder="Confirm password"
              rules={[
                {
                  required: true,
                  message: 'Confirm your password',
                },
                {
                  min: 8,
                  type: 'string',
                  message: 'Password must be at least 8 characters',
                },
              ]}
            />
          </>
        </LoginForm>
        <div style={{ textAlign: 'center', marginTop: 16 }}>
          Already have an account? <Link to="/user/login">Sign in</Link>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Register;
