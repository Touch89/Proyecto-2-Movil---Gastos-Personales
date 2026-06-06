package com.example.proyectos2gastospersonales

data class Group(
    val id: String,
    var name: String,
    var description: String,
    var balance: Double,
    var members: List<String>,
    var movements: List<String>
)
