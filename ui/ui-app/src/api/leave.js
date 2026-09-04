import request from "@/utils/request.js";

const leaveApi = {
    //当前登录老人的请假记录
    list() {
        return request.get("/elderleaves")
    }
}

export default leaveApi
