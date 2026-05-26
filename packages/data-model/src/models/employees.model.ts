import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const employees: ModelConfig = withModelDefaults({
  name: 'employees',
  title: 'Employees',
  fields: [
    'employee_id',
    'first_name',
    'last_name',
    'email',
    'hire_date',
    'salary',
    'employee_type',
    'position_id',
    'position_title',
    'position_level',
    'position_min_salary',
    'position_max_salary',
    'position_description',
    'department_id',
    'department_name',
    'assignment_count',
    'timesheet_count',
    'benefit_plan',
    'annual_leave_quota',
    'contract_start_date',
    'contract_end_date',
  ],
  fieldsAlias: {
    employee_id: 'Employee ID',
    first_name: 'First Name',
    last_name: 'Last Name',
    email: 'Email',
    hire_date: 'Hire Date',
    salary: 'Salary',
    employee_type: 'Employee Type',
    position_id: 'Position',
    position_title: 'Position Title',
    position_level: 'Level',
    position_min_salary: 'Minimum Salary',
    position_max_salary: 'Maximum Salary',
    position_description: 'Position Description',
    department_id: 'Department',
    department_name: 'Department',
    assignment_count: 'Assignment Count',
    timesheet_count: 'Timesheet Count',
    benefit_plan: 'Benefit Plan',
    annual_leave_quota: 'Annual Leave Quota',
    contract_start_date: 'Contract Start Date',
    contract_end_date: 'Contract End Date',
  },
  view: {
    list: {
      uid: 'employee_id',
    },
  },
  transaction: {
    fields: [
      'first_name',
      'last_name',
      'email',
      'hire_date',
      'salary',
      'employee_type',
      'position_id',
      'department_id',
      'benefit_plan',
      'annual_leave_quota',
      'contract_start_date',
      'contract_end_date',
    ],
    inputConfig: {
      first_name: { type: 'text', props: { required: true } },
      last_name: { type: 'text', props: { required: true } },
      email: { type: 'text', props: { required: true, validation: z.string().email() } },
      hire_date: { type: 'date', props: { required: true } },
      salary: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      position_id: {
        type: 'lookup',
        props: {
          required: true,
          getAPI: 'positions',
          fields: ['title', 'level', 'min_salary', 'max_salary'],
          fieldsAlias: {
            title: 'Position Title',
            level: 'Level',
            min_salary: 'Minimum Salary',
            max_salary: 'Maximum Salary',
          },
          pick: 'position_id',
          placeholder: 'Select position',
        },
      },
      department_id: {
        type: 'lookup',
        props: {
          required: true,
          getAPI: 'departments',
          fields: ['name'],
          fieldsAlias: {
            name: 'Department Name',
          },
          pick: 'department_id',
          placeholder: 'Select department',
        },
      },
      employee_type: {
        type: 'radio',
        props: {
          required: true,
          data: [
            { id: 'Permanent Employee', name: 'Permanent Employee' },
            { id: 'Contract Employee', name: 'Contract Employee' },
          ],
        },
      },
      benefit_plan: {
        type: 'text',
        dependency: {
          fields: ['employee_type'],
          visibility: {
            default: false,
            validator: ({ employee_type }) => employee_type === 'Permanent Employee',
          },
        },
      },
      annual_leave_quota: {
        type: 'number',
        dependency: {
          fields: ['employee_type'],
          visibility: {
            default: false,
            validator: ({ employee_type }) => employee_type === 'Permanent Employee',
          },
        },
      },
      contract_start_date: {
        type: 'date',
        dependency: {
          fields: ['employee_type'],
          visibility: {
            default: false,
            validator: ({ employee_type }) => employee_type === 'Contract Employee',
          },
        },
      },
      contract_end_date: {
        type: 'date',
        dependency: {
          fields: ['employee_type'],
          visibility: {
            default: false,
            validator: ({ employee_type }) => employee_type === 'Contract Employee',
          },
        },
      },
    },
  },
})



export default employees
