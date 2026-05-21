<script setup lang="ts">
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue';
import employees from '@client/data-model/models/employees.model'

</script>

<template>
  <CRUDComposite :config="employees"/>
</template>