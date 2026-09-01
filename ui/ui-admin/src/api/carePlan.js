import request from "@/utils/request.js";


const carePlanApi = {
    list(carePlanQuery) {
        return request.get("/care-plans", {params: carePlanQuery});
    },
    deleteById(id){
        return request.delete(`/care-plans/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/care-plans",{data: ids} )
    },
    add(carePlan){
        return request.post("/care-plans", carePlan)
    },
    update(id,carePlan){
        return request.put(`/care-plans/${id}`, carePlan)
    },
    selectById(id){
        return request.get(`/care-plans/${id}`)
    }
}

export default carePlanApi
