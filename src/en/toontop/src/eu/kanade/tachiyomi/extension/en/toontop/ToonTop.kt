package eu.kanade.tachiyomi.extension.en.toontop

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import io.github.keiyoushi.annotation.Source
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Source
class ToonTop : ParsedHttpSource() {

    override val name = "ToonTop"
    override val baseUrl = "https://toontop.io"
    override val lang = "en"
    override val supportsLatest = true

    override val headers = super.headers.newBuilder()
        .add("Referer", "$baseUrl/")
        .build()

    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/most-popular?page=$page", headers)
    override fun popularMangaSelector() = "div.flw-item"
    override fun popularMangaNextPageSelector() = "li.page-item.active + li a"

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        val link = element.selectFirst("a")!!
        setUrlWithoutDomain(link.attr("href"))
        title = link.attr("title").ifEmpty { link.text() }
        thumbnail_url = element.selectFirst("img")?.attr("data-src") ?: element.selectFirst("img")?.attr("src")
    }

    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/latest-release?page=$page", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/search?keyword=$query&page=$page", headers)

    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1, h2.film-name")?.text() ?: name
    }

    override fun chapterListSelector() = "a.chapter, .c-item a"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.attr("href"))
        name = element.text()
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select("img").mapIndexed { index, element ->
            val url = element.attr("data-src").ifEmpty { element.attr("src") }
            Page(index, imageUrl = url)
        }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException()
}
