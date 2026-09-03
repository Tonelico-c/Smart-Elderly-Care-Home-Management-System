import request from "@/utils/request.js";


const examAppointmentApi = {
    list(examAppointmentQuery) {
        return request.get("/exam-appointments", { params: examAppointmentQuery });
    },
    deleteById(id) {
        return request.delete(`/exam-appointments/${id}`)
    },
    deleteBatch(ids) {
        return request.delete("/exam-appointments", { data: ids })
    },
    add(examAppointment) {
        return request.post("/exam-appointments", examAppointment)
    },
    update(id, examAppointment) {
        return request.put(`/exam-appointments/${id}`, examAppointment)
    },
    selectById(id) {
        return request.get(`/exam-appointments/${id}`)
    },
    listItems(id) {
        return request.get(`/exam-appointments/${id}/items`)
    },
    saveResults(id, items) {
        return request.put(`/exam-appointments/${id}/results`, items)
    }
}

export default examAppointmentApi
