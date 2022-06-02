package com.example.flowerfollower
// 글을 파이어베이스에 올리거나 파이어베이스에서 가져올 때 사용하는 클래스
data class CommunityPosting(val uid : String
                            , val title:String
                            , val content:String
                            , val time : String
                            , val commentNum : String
                            , val nickname : String
                            , val imageUrl : String
                            , val epoch : String
                            , val postingID : String)
