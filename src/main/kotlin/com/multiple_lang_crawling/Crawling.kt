package com.multiple_lang_crawling

import com.gargoylesoftware.htmlunit.AjaxController
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.*
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.apache.commons.io.FileUtils
import java.io.File
import javax.swing.JOptionPane

val client = OkHttpClient()
val web = WebClient()

fun main(args: Array<String>) {
    val getUrlInfo = getUrl()

    if (getUrlInfo.type == "1") {
        getImageList(getUrlInfo.url)
    } else if (getUrlInfo.type == "2") {
        val comic = getPageList(getUrlInfo.url)
        for (imagePage in comic.pageList) {
            getImageList(imagePage, comic.title)
        }
    }
}


fun getUrl(): UrlType {
    val urlType = UrlType(
        JOptionPane.showInputDialog("URL을 입력해주세요.").trim { it <= ' ' },
        JOptionPane.showInputDialog("단편이면 1, 묶음이면 2를 입력해주세요.").trim { it <= ' ' }
    )

    println(urlType.url + " - " + urlType.type)

    return urlType
}


fun getPageList(url: String): Comic {
    web.options.isThrowExceptionOnScriptError = false
    web.options.isJavaScriptEnabled = true
    web.cookieManager.isCookiesEnabled = false
    web.ajaxController = AjaxController()

    val doc = web.getPage<HtmlPage>(url.trim { it <= ' ' })
    val pageTitle = "//div[@class='red title']"
    val comicUrlList = "//div[@class='chapter-list']/div[@class='slot ']/a"

    val titleList = doc.getByXPath<HtmlDivision>(pageTitle)
    val comicList = doc.getByXPath<HtmlAnchor>(comicUrlList)

    println(comicList)

    val comic = Comic()

    comic.title = "/" + titleList[0].textContent

    comicList.reverse()
    for (i in comicList.indices) {
        comic.pageList.add(comicList[i].hrefAttribute)
    }

    return comic
}

fun getImageList(url: String, parentPath: String = "/단편") {

    web.options.isThrowExceptionOnScriptError = false
    web.options.isJavaScriptEnabled = true
    web.cookieManager.isCookiesEnabled = false
    web.ajaxController = AjaxController()

    val dir = File(System.getProperty("user.home") + "/comicbook_crawling" + parentPath)

    val page = web.getPage<HtmlPage>(url.trim { it <= ' ' })

    val titleXPath = "//div[@class='subject']/h1"
    val imageXpath = "//div[@class='view-content scroll-viewer']/img"

    val titleList = page.getByXPath<HtmlHeading1>(titleXPath)
    val title_dir = File(dir.path + "/" + titleList[0].textContent)
    title_dir.mkdirs()

    val imgList = page.getByXPath<HtmlImage>(imageXpath)

    println(titleList[0].textContent)

    imgUrlToLocal(imgList, title_dir)
}

fun imgUrlToLocal(imgList: MutableList<HtmlImage>, title_dir: File) {
    for (i in imgList.indices) {
        val str = imgList[i]

        val request = Request.Builder()
            .url(str.srcAttribute.trim { it <= ' ' })
            .get()
            .build()

        val response = client.newCall(request).execute()
        FileUtils.copyInputStreamToFile(
            response.body().byteStream(),
            File(
                title_dir.path + "/" + i + str.srcAttribute.substring(
                    str.srcAttribute.length - 4,
                    str.srcAttribute.length
                )
            )
        )
        println(response.toString())
    }
}
