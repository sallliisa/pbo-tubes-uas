// Uses name as permission if permission field is not defined
// permission is only read on route-level
const menu: Modules = [
  {
    name: 'master',
    title: 'Master',
    icon: 'database',
    description: 'Master data management',
    routes: [
      {
        name: 'employees',
        title: 'Employees',
        icon: 'user',
      },
      {
        name: 'departments',
        title: 'Departments',
        icon: 'team',
      },
      {
        name: 'positions',
        title: 'Positions',
        icon: 'building',
      },
      {
        name: 'clients',
        title: 'Clients',
        icon: 'group',
      },
      {
        name: 'projects',
        title: 'Projects',
        icon: 'dashboard',
      },
      {
        name: 'contracts',
        title: 'Contracts',
        icon: 'file-text',
      },
      {
        name: 'invoices',
        title: 'Invoices',
        icon: 'money-dollar-circle',
      },
    ],
  }
]
export default menu
