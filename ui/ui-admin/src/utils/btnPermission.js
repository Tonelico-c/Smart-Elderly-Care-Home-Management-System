import {UserInfoStore} from '@/store/userInfo.js'

//判断当前用户是否具有这个按钮权限 user:add
export default function hasBtnPermission(permission) {
    const userInfoStore = UserInfoStore()
    //获取当前用户所有的按钮权限
    const btns = userInfoStore.btn;
    //['user:add','user:deleteById']
    return btns.indexOf(permission) !== -1
}