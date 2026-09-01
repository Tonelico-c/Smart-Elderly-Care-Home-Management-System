import request from "@/utils/request.js";


const careTaskApi = {
  list(careTaskQuery) {
    return request.get("/care-tasks", { params: careTaskQuery });
  },
  deleteById(id) {
    return request.delete(`/care-tasks/${id}`)
  },
  deleteBatch(ids) {
    return request.delete("/care-tasks", { data: ids })
  },
  update(id, careTask) {
    return request.put(`/care-tasks/${id}`, careTask)
  },
  selectById(id) {
    return request.get(`/care-tasks/${id}`)
  }
}

export default careTaskApi
