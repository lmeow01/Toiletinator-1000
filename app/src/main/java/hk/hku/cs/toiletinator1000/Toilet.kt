package hk.hku.cs.toiletinator1000

import com.google.android.gms.maps.model.LatLng

class Toilet {
    private var id: String = ""
    private var floor: String = ""
    private var building: String = ""
    private var latLng: LatLng = LatLng(0.0, 0.0)

    constructor(id: String, floor: String, building: String, latLng: LatLng) {
        this.id = id
        this.floor = floor
        this.building = building
        this.latLng = latLng
    }


    fun getId(): String {
        return this.id
    }

    fun getFloor(): String {
        return this.floor
    }

    fun getBuilding(): String {
        return this.building
    }

    fun getLatLng(): LatLng {
        return this.latLng
    }

}