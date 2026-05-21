import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const departments: ModelConfig = withModelDefaults({
  name: 'departments',
  title: 'Departments',
  fields: ['department_id', 'name'],
  fieldsAlias: {
    department_id: 'Department ID',
    name: 'Name',
  },
  view: {
    list: {
      uid: 'department_id',
    },
  },
  transaction: {
    inputConfig: {
      department_id: { type: 'number', props: { required: true } },
      name: { type: 'text', props: { required: true, validation: z.string().min(1) } },
    },
  },
})

export default departments