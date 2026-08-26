// 创建一个路由器，并暴露出去
// 第一步：引入createRouter
import {createRouter, createWebHistory} from 'vue-router'
import {useTokenStore} from '@/store/token.js'
// 引入一个一个可能要呈现组件
import Index from '@/views/Index.vue'
import Login from '@/views/Login.vue'
import User from '@/views/User.vue'
import Elder from '@/views/Elder.vue'
import UserInfo from "@/views/UserInfo.vue";
import ResetPassword from "@/views/ResetPassword.vue";


//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            component: Login
        },
        {
            path: '/', component: Index, children: [
                {path: '/user', component: User},
                {path: '/elder', component: Elder},
                {path: '/user/info', component: UserInfo},
                {path:'/user/resetPassword', component: ResetPassword}
            ]
        }
    ]
})
//路由守卫
//全局前置守卫

let whiteList = ['/login']; // 白名单
router.beforeEach((to, from, next) => {
    const tokenStore = useTokenStore()
    const token = tokenStore.token;
    if (!whiteList.includes(to.path) && !token) {
        next('/login')
    } else {
        next()
    }
})

// 暴露出去router
export default router