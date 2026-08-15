import { useRef } from 'react';
import type { ProColumns, ActionType } from '@ant-design/pro-table';
import ProTable from '@ant-design/pro-table';
import { deleteUser, searchUsers } from "@/services/ant-design-pro/api";
import {Image, message, Popconfirm} from "antd";

const columns: ProColumns<API.CurrentUser>[] = [
  {
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: 'Name',
    dataIndex: 'userName',
    copyable: true,
  },
  {
    title: 'Account',
    dataIndex: 'userAccount',
    copyable: true,
  },
  {
    title: 'Avatar',
    dataIndex: 'avatarUrl',
    render: (_, record) => (
      <div>
        <Image src={record.avatarUrl} width={100} />
      </div>
    ),
  },
  {
    title: 'Gender',
    dataIndex: 'gender',
  },
  {
    title: 'Phone',
    dataIndex: 'phone',
    copyable: true,
  },
  {
    title: 'Email',
    dataIndex: 'email',
    copyable: true,
  },
  {
    title: 'Status',
    dataIndex: 'userStatus',
  },
  {
    title: 'Role',
    dataIndex: 'userRole',
    valueType: 'select',
    valueEnum: {
      0: { text: 'User', status: 'Default' },
      1: {
        text: 'Administrator',
        status: 'Success',
      },
    },
  },
  {
    title: 'Created',
    dataIndex: 'createTime',
    valueType: 'dateTime',
  },
  {
    title: 'Actions',
    valueType: 'option',
    render: (_, record, __, action) => [
      <Popconfirm
        key="delete"
        title="Delete this user?"
        okText="Delete"
        cancelText="Cancel"
        onConfirm={async () => {
          const success = await deleteUser(record.id);
          if (success) {
            message.success('User deleted');
            action?.reload();
          }
        }}
      >
        <a>Delete</a>
      </Popconfirm>,
    ],
  },
];

export default () => {
  const actionRef = useRef<ActionType>();
  return (
    <ProTable<API.CurrentUser>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      request={async () => {
        const userList = await searchUsers();
        return {
          data: userList,
          total: userList.length,
          success: true,
        }
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={false}
      form={{
        // 由于配置了 transform，提交的参与与定义的不同这里需要转化一下
        syncToUrl: (values, type) => {
          if (type === 'get') {
            return {
              ...values,
              created_at: [values.startTime, values.endTime],
            };
          }
          return values;
        },
      }}
      pagination={{
        pageSize: 5,
      }}
      dateFormatter="string"
      headerTitle="Users"
    />
  );
};
