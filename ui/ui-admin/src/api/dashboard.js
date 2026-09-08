import request from "@/utils/request.js";


const dashboardApi = {
    stats() {
        return request.get("/dashboard/stats")
    },
    //老人年龄分布（首页ECharts图表）
    ageDistribution() {
        return request.get("/dashboard/ageDistribution")
    }
}

export default dashboardApi
