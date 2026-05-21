<script setup lang="ts">
import CRUDComposite from '@southneuhof/is-vue-framework/components/composites/CRUDComposite.vue';
import projectsModel from '@client/data-model/models/projects.model'

</script>

<template>
  <CRUDComposite :config="projectsModel"/>
</template>