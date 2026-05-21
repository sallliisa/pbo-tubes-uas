import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const clients: ModelConfig = withModelDefaults({
  name: 'clients',
  title: 'Clients',
  fields: ['client_id', 'name', 'industry', 'contact_name', 'contact_email', 'contact_phone'],
  fieldsAlias: {
    client_id: 'Client ID',
    name: 'Name',
    industry: 'Industry',
    contact_name: 'Contact Name',
    contact_email: 'Contact Email',
    contact_phone: 'Contact Phone',
  },
  view: {
    list: {
      uid: 'client_id',
    },
  },
  transaction: {
    inputConfig: {
      client_id: { type: 'number', props: { required: true } },
      name: { type: 'text', props: { required: true } },
      industry: { type: 'text', props: { required: true } },
      contact_name: { type: 'text', props: { required: true } },
      contact_email: { type: 'text', props: { required: true, validation: z.string().email() } },
      contact_phone: { type: 'text', props: { required: true } },
    },
  },
})

export default clients