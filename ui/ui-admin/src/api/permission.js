import request from "@/utils/request.js";


const permissionApi = {
    /*list(permissionQuery){
        return request.get("/permissions", {params: permissionQuery});
    },*/
    selectPermissionTree(){
        return request.get("/permissions/selectPermissionTree");
    },
    selectById(id){
        return request.get(`/permissions/${id}`);
    },
    add(permission){
        return request.post("/permissions", permission);
    },
    update(id, permission){
        return request.put(`/permissions/${id}`, permission);
    },
    deleteById(id){
        return request.delete(`/permissions/${id}`);
    },
    deleteBatch(ids){
        return request.delete("/permissions", {data: ids});
    },
    selectParentId(){
        return request.get("/permissions/permissionVO");
    }
}

export default permissionApi