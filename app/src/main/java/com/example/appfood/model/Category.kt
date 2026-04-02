package com.example.appfood.model

import java.io.Serializable

data class Category(
    var id: Int = 0,
    var name: String = "",
    var imagePath: String = ""
) : Serializable