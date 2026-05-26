import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const positions: ModelConfig = withModelDefaults({
  name: 'positions',
  title: 'Positions',
  fields: ['position_id', 'title', 'level', 'min_salary', 'max_salary', 'description'],
  fieldsAlias: {
    position_id: 'Position ID',
    title: 'Position Title',
    level: 'Level',
    min_salary: 'Minimum Salary',
    max_salary: 'Maximum Salary',
    description: 'Description',
  },
  view: {
    list: {
      uid: 'position_id',
    },
  },
  transaction: {
    fields: ['title', 'level', 'min_salary', 'max_salary', 'description'],
    inputConfig: {
      title: { type: 'text', props: { required: true } },
      level: { type: 'text', props: { required: true } },
      min_salary: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      max_salary: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      description: { type: 'textarea', props: { required: true } },
    },
  },
})

export default positions
