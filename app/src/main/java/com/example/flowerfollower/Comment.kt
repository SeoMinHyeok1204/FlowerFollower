package com.example.flowerfollower
// 댓글을 파이어베이스에 올리거나 파이어베이스에서 가져올 때 사용하는 클래스
data class Comment(val writer : String, val id : String, val content : String, val time : String, val epoch : String, val commentID : String)
