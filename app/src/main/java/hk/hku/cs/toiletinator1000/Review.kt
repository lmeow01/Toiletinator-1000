package hk.hku.cs.toiletinator1000

class Review {
    private var toiletId: String
    private var stars: Double = 0.0
    private var comment: String = ""

    constructor(toiletId: String, stars: Double, comment: String) {
        this.toiletId = toiletId
        this.stars = stars
        this.comment = comment
    }

    fun getToiletId(): String {
        return this.toiletId
    }

    fun getStars(): Double {
        return this.stars
    }

    fun getComment(): String {
        return this.comment
    }
}