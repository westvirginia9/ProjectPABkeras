package com.example.projectpabkeras.models

data class Goal(
    val goalId: String = "",
    val goalName: String = "",
    val goalDescription: String = "",
    val goalTargetAmount: Int = 0,
    val currentAmount: Int = 0,
    val goalPeriod: String = "",
    val userId: String = "",
    val isCompleted: Boolean = false
)
