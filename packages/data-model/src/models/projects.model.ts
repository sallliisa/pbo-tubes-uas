import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const projects: ModelConfig = withModelDefaults({
  name: 'projects',
  title: 'Projects',
  fields: ['project_id', 'client_id', 'name', 'description', 'start_date', 'end_date', 'status', 'budget', 'total_billed_amount'],
  fieldsAlias: {
    project_id: 'Project ID',
    client_id: 'Client',
    name: 'Project Name',
    description: 'Description',
    start_date: 'Start Date',
    end_date: 'End Date',
    status: 'Status',
    budget: 'Budget',
    total_billed_amount: 'Total Billed Amount',
  },
  view: {
    list: {
      uid: 'project_id',
    },
  },
  transaction: {
    fields: ['client_id', 'name', 'description', 'start_date', 'end_date', 'status', 'budget'],
    inputConfig: {
      client_id: {
        type: 'lookup',
        props: {
          required: true,
          getAPI: 'clients',
          fields: ['name', 'industry', 'contact_name'],
          fieldsAlias: {
            name: 'Client Name',
            industry: 'Industry',
            contact_name: 'Contact Name',
          },
          pick: 'client_id',
          placeholder: 'Select client',
        },
      },
      name: { type: 'text', props: { required: true } },
      description: { type: 'textarea', props: { required: true } },
      start_date: { type: 'date', props: { required: true } },
      end_date: { type: 'date', props: { required: true } },
      status: {
        type: 'select',
        props: {
          required: true,
          clearable: false,
          data: [
            { id: 'Planned', name: 'Planned' },
            { id: 'Active', name: 'Active' },
            { id: 'OnHold', name: 'On Hold' },
            { id: 'Completed', name: 'Completed' },
            { id: 'Cancelled', name: 'Cancelled' },
          ],
        },
      },
      budget: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
    },
  },
})

export default projects
