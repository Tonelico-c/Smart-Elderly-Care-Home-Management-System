import request from "@/utils/request.js";


const careLevelApi = {
    list(careLevelQuery) {
        return request.get("/care-levels", {params: careLevelQuery});
    },
    deleteById(id){
        return request.delete(`/care-levels/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/care-levels",{data: ids} )
    },
    add(careLevel){
        return request.post("/care-levels", careLevel)
    },
    update(id,careLevel){
        return request.put(`/care-levels/${id}`, careLevel)
    },
    selectById(id){
        return request.get(`/care-levels/${id}`)
    }
}

export default careLevelApi
