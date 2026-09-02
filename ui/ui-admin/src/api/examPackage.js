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
    },
    //查询套餐已分配的体检项目id列表
    selectAssignedItem(packageId){
        return request.get(`/exam-package-item/selectAssignedItem/${packageId}`)
    },
    //给套餐分配体检项目
    //examItemIds是数组,序列化成"1,2,3"再传,后端Spring会自动转成Long[]
    assignItem(packageId, examItemIds){
        return request.post("/exam-package-item/assignItem", null, {params: {packageId, examItemIds: examItemIds.join(',')}})
    }
}

export default examPackageApi
