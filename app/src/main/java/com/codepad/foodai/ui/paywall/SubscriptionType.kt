package com.codepad.foodai.ui.paywall

enum class SubscriptionType(val id: String, val displayName: String) {
    WEEKLY("foodai_weekly_tier1", "WEEKLY"),
    ANNUAL("foodai_yearly_standart", "ANNUAL"),
    SPECIAL("foodai_special_discount", "SPECIAL"),
    WEEKLY_NO_TRIAL("foodai_weekly_no_trial", "CUSTOM")
}