import request from "@/utils/request.js";


const examPackageApi = {
    list(examPackageQuery) {
        return request.get("/exam-packages", {params: examPackageQuery});
    },
    deleteById(id){
        return request.delete(`/exam-packages/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/exam-packages",{data: ids} )
    },
    add(examPackage){
        return request.post("/exam-packages", examPackage)
    },
    update(id,examPackage){
        return request.put(`/exam-packages/${id}`, examPackage)
    },
    selectById(id){
        return request.get(`/exam-packages/${id}`)
    }
}

export default examPackageApi
