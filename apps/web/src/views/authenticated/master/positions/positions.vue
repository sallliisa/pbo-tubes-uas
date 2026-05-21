<script setup lang="ts">
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue';
import positionsModel from '@client/data-model/models/positions.model'

</script>

<template>
  <CRUDComposite :config="positionsModel"/>
</template>