// vite.config.ts
import { defineConfig } from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/vite/dist/node/index.js";
import vue from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import { transformAssetUrls } from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/@quasar/vite-plugin/src/index.js";
import AutoImport from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/unplugin-auto-import/dist/vite.js";
import Components from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/unplugin-vue-components/dist/vite.js";
import { ElementPlusResolver } from "file:///D:/code/vue/edusoft2/EduSoft/frontend/project/node_modules/unplugin-vue-components/dist/resolvers.js";
import { resolve } from "path";
var __vite_injected_original_dirname = "D:\\code\\vue\\edusoft2\\EduSoft\\frontend\\project";
var vite_config_default = defineConfig({
  base: "./",
  // 确保资源路径正确
  plugins: [
    vue({
      template: { transformAssetUrls }
    }),
    // quasar({
    //   sassVariables: 'src/quasar-variables.sass'
    // }),
    AutoImport({
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      "@": resolve(__vite_injected_original_dirname, "src"),
      "@popperjs/core/lib/modifiers/offset.js": resolve("./node_modules/@popperjs/core/dist/esm/modifiers/offset.js")
    }
  },
  server: {
    port: 3e3,
    // 修改为 3000 端口
    fs: {
      allow: [".."]
      // 允许访问上级目录
    },
    hmr: {
      overlay: false
      // 禁用热更新错误覆盖
    }
  },
  optimizeDeps: {
    include: ["vue3-quill", "quill"]
  },
  css: {
    preprocessorOptions: {
      sass: {
        // 移除了 require('sass')
        additionalData: ""
      }
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJEOlxcXFxjb2RlXFxcXHZ1ZVxcXFxlZHVzb2Z0MlxcXFxFZHVTb2Z0XFxcXGZyb250ZW5kXFxcXHByb2plY3RcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZmlsZW5hbWUgPSBcIkQ6XFxcXGNvZGVcXFxcdnVlXFxcXGVkdXNvZnQyXFxcXEVkdVNvZnRcXFxcZnJvbnRlbmRcXFxccHJvamVjdFxcXFx2aXRlLmNvbmZpZy50c1wiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9pbXBvcnRfbWV0YV91cmwgPSBcImZpbGU6Ly8vRDovY29kZS92dWUvZWR1c29mdDIvRWR1U29mdC9mcm9udGVuZC9wcm9qZWN0L3ZpdGUuY29uZmlnLnRzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSdcclxuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXHJcbmltcG9ydCB7IHF1YXNhciwgdHJhbnNmb3JtQXNzZXRVcmxzIH0gZnJvbSAnQHF1YXNhci92aXRlLXBsdWdpbidcclxuaW1wb3J0IEF1dG9JbXBvcnQgZnJvbSAndW5wbHVnaW4tYXV0by1pbXBvcnQvdml0ZSdcclxuaW1wb3J0IENvbXBvbmVudHMgZnJvbSAndW5wbHVnaW4tdnVlLWNvbXBvbmVudHMvdml0ZSdcclxuaW1wb3J0IHsgRWxlbWVudFBsdXNSZXNvbHZlciB9IGZyb20gJ3VucGx1Z2luLXZ1ZS1jb21wb25lbnRzL3Jlc29sdmVycydcclxuaW1wb3J0IHsgcmVzb2x2ZSB9IGZyb20gJ3BhdGgnXHJcblxyXG4vLyBodHRwczovL3ZpdGVqcy5kZXYvY29uZmlnL1xyXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoe1xyXG4gIGJhc2U6ICcuLycsIC8vIFx1Nzg2RVx1NEZERFx1OEQ0NFx1NkU5MFx1OERFRlx1NUY4NFx1NkI2M1x1Nzg2RVxyXG4gIHBsdWdpbnM6IFtcclxuICAgIHZ1ZSh7XHJcbiAgICAgIHRlbXBsYXRlOiB7IHRyYW5zZm9ybUFzc2V0VXJscyB9XHJcbiAgICB9KSxcclxuICAgIC8vIHF1YXNhcih7XHJcbiAgICAvLyAgIHNhc3NWYXJpYWJsZXM6ICdzcmMvcXVhc2FyLXZhcmlhYmxlcy5zYXNzJ1xyXG4gICAgLy8gfSksXHJcbiAgICBBdXRvSW1wb3J0KHtcclxuICAgICAgcmVzb2x2ZXJzOiBbRWxlbWVudFBsdXNSZXNvbHZlcigpXSxcclxuICAgIH0pLFxyXG4gICAgQ29tcG9uZW50cyh7XHJcbiAgICAgIHJlc29sdmVyczogW0VsZW1lbnRQbHVzUmVzb2x2ZXIoKV0sXHJcbiAgICB9KSwgIF0sXHJcbiAgcmVzb2x2ZToge1xyXG4gICAgYWxpYXM6IHtcclxuICAgICAgJ0AnOiByZXNvbHZlKF9fZGlybmFtZSwgJ3NyYycpLFxyXG4gICAgICAnQHBvcHBlcmpzL2NvcmUvbGliL21vZGlmaWVycy9vZmZzZXQuanMnOiByZXNvbHZlKCcuL25vZGVfbW9kdWxlcy9AcG9wcGVyanMvY29yZS9kaXN0L2VzbS9tb2RpZmllcnMvb2Zmc2V0LmpzJyksXHJcbiAgICB9XHJcbiAgfSxcclxuICBzZXJ2ZXI6IHtcclxuICAgIHBvcnQ6IDMwMDAsIC8vIFx1NEZFRVx1NjUzOVx1NEUzQSAzMDAwIFx1N0FFRlx1NTNFM1xyXG4gICAgZnM6IHtcclxuICAgICAgYWxsb3c6IFsnLi4nXSAvLyBcdTUxNDFcdThCQjhcdThCQkZcdTk1RUVcdTRFMEFcdTdFQTdcdTc2RUVcdTVGNTVcclxuICAgIH0sXHJcbiAgICBobXI6IHtcclxuICAgICAgb3ZlcmxheTogZmFsc2UgLy8gXHU3OTgxXHU3NTI4XHU3MEVEXHU2NkY0XHU2NUIwXHU5NTE5XHU4QkVGXHU4OTg2XHU3NkQ2XHJcbiAgICB9XHJcbiAgfSxcclxuICBvcHRpbWl6ZURlcHM6IHtcclxuICAgIGluY2x1ZGU6IFsndnVlMy1xdWlsbCcsJ3F1aWxsJ11cclxuICB9LFxyXG4gIGNzczoge1xyXG4gICAgcHJlcHJvY2Vzc29yT3B0aW9uczoge1xyXG4gICAgICBzYXNzOiB7XHJcbiAgICAgICAgLy8gXHU3OUZCXHU5NjY0XHU0RTg2IHJlcXVpcmUoJ3Nhc3MnKVxyXG4gICAgICAgIGFkZGl0aW9uYWxEYXRhOiAnJ1xyXG4gICAgICB9XHJcbiAgICB9XHJcbiAgfVxyXG59KVxyXG4iXSwKICAibWFwcGluZ3MiOiAiO0FBQXVVLFNBQVMsb0JBQW9CO0FBQ3BXLE9BQU8sU0FBUztBQUNoQixTQUFpQiwwQkFBMEI7QUFDM0MsT0FBTyxnQkFBZ0I7QUFDdkIsT0FBTyxnQkFBZ0I7QUFDdkIsU0FBUywyQkFBMkI7QUFDcEMsU0FBUyxlQUFlO0FBTnhCLElBQU0sbUNBQW1DO0FBU3pDLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQzFCLE1BQU07QUFBQTtBQUFBLEVBQ04sU0FBUztBQUFBLElBQ1AsSUFBSTtBQUFBLE1BQ0YsVUFBVSxFQUFFLG1CQUFtQjtBQUFBLElBQ2pDLENBQUM7QUFBQTtBQUFBO0FBQUE7QUFBQSxJQUlELFdBQVc7QUFBQSxNQUNULFdBQVcsQ0FBQyxvQkFBb0IsQ0FBQztBQUFBLElBQ25DLENBQUM7QUFBQSxJQUNELFdBQVc7QUFBQSxNQUNULFdBQVcsQ0FBQyxvQkFBb0IsQ0FBQztBQUFBLElBQ25DLENBQUM7QUFBQSxFQUFJO0FBQUEsRUFDUCxTQUFTO0FBQUEsSUFDUCxPQUFPO0FBQUEsTUFDTCxLQUFLLFFBQVEsa0NBQVcsS0FBSztBQUFBLE1BQzdCLDBDQUEwQyxRQUFRLDREQUE0RDtBQUFBLElBQ2hIO0FBQUEsRUFDRjtBQUFBLEVBQ0EsUUFBUTtBQUFBLElBQ04sTUFBTTtBQUFBO0FBQUEsSUFDTixJQUFJO0FBQUEsTUFDRixPQUFPLENBQUMsSUFBSTtBQUFBO0FBQUEsSUFDZDtBQUFBLElBQ0EsS0FBSztBQUFBLE1BQ0gsU0FBUztBQUFBO0FBQUEsSUFDWDtBQUFBLEVBQ0Y7QUFBQSxFQUNBLGNBQWM7QUFBQSxJQUNaLFNBQVMsQ0FBQyxjQUFhLE9BQU87QUFBQSxFQUNoQztBQUFBLEVBQ0EsS0FBSztBQUFBLElBQ0gscUJBQXFCO0FBQUEsTUFDbkIsTUFBTTtBQUFBO0FBQUEsUUFFSixnQkFBZ0I7QUFBQSxNQUNsQjtBQUFBLElBQ0Y7QUFBQSxFQUNGO0FBQ0YsQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
