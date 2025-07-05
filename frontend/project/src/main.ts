import { createApp } from 'vue'
import { Quasar, QBtn, QTooltip, Loading, Dialog, Notify } from 'quasar'
import { createPinia } from 'pinia'
import router from './router'
import './styles/index.scss'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import ProgressKnob from './components/littlecomponents/ProgressKnob.vue' 
import WriteBoard from './components/littlecomponents/WriteBoard.vue'
// 导入修改后的 Quasar CSS 以解决 math.div 问题
import 'quasar/src/css/index.sass'
// 导入图标集
import '@quasar/extras/material-icons/material-icons.css'
import 'font-awesome/css/font-awesome.min.css'
import Varlet from '@varlet/ui'
import '@varlet/ui/es/style'
import { StyleProvider, Themes } from '@varlet/ui'

// 从本地存储读取用户偏好或使用默认主题

const themeMap = {
  light: null, // 对应 Material Design 2 亮色
  dark: Themes.dark, // 对应 Material Design 2 暗色
  md3Light: Themes.md3Light,
  md3Dark: Themes.md3Dark
}

// 初始化主题
const savedTheme = (localStorage.getItem('theme') as 'light' | 'dark' | 'md3Light' | 'md3Dark') || 'md3Light'
StyleProvider(themeMap[savedTheme])

// 应用主题类到 HTML 元素
const applyThemeClass = (theme: string) => {
  document.documentElement.classList.remove('theme-dark', 'theme-md3-light', 'theme-md3-dark')
  
  switch(theme) {
    case 'dark':
      document.documentElement.classList.add('theme-dark')
      break
    case 'md3Light':
      document.documentElement.classList.add('theme-md3-light')
      break
    case 'md3Dark':
      document.documentElement.classList.add('theme-md3-dark')
      break
  }
}

applyThemeClass(savedTheme)
const app = createApp(App)
const pinia = createPinia()

// 使用各种插件
app.use(pinia)
    .use(router)
    .use(ElementPlus)
    .use(Quasar, {
      components: {
        QBtn,
        QTooltip
      },
      plugins: {
        Loading,
        Dialog,
        Notify
      }
    })
    .use(Varlet)
    .component('ProgressKnob', ProgressKnob)
    .component('WriteBoard', WriteBoard)

// 挂载应用
app.mount('#app')