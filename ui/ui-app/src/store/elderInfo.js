import {defineStore} from 'pinia'

export const elderElderInfoStore = defineStore('elderInfo', {
    state() {
        return {
            elder: {}
        }
    },
    actions: {
        setUserInfo(elder) {
            this.elder = elder
        },
        removeUserInfo() {
            this.elder = {}
        }
    },
    persist: {
        enabled: true
    }
})
