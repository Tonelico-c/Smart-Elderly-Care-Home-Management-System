import request from "@/utils/request.js";

const elderApi = {
    login(elder) {
        return request.post("/elders/login", elder)
    },
    elderInfo() {
        return request.get("/elders/elderInfo")
    },
    resetPassword(elderPasswordDTO) {
        return request.post("/elders/resetPassword", elderPasswordDTO)
    },
    //修改基本资料（姓名/头像/联系电话/家庭住址）
    updateInfo(elderInfoUpdateDTO) {
        return request.post("/elders/updateInfo", elderInfoUpdateDTO)
    },
    //头像上传：走通用上传接口，临时把baseURL改为/api以命中后端/upload
    uploadAvatar(file) {
        const formData = new FormData()
        formData.append("file", file)
        return request.post("/upload", formData, {baseURL: "/api"})
    }
}

export default elderApi
