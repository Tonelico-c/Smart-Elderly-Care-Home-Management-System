import request from "@/utils/request.js";


const buildingApi = {
    list(buildingQuery) {
        return request.get("/buildings", {params: buildingQuery});
    },
    stats(){
        return request.get("/buildings/stats")
    },
    deleteById(id){
        return request.delete(`/buildings/${id}`)
    },
    deleteBatch(ids){
        return request.delete("/buildings",{data: ids} )
    },
    add(building){
        return request.post("/buildings", building)
    },
    update(id,building){
        return request.put(`/buildings/${id}`, building)
    },
    selectById(id){
        return request.get(`/buildings/${id}`)
    }
}

export default buildingApi
