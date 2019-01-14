package com.multiple_lang_crawling

data class Comic(
    var title: String,
    var pageList: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}