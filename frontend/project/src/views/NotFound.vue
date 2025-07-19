<template>
  <div
    style="
      min-height: calc(100vh - 64px);
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
    "
  >
    <div
      style="
        text-align: center;
        background: rgba(255,255,255,0.15);
        backdrop-filter: blur(18px);
        -webkit-backdrop-filter: blur(18px);
        border-radius: 24px;
        box-shadow: 0 8px 32px rgba(0,0,0,0.12), inset 0 1px 0 rgba(255,255,255,0.18);
        border: 1px solid rgba(255,255,255,0.22);
        padding: 3.5rem 2.5rem 2.5rem 2.5rem;
        max-width: 420px;
        margin: 0 1rem;
        animation: fadeInUp404 0.8s cubic-bezier(.4,0,.2,1);
      "
    >
      <div style="margin-bottom: 1.5rem;">
        <img
          src="https://cdn.jsdelivr.net/gh/edent/SuperTinyIcons/images/svg/ghost.svg"
          alt="404"
          style="width: 90px; height: 90px; opacity: 0.8;"
        />
      </div>
      <h1
        style="
          font-size: 4.5rem;
          font-weight: 900;
          color: #5a6fd8;
          margin-bottom: 0.5rem;
          letter-spacing: 0.1em;
          text-shadow: 0 4px 12px rgba(90,111,216,0.18);
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        "
      >404</h1>
      <p
        style="
          font-size: 1.5rem;
          color: #333;
          font-weight: 600;
          margin-bottom: 1.2rem;
          text-shadow: 0 2px 6px rgba(0,0,0,0.08);
        "
      >页面未找到</p>
      <p
        style="
          color: #666;
          font-size: 1rem;
          margin-bottom: 2.2rem;
        "
      >抱歉，您访问的页面不存在或已被删除。</p>
      <router-link
        to="/"
        style="
          display: inline-block;
          padding: 0.85rem 2.2rem;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
          border-radius: 999px;
          font-weight: 700;
          font-size: 1.1rem;
          letter-spacing: 0.05em;
          box-shadow: 0 4px 16px rgba(102,126,234,0.18);
          border: none;
          text-decoration: none;
          transition: background 0.2s, transform 0.2s;
        "
        @mouseover="hover = true"
        @mouseleave="hover = false"
        :style="hover ? 'background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%); transform: translateY(-2px) scale(1.04);' : ''"
      >
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const hover = ref(false)
</script>

<style scoped>
@keyframes fadeInUp404 {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
