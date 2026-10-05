package com.igrupos.domain.model


data class ChecklistItem(
    val id: Long,
    val label: String,
    val isEnabled: Boolean,
    val orderIndex: Int
)
