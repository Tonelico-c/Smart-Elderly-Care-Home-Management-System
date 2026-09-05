import request from "@/utils/request.js";

const leaveApi = {
    //当前登录老人的请假记录
    list() {
        return request.get("/elderleaves")
    },
    //提交请假申请
    add(leave) {
        return request.post("/elderleaves", leave)
    },
    //销假（不传实际返回时间时后端默认当前时间）
    checkout(id, actualReturnTime) {
        return request.post(`/elderleaves/${id}/checkout`, null, { params: { actualReturnTime } })
    },
    //取消待审批的请假申请
    cancel(id) {
        return request.delete(`/elderleaves/${id}/cancel`)
    }
}

export default leaveApi
