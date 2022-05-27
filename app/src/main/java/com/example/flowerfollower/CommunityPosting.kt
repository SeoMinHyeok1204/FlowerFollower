package com.example.flowerfollower

data class CommunityPosting(val uid : String
                            , val title:String
                            , val content:String
                            , val time : String
                            , val commentNum : String
                            , val nickname : String
                            , val imageUrl : String
                            , val epoch : String
                            , val postingID : String)
//{
//    constructor():this("0", "오류", "글을 불러오지 못했습니다", "???", "0", "error", "0", "0", "0")
//}
