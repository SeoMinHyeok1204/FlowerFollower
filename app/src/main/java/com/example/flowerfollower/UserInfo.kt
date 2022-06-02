package com.example.flowerfollower
// 유저 정보를 파이어베이스에 올리거나 파이어베이스에서 가져올 때 사용하는 클래스
data class UserInfo(val nickName:String, val email:String, val password:String) {
    constructor():this("1", "2", "3") {

    }
}