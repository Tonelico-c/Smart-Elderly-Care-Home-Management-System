import request from "@/utils/request.js";

const elderApi = {
    login(elder) {
        return request.post("/elders/login", elder)
    },
    userInfo() {
        return request.get("/elders/userInfo")
    }
}

export default elderApi
