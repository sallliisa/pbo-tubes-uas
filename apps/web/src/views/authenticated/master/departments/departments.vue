<script setup lang="ts">
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue';
import departmentsModel from '@client/data-model/models/departments.model'

</script>

<template>
  <CRUDComposite :config="departmentsModel"/>
</template>