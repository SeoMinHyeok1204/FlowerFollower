package com.example.flowerfollower
// 마이 가든 정보를 파이어베이스에 올리거나 파이어베이스에서 가져올 때 사용하는 클래스
data class gardenClass(val flowerName : String,
                        val plantDate : String,
                        val epoch : String,
                        val imageUrl : String,
                        val imageDate : String,
                        val latitude : String,
                        val longitude : String)
