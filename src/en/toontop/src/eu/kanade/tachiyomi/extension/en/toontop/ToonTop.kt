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

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/", headers)

    override fun popularMangaSelector() = "div.flw-item"

    override fun popularMangaNextPageSelector() = "a.page-link.next"

    override fun popularMangaFromElement(element: Element): SManga =
        SManga.create().apply {
            val link = element.selectFirst("a")!!
            setUrlWithoutDomain(link.attr("href"))
            title = link.attr("title").ifEmpty { link.text() }
            thumbnail_url = element.selectFirst("img")?.attr("src")
        }

    override fun latestUpdatesRequest(page: Int): Request =
        popularMangaRequest(page)

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun latestUpdatesFromElement(element: Element) =
        popularMangaFromElement(element)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/?s=$query&page=$page", headers)

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaFromElement(element: Element) =
        popularMangaFromElement(element)

    override fun mangaDetailsParse(document: Document): SManga {
    return SManga.create().apply {
        title = name
    }
    }

    override fun chapterListSelector() = "a.chapter"

    override fun chapterFromElement(element: Element): SChapter =
        SChapter.create().apply {
            setUrlWithoutDomain(element.attr("href"))
            name = element.text()
        }

    override fun pageListParse(document: Document): List<Page> =
        emptyList()

    override fun imageUrlParse(document: Document): String =
        throw UnsupportedOperationException()
}
