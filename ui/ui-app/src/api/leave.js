import request from "@/utils/request.js";

const leaveApi = {
    //当前登录老人的请假记录
    list() {
        return request.get("/elderleaves")
    },
    //提交请假申请
    add(leave) {
        return request.post("/elderleaves", leave)
    }
}

export default leaveApi
