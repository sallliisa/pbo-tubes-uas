import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const invoices: ModelConfig = withModelDefaults({
  name: 'invoices',
  title: 'Invoices',
  fields: [
    'invoice_id',
    'project_id',
    'title',
    'invoice_date',
    'status',
    'amount',
    'notes',
    'signed',
    'signed_by',
    'signed_at',
  ],
  fieldsAlias: {
    invoice_id: 'Invoice ID',
    project_id: 'Project',
    title: 'Invoice Title',
    invoice_date: 'Invoice Date',
    status: 'Status',
    amount: 'Amount',
    notes: 'Notes',
    signed: 'Signed',
    signed_by: 'Signed By',
    signed_at: 'Signed At',
  },
  view: {
    list: {
      uid: 'invoice_id',
      fieldsType: {
        status: {
          type: 'chip',
          props: {
            options: {
              Draft: { color: 'neutral', label: 'Draft' },
              Generated: { color: 'info', label: 'Generated' },
              Sent: { color: 'warning', label: 'Sent' },
              Paid: { color: 'success', label: 'Paid' },
              Cancelled: { color: 'error', label: 'Cancelled' },
            },
          },
        },
      },
    },
  },
  transaction: {
    fields: ['project_id', 'title', 'invoice_date', 'status', 'amount', 'notes', 'signed', 'signed_by', 'signed_at'],
    inputConfig: {
      project_id: {
        type: 'lookup',
        props: {
          required: true,
          getAPI: 'projects',
          fields: ['name', 'status', 'client_id'],
          fieldsAlias: {
            name: 'Project Name',
            status: 'Status',
            client_id: 'Client',
          },
          pick: 'project_id',
          placeholder: 'Select project',
        },
      },
      title: { type: 'text', props: { required: true } },
      invoice_date: { type: 'date', props: { required: true } },
      status: {
        type: 'select',
        props: {
          required: true,
          clearable: false,
          data: [
            { id: 'Draft', name: 'Draft' },
            { id: 'Generated', name: 'Generated' },
            { id: 'Sent', name: 'Sent' },
            { id: 'Paid', name: 'Paid' },
            { id: 'Cancelled', name: 'Cancelled' },
          ],
        },
      },
      amount: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      notes: { type: 'textarea', props: { required: true } },
      signed: { type: 'radio', props: { required: true, data: [{ id: true, name: 'Signed' }, { id: false, name: 'Unsigned' }] } },
      signed_by: {
        type: 'text',
        dependency: {
          fields: ['signed'],
          visibility: {
            default: false,
            validator: ({ signed }) => signed === true,
          },
        },
      },
      signed_at: {
        type: 'date',
        dependency: {
          fields: ['signed'],
          visibility: {
            default: false,
            validator: ({ signed }) => signed === true,
          },
        },
      },
    },
  },
})

export default invoices
