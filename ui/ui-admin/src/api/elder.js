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
    },
    assignTag(elderId, tagIds){
        return request.post("/elders/assignTag", null, {params: {elderId, tagIds}})
    },
    exportExcel(){
        return request({
            url: `/elders/exportExcel`,
            method: 'get',
            //XMLHttpRequest 属性 responseType 是一个枚举字符串值，用于指定响应中包含的数据类型。
            //"blob": response 是一个包含二进制数据的 Blob 对象。
            responseType: 'blob'
        })
    }
}

export default elderApi