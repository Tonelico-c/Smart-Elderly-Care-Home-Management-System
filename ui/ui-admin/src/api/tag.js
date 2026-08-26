import request from "@/utils/request.js";


const tagApi = {
    list(tagQuery) {
        return request.get("/tags", {params: tagQuery});
    },
    deleteById(id){
        return request.delete(`/tags/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/tags",{data: ids} )
    },
    add(tag){
        return request.post("/tags", tag)
    },
    update(id,tag){
        return request.put(`/tags/${id}`, tag)
    },
    selectById(id){
        return request.get(`/tags/${id}`)
    }
}

export default tagApi
