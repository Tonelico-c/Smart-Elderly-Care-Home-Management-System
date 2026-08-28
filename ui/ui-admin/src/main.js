import { createApp } from 'vue'
import App from './App.vue'

import router from './router'
import {createPinia} from 'pinia'
const pinia = createPinia()
import ElementPlus from 'element-plus' //导入element-plus
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css' //导入element-plus样式
//导入element-plus的全部图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

//引入持久化插件
import piniaPluginPersistedstate from "pinia-plugin-persistedstate"
//使用持久化插件
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)
//批量全局注册所有图标组件
//注册后,数据库里存图标名(如 "User"、"Setting"),模板里 <component :is="row.icon"/> 就能解析到
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component)
}
app.use(router).use(pinia).use(ElementPlus, {locale: zhCn }).mount('#app')