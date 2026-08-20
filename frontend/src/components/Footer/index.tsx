import { DefaultFooter } from '@ant-design/pro-layout';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();
  return <DefaultFooter copyright={`${currentYear} Co-op Application Tracker`} links={[]} />;
};

export default Footer;
