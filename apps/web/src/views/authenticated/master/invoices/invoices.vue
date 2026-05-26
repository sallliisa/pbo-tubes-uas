<script setup lang="ts">
import { toast } from 'vue-sonner'
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue'
import Button from '@southneuhof/is-vue-framework/components/base/Button.vue'
import invoicesModel from '@client/data-model/models/invoices.model'
import services from '@/utils/services'
import { keyManager } from '@southneuhof/is-vue-framework/adapters/state'

function refreshTables() {
  keyManager().triggerChange('invoices_table')
  keyManager().triggerChange('projects_table')
}

async function patchInvoice(data: Record<string, any>, patch: Record<string, any>, message: string) {
  const payload = {
    invoice_id: data.invoice_id,
    project_id: data.project_id,
    title: data.title,
    invoice_date: data.invoice_date,
    status: data.status,
    amount: data.amount,
    notes: data.notes,
    signed: data.signed,
    signed_by: data.signed_by,
    signed_at: data.signed_at,
    ...patch,
  }
  await services.put('invoices/update', payload)
  toast.success(message)
  refreshTables()
}

function todayISO() {
  return new Date().toISOString().slice(0, 10)
}

function markGenerated(data: Record<string, any>) {
  return patchInvoice(data, { status: 'Generated' }, 'Invoice marked as generated')
}

function signInvoice(data: Record<string, any>) {
  return patchInvoice(data, { signed: true, signed_by: 'Finance Lead', signed_at: todayISO() }, 'Invoice signed')
}

function markSent(data: Record<string, any>) {
  return patchInvoice(data, { status: 'Sent' }, 'Invoice marked as sent')
}

function markPaid(data: Record<string, any>) {
  return patchInvoice(data, { status: 'Paid' }, 'Invoice marked as paid and project billed total refreshed')
}

function cancelInvoice(data: Record<string, any>) {
  return patchInvoice(data, { status: 'Cancelled' }, 'Invoice cancelled')
}
</script>

<template>
  <CRUDComposite :config="invoicesModel">
    <template #list-rowAdditionalActions="{ data }">
      <div class="flex items-center gap-2">
        <Button v-if="data.status === 'Draft'" size="sm" variant="tonal" color="info" @click="() => markGenerated(data)">Generate</Button>
        <Button v-if="data.status === 'Generated' && !data.signed" size="sm" variant="tonal" color="success" @click="() => signInvoice(data)">Sign</Button>
        <Button v-if="data.status === 'Generated' && data.signed" size="sm" variant="tonal" color="warning" @click="() => markSent(data)">Mark Sent</Button>
        <Button v-if="data.status === 'Sent'" size="sm" variant="tonal" color="success" @click="() => markPaid(data)">Mark Paid</Button>
        <Button v-if="data.status !== 'Paid' && data.status !== 'Cancelled'" size="sm" variant="text" color="error" @click="() => cancelInvoice(data)">Cancel</Button>
      </div>
    </template>
  </CRUDComposite>
</template>
