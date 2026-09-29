/**
 * request 网络请求工具
 * 更详细的 api 文档: https://github.com/umijs/umi-request
 */
import { extend } from 'umi-request';
import { history } from '@@/core/history';
import { stringify } from 'querystring';

/**
 * 配置request请求时的默认参数
 */
const request = extend({
  credentials: 'include', // 默认请求是否带上cookie
  timeout: 30_000,
  // requestType: 'form',
});

/**
 * 所有响应拦截器
 */
request.interceptors.response.use(async (response): Promise<any> => {
  const res = await response.clone().json();
  if (res.code === 0) {
    return res.data;
  }
  const currentPath = history.location.pathname;
  const publicPaths = ['/user/login', '/user/register'];
  if (res.code === 40100 && !publicPaths.includes(currentPath)) {
    history.replace({
      pathname: '/user/login',
      search: stringify({
        redirect: currentPath,
      }),
    });
  }
  const error = new Error(res.description || res.message || 'Request failed') as Error & {
    code: number;
  };
  error.name = 'ApiError';
  error.code = res.code;
  throw error;
});

export default request;
