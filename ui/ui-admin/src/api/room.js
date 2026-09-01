import request from "@/utils/request.js";


const roomApi = {
    list(roomQuery) {
        return request.get("/rooms", {params: roomQuery});
    },
    deleteById(id){
        return request.delete(`/rooms/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/rooms",{data: ids} )
    },
    add(room){
        return request.post("/rooms", room)
    },
    update(id,room){
        return request.put(`/rooms/${id}`, room)
    },
    selectById(id){
        return request.get(`/rooms/${id}`)
    }
}

export default roomApi
