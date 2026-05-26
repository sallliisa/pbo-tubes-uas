import type { ModelConfig } from '@southneuhof/is-data-model'
import { withModelDefaults } from './_defaults'
import { z } from 'zod'

const contracts: ModelConfig = withModelDefaults({
  name: 'contracts',
  title: 'Contracts',
  fields: [
    'contract_id',
    'project_id',
    'title',
    'contract_date',
    'status',
    'contract_number',
    'start_date',
    'end_date',
    'value',
    'terms',
    'notes',
    'signed',
    'signed_by',
    'signed_at',
  ],
  fieldsAlias: {
    contract_id: 'Contract ID',
    project_id: 'Project',
    title: 'Contract Title',
    contract_date: 'Contract Date',
    status: 'Status',
    contract_number: 'Contract Number',
    start_date: 'Start Date',
    end_date: 'End Date',
    value: 'Value',
    terms: 'Terms',
    notes: 'Notes',
    signed: 'Signed',
    signed_by: 'Signed By',
    signed_at: 'Signed At',
  },
  view: {
    list: {
      uid: 'contract_id',
      fieldsType: {
        status: {
          type: 'chip',
          props: {
            options: {
              Draft: { color: 'neutral', label: 'Draft' },
              Active: { color: 'success', label: 'Active' },
              Terminated: { color: 'error', label: 'Terminated' },
              Renewed: { color: 'warning', label: 'Renewed' },
            },
          },
        },
      },
    },
  },
  transaction: {
    fields: [
      'project_id',
      'title',
      'contract_date',
      'status',
      'contract_number',
      'start_date',
      'end_date',
      'value',
      'terms',
      'notes',
      'signed',
      'signed_by',
      'signed_at',
    ],
    inputConfig: {
      project_id: {
        type: 'lookup',
        props: {
          required: true,
          getAPI: 'projects',
          fields: ['name', 'status', 'start_date'],
          fieldsAlias: {
            name: 'Project Name',
            status: 'Status',
            start_date: 'Start Date',
          },
          pick: 'project_id',
          placeholder: 'Select project',
        },
      },
      title: { type: 'text', props: { required: true } },
      contract_date: { type: 'date', props: { required: true } },
      status: {
        type: 'select',
        props: {
          required: true,
          clearable: false,
          data: [
            { id: 'Draft', name: 'Draft' },
            { id: 'Active', name: 'Active' },
            { id: 'Terminated', name: 'Terminated' },
            { id: 'Renewed', name: 'Renewed' },
          ],
        },
      },
      contract_number: { type: 'text', props: { required: true } },
      start_date: { type: 'date', props: { required: true } },
      end_date: { type: 'date', props: { required: true } },
      value: { type: 'number', props: { required: true, validation: z.number().nonnegative() } },
      terms: { type: 'textarea', props: { required: true } },
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

export default contracts
