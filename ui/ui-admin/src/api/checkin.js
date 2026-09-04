import request from "@/utils/request.js";


const checkInApi = {
    list(checkInRecordQuery) {
        return request.get("/checkin-records", {params: checkInRecordQuery});
    },
    listAvailableBeds(params) {
        return request.get("/checkin-records/available-beds", {params});
    },
    add(checkInRecord) {
        return request.post("/checkin-records", checkInRecord)
    },
    checkout(id, checkInRecord) {
        return request.put(`/checkin-records/${id}/checkout`, checkInRecord)
    },
    listAvailableElders() {
        return request.get("/checkin-records/available-elders");
    },
    updateRoom(id, checkInRecord){
        return request.put(`/checkin-records/${id}/update-room`, checkInRecord)
    }
}

export default checkInApi
