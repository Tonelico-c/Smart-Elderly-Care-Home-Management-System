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
    selectAssignedPermission(roleId) {
        return request.get(`/roles/selectAssignedPermission/${roleId}`)
    },
    assignPermission(roleId, permissionIds) {
        return request.post('/roles/assignPermission', null, {params: {roleId, permissionIds}})
    }
}

export default roleApi