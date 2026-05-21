import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const positions: ModelConfig = withModelDefaults({
  name: 'positions',
  title: 'Positions',
  fields: ['position_id', 'title', 'level', 'min_salary', 'max_salary', 'description'],
  fieldsAlias: {
    position_id: 'Position ID',
    title: 'Title',
    level: 'Level',
    min_salary: 'Min Salary',
    max_salary: 'Max Salary',
    description: 'Description',
  },
  view: {
    list: {
      uid: 'position_id',
    },
  },
  transaction: {
    inputConfig: {
      position_id: { type: 'number', props: { required: true } },
      title: { type: 'text', props: { required: true } },
      level: { type: 'text', props: { required: true } },
      min_salary: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      max_salary: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      description: { type: 'textarea', props: { required: true } },
    },
  },
})

export default positions