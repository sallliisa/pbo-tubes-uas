<script setup lang="ts">
import { ref } from 'vue'
import { toast } from 'vue-sonner'
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue'
import DialogForm from '@southneuhof/is-vue-framework/components/composites/DialogForm.vue'
import Button from '@southneuhof/is-vue-framework/components/base/Button.vue'
import contractsModel from '@client/data-model/models/contracts.model'
import services from '@/utils/services'
import { keyManager } from '@southneuhof/is-vue-framework/adapters/state'

const signingTarget = ref<Record<string, any> | null>(null)

function refreshTables() {
  keyManager().triggerChange('contracts_table')
  keyManager().triggerChange('projects_table')
}

function sanitizeContractPayload(payload: Record<string, any>) {
  const { contract_value: _contractValue, ...rest } = payload
  return rest
}

async function submitSigning({ payload }: { payload: Record<string, any> }) {
  const signer = String(payload.signed_by || '').trim()
  if (!signingTarget.value || !signer) throw new Error('Signer is required')

  return services.put('contracts/update', {
    ...sanitizeContractPayload(signingTarget.value),
    signed: true,
    signed_by: signer,
    signed_at: payload.signed_at,
    status: 'Active',
  })
}

async function submitRenewal({ payload }: { payload: Record<string, any> }) {
  return services.put('contracts/update', {
    ...sanitizeContractPayload(payload),
    status: 'Renewed',
  })
}

function todayISO() {
  return new Date().toISOString().slice(0, 10)
}
</script>

<template>
  <CRUDComposite :config="contractsModel">
    <template #list-rowAdditionalActions="{ data }">
      <div v-if="(data.status === 'Draft') || (data.status === 'Active' || data.status === 'Renewed')" class="flex items-center gap-2">
        <DialogForm
          v-if="data.status === 'Draft'"
          :fields="['signed_by', 'signed_at']"
          :fieldsAlias="{ signed_by: 'Signer', signed_at: 'Signing Date' }"
          :inputConfig="{
            signed_by: { type: 'text', props: { required: true } },
            signed_at: { type: 'date', props: { required: true } },
          }"
          :getInitialData="async () => ({ signed_at: todayISO() })"
          :onSubmit="submitSigning"
          :onSuccess="() => {
            toast.success('Contract signed successfully')
            refreshTables()
          }"
          @open="() => (signingTarget = data)"
        >
          <template #trigger>
            <Button size="sm" variant="tonal" color="success">Sign</Button>
          </template>
          <template #title>Electronic Signing</template>
        </DialogForm>

        <DialogForm
          v-if="data.status === 'Active' || data.status === 'Renewed'"
          :fields="['project_id', 'title', 'contract_date', 'contract_number', 'start_date', 'end_date', 'value', 'terms', 'notes', 'signed', 'signed_by', 'signed_at']"
          :fieldsAlias="contractsModel.fieldsAlias"
          :inputConfig="contractsModel.transaction?.inputConfig"
          :getInitialData="
            async () => ({
              ...data,
              end_date: data.end_date,
              value: data.value,
              notes: data.notes,
            })
          "
          :onSubmit="submitRenewal"
          :onSuccess="() => {
            toast.success('Contract renewed/adjusted successfully')
            refreshTables()
          }"
        >
          <template #trigger>
            <Button size="sm" variant="tonal" color="warning">Renew/Adjust</Button>
          </template>
          <template #title>Contract Renewal & Adjustments</template>
        </DialogForm>
      </div>
    </template>
  </CRUDComposite>
</template>
