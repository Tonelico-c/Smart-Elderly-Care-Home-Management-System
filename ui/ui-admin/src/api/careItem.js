import request from "@/utils/request.js";


const careItemApi = {
    list(careItemQuery) {
        return request.get("/care-items", {params: careItemQuery});
    },
    deleteById(id){
        return request.delete(`/care-items/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/care-items",{data: ids} )
    },
    add(careItem){
        return request.post("/care-items", careItem)
    },
    update(id,careItem){
        return request.put(`/care-items/${id}`, careItem)
    },
    selectById(id){
        return request.get(`/care-items/${id}`)
    }
}

export default careItemApi
