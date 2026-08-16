export default [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        path: '/user', routes: [
          {name: 'Sign in', path: '/user/login', component: './user/Login'},
          {name: 'Create account', path: '/user/register', component: './user/Register'}
        ]
      },
      {component: './404'},
    ],
  },
  {path: '/welcome', name: 'Dashboard', icon: 'dashboard', component: './Welcome'},
  {path: '/applications', name: 'Applications', icon: 'profile', component: './Applications'},
  {
    path: '/admin',
    name: 'Administration',
    icon: 'crown',
    access: 'canAdmin',
    component: './Admin',
    routes: [
      {path: '/admin/user-manage', name: 'User Management', icon: 'team', component: './Admin/UserManage'},
      {component: './404'},
    ],
  },
  {path: '/', redirect: '/welcome'},
  {component: './404'},
];
