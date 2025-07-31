package com.example.phychosiolz.data.enums

enum class SportType(val code: Int, val description: String) {
    跑步(0, "跑步"),
    游泳(1, "游泳"),
    徒步(2, "徒步"),
    骑行(3, "骑行"),
    划船(4, "划船"),
    其它(5, "其它");
    companion object {
        fun fromCode(code: Int): SportType {
            return values().find { it.code == code } ?: 其它
        }
    }
}