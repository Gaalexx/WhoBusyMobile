package com.gaalex.whobusymp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform