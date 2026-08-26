import request from "@/utils/request.js";

const elderApi = {
    list(elderQuery){
        return request.get("/elders", {params: elderQuery})
    },
    add(elder){
        return request.post("/elders", elder)
    },
    deleteById(id){
        return request.delete(`/elders/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/elders",{data: ids} )
    },
    selectById(id){
        return request.get(`/elders/${id}`)
    },
    update(id, elder){
        return request.put(`/elders/${id}`, elder)
    },
    selectAssignedTag(elderId){
        return request.get(`/elders/selectAssignedTag/${elderId}`)
    }
}

export default elderApi