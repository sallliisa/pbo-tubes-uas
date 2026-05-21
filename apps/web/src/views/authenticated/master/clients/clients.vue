<script setup lang="ts">
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue';
import clientsModel from '@client/data-model/models/clients.model'

</script>

<template>
  <CRUDComposite :config="clientsModel"/>
</template>