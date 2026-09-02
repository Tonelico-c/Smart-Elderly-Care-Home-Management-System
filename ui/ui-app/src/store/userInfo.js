import {defineStore} from 'pinia'

export const useUserInfoStore = defineStore('userInfo', {
    state() {
        return {
            user: {}
        }
    },
    actions: {
        setUserInfo(user) {
            this.user = user
        },
        removeUserInfo() {
            this.user = {}
        }
    },
    persist: {
        enabled: true
    }
})
