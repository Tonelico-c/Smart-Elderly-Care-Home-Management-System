import request from "@/utils/request.js";


const examItemApi = {
    list(examItemQuery) {
        return request.get("/exam-items", {params: examItemQuery});
    },
    deleteById(id){
        return request.delete(`/exam-items/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/exam-items",{data: ids} )
    },
    add(examItem){
        return request.post("/exam-items", examItem)
    },
    update(id,examItem){
        return request.put(`/exam-items/${id}`, examItem)
    },
    selectById(id){
        return request.get(`/exam-items/${id}`)
    }
}

export default examItemApi
