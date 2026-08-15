import { useEffect, useState } from 'react';
import { Tag, message } from 'antd';
import { groupBy } from 'lodash';
import moment from 'moment';
import { useRequest } from 'umi';
import { getNotices } from '@/services/ant-design-pro/api';

import NoticeIcon from './NoticeIcon';
import styles from './index.less';

export type GlobalHeaderRightProps = {
  fetchingNotices?: boolean;
  onNoticeVisibleChange?: (visible: boolean) => void;
  onNoticeClear?: (tabName?: string) => void;
};

const getNoticeData = (notices: API.NoticeIconItem[]): Record<string, API.NoticeIconItem[]> => {
  if (!notices || notices.length === 0 || !Array.isArray(notices)) {
    return {};
  }

  const newNotices = notices.map((notice) => {
    const newNotice = { ...notice };

    if (newNotice.datetime) {
      newNotice.datetime = moment(notice.datetime as string).fromNow();
    }

    if (newNotice.id) {
      newNotice.key = newNotice.id;
    }

    if (newNotice.extra && newNotice.status) {
      const color = {
        todo: '',
        processing: 'blue',
        urgent: 'red',
        doing: 'gold',
      }[newNotice.status];
      newNotice.extra = (
        <Tag
          color={color}
          style={{
            marginRight: 0,
          }}
        >
          {newNotice.extra}
        </Tag>
      ) as any;
    }

    return newNotice;
  });
  return groupBy(newNotices, 'type');
};

const getUnreadData = (noticeData: Record<string, API.NoticeIconItem[]>) => {
  const unreadMsg: Record<string, number> = {};
  Object.keys(noticeData).forEach((key) => {
    const value = noticeData[key];

    if (!unreadMsg[key]) {
      unreadMsg[key] = 0;
    }

    if (Array.isArray(value)) {
      unreadMsg[key] = value.filter((item) => !item.read).length;
    }
  });
  return unreadMsg;
};

const NoticeIconView: React.FC = () => {
  const [notices, setNotices] = useState<API.NoticeIconItem[]>([]);
  const { data } = useRequest(getNotices);

  useEffect(() => {
    setNotices(data || []);
  }, [data]);

  const noticeData = getNoticeData(notices);
  const unreadMsg = getUnreadData(noticeData || {});

  const changeReadState = (id: string) => {
    setNotices(
      notices.map((item) => {
        const notice = { ...item };
        if (notice.id === id) {
          notice.read = true;
        }
        return notice;
      }),
    );
  };

  const clearReadState = (title: string, key: string) => {
    setNotices(
      notices.map((item) => {
        const notice = { ...item };
        if (notice.type === key) {
          notice.read = true;
        }
        return notice;
      }),
    );
    message.success(`Cleared ${title}`);
  };

  return (
    <NoticeIcon
      className={styles.action}
      count={Object.values(unreadMsg).reduce((total, count) => total + count, 0)}
      onItemClick={(item) => {
        changeReadState(item.id!);
      }}
      onClear={(title: string, key: string) => clearReadState(title, key)}
      loading={false}
      clearText="Clear"
      viewMoreText="View more"
      onViewMore={() => message.info('Click on view more')}
      clearClose
    >
      <NoticeIcon.Tab
        tabKey="notification"
        count={unreadMsg.notification}
        list={noticeData.notification}
        title="Notifications"
        emptyText="You have viewed all notifications"
        showViewMore
      />
      <NoticeIcon.Tab
        tabKey="message"
        count={unreadMsg.message}
        list={noticeData.message}
        title="Messages"
        emptyText="You have read all messages"
        showViewMore
      />
      <NoticeIcon.Tab
        tabKey="event"
        title="Tasks"
        emptyText="You have completed all tasks"
        count={unreadMsg.event}
        list={noticeData.event}
        showViewMore
      />
    </NoticeIcon>
  );
};

export default NoticeIconView;
