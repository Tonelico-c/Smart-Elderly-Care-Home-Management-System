import request from "@/utils/request.js";


const bedApi = {
    listByRoom(roomId) {
        return request.get(`/beds/room/${roomId}`)
    },
    deleteById(id){
        return request.delete(`/beds/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/beds",{data: ids} )
    },
    add(bed){
        return request.post("/beds", bed)
    },
    update(id,bed){
        return request.put(`/beds/${id}`, bed)
    },
    selectById(id){
        return request.get(`/beds/${id}`)
    }
}

export default bedApi
