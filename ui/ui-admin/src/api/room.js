import request from "@/utils/request.js";


const roomApi = {
    list(roomQuery) {
        return request.get("/rooms", { params: roomQuery });
    },
    deleteById(id) {
        return request.delete(`/rooms/${id}`)
    },
    deleteBatch(ids) {
        return request.delete("/rooms", { data: ids })
    },
    add(room) {
        return request.post("/rooms", room)
    },
    update(id, room) {
        return request.put(`/rooms/${id}`, room)
    },
    selectById(id) {
        return request.get(`/rooms/${id}`)
    },
    //按楼栋查房间列表（含每个房间入住人数）
    listByBuildingId(id) {
        return request.get(`/rooms/building/${id}`)
    }
}

export default roomApi
