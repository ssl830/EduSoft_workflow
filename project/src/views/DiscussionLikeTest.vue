<template>
  <q-page class="discussion-like-test-page">
    <div class="q-pa-md">
      <h1>讨论区点赞功能测试</h1>
      <DiscussionLikeDemo />
    </div>
  </q-page>
</template>

<script setup lang="ts">
import DiscussionLikeDemo from '../components/DiscussionLikeDemo.vue'
</script>

<style scoped>
.discussion-like-test-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

h1 {
  text-align: center;
  color: #1976d2;
  margin-bottom: 30px;
}
</style>
