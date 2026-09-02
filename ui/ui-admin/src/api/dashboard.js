import request from "@/utils/request.js";


const dashboardApi = {
    stats() {
        return request.get("/dashboard/stats")
    }
}

export default dashboardApi
