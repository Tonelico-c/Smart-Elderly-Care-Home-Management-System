import {defineStore} from 'pinia'

export const UserInfoStore = defineStore('userInfo', {
    //存储数据地方
    state() {
        return {
            user: {},
            btn:[]
        }
    },
    //actions里面放的是一个一个方法
    actions: {
        setUserInfo(user) {
            this.user = user
        },
        removeUserInfo() {
            this.user = {}
        },
        setBtn(btn) {
            this.btn = btn
        },
        removeBtn() {
            this.btn = []
        },
    },
    persist: {
        enabled: true, // 开启缓存  默认会存储在本地localstorage
    }
})
