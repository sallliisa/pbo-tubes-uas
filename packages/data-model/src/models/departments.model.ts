import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const departments: ModelConfig = withModelDefaults({
  name: 'departments',
  title: 'Departments',
  fields: ['department_id', 'name'],
  fieldsAlias: {
    department_id: 'Department ID',
    name: 'Department Name',
  },
  view: {
    list: {
      uid: 'department_id',
    },
  },
  transaction: {
    fields: ['name'],
    inputConfig: {
      name: { type: 'text', props: { required: true, validation: z.string().min(1) } },
    },
  },
})

export default departments
