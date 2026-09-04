import request from "@/utils/request.js";


const elderLeaveApi = {
    list(elderLeaveQuery) {
        return request.get("/elder-leaves", { params: elderLeaveQuery });
    },
    deleteById(id) {
        return request.delete(`/elder-leaves/${id}`)
    },
    deleteBatch(ids) {
        return request.delete("/elder-leaves", { data: ids })
    },
    add(elderLeave) {
        return request.post("/elder-leaves", elderLeave)
    },
    update(id, elderLeave) {
        return request.put(`/elder-leaves/${id}`, elderLeave)
    },
    selectById(id) {
        return request.get(`/elder-leaves/${id}`)
    },
    //审批通过(审批人id由后端从token解析)
    approve(id) {
        return request.post(`/elder-leaves/${id}/approve`)
    },
    //审批驳回,必须传驳回理由
    reject(id, rejectReason) {
        return request.post(`/elder-leaves/${id}/reject`, null, { params: { rejectReason } })
    },
    //销假,实际返回时间不传时后端默认取当前时间
    checkout(id, actualReturnTime) {
        return request.post(`/elder-leaves/${id}/checkout`, null, { params: { actualReturnTime } })
    }
}

export default elderLeaveApi
