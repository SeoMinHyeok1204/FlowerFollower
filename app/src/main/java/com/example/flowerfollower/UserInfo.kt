package com.example.flowerfollower

data class UserInfo(val nickName:String, val email:String, val password:String) {
    constructor():this("1", "2", "3") {

    }
}