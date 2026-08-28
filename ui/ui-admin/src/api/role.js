import request from "@/utils/request";


const roleApi = {
    list(roleQuery){
        return request.get("/roles", {params: roleQuery});
    },
    deleteById(id){
        return request.delete(`/roles/${id}`);
    },
    deleteBatch(ids){
        return request.delete("/roles",{data: ids} );
    },
    add(role){
        return request.post("/roles", role);
    },
    update(id,role){
        return request.put(`/roles/${id}`, role);
    },
    selectById(id){
        return request.get(`/roles/${id}`);
    },
    selectRelatedPermission(roleId, pageQuery){
        return request.get(`/roles/selectRelatedPermission/${roleId}`, {params: pageQuery})
    }
}

export default roleApi