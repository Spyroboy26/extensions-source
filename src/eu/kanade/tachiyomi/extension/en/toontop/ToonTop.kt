package eu.kanade.tachiyomi.extension.en.toontop

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ToonTop : ParsedHttpSource() {

    override val name = "ToonTop"
    override val baseUrl = "https://toontop.io"
    override val lang = "en"
    override val supportsLatest = true

    override val headers = super.headers.newBuilder()
        .add("Referer", "$baseUrl/")
        .build()

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/most-popular?page=$page", headers)

    override fun popularMangaSelector() = ".flw-item"
    override fun popularMangaNextPageSelector() = ".pagination .active + li a"

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        val link = element.selectFirst(".film-name a")!!
        setUrlWithoutDomain(link.attr("href"))
        title = link.text()
        thumbnail_url = element.selectFirst("img")?.attr("data-src")
            ?: element.selectFirst("img")?.attr("src")
    }

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl/latest-release?page=$page", headers)

    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/search?keyword=$query&page=$page", headers)

    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1, .film-name")?.text() ?: "Unknown"
        description = document.selectFirst(".film-description, .description")?.text()
        genre = document.select(".item-list a").joinToString { it.text() }
        thumbnail_url = document.selectFirst("img.film-poster-img, .film-poster img")?.attr("src")
    }

    override fun chapterListSelector() = ".c-item a, ul.episodes-list li a"

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        setUrlWithoutDomain(element.attr("href"))
        name = element.selectFirst(".name, .chapter-name")?.text() ?: element.text()
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select(".read-content img, .reader-area img, .chapter-images img")
            .mapIndexed { index, element ->
                val imageUrl = element.attr("data-src").ifEmpty { element.attr("src") }
                Page(index, imageUrl = imageUrl)
            }
    }

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException()
}
