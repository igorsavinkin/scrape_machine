package com.scraperservice.scraper;

import com.scraperservice.scraper.page.PageType;
import com.scraperservice.storage.DataArray;
import com.scraperservice.storage.DataCell;
import com.scraperservice.utils.RegexUtils;
import com.scraperservice.utils.ScrapeUtils;
import org.jsoup.nodes.Document;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScraperExample extends Scraper {
    private static final String BASE_SITE_URL = "";

    private static final String CATEGORY_SELECTOR = "";
    private static final String SUBCATEGORY_SELECTOR = "";
    private static final String PRODUCT_SELECTOR = "";
    private static final String NEXT_PAGE_SELECTOR = "";

    private static final String NAME_SELECTOR = "";
    private static final String ARTICLE_SELECTOR = "";
    private static final String PRICE_SELECTOR = "";

    @Override
    public List<DataArray> scrapeData(Document document, String productUrl) {
        DataArray dataArray = new DataArray(productUrl);
        dataArray.add(new DataCell(1, "name", ScrapeUtils.getText(document, NAME_SELECTOR)));
        dataArray.add(new DataCell(2, "article", ScrapeUtils.getText(document, ARTICLE_SELECTOR)));
        dataArray.add(new DataCell(3, "price", ScrapeUtils.getText(document, PRICE_SELECTOR)));
        return Collections.singletonList(dataArray);
    }

    @Override
    public PageType getPageType(Document document) {
        boolean isHaveCategoryPageSign = document.selectFirst(String.join(",",
                CATEGORY_SELECTOR, SUBCATEGORY_SELECTOR, NEXT_PAGE_SELECTOR, PRODUCT_SELECTOR)) != null;
        boolean isHaveProductPageSign = document.selectFirst(NAME_SELECTOR) != null;
        if(isHaveCategoryPageSign && isHaveProductPageSign)
            return PageType.CATEGORY_AND_PRODUCT_PAGE;
        else if(isHaveCategoryPageSign)
            return PageType.CATEGORY_PAGE;
        else if(isHaveProductPageSign)
            return PageType.PRODUCT_PAGE;
        else
            return PageType.UNDEFINED;
    }

    @Override
    public List<String> getStartLinks() {
        return Collections.singletonList("https://testsite.com/products");
    }

    @Override
    public List<String> scrapeCategories(Document document) {
        return ScrapeUtils.getAttributes(document, CATEGORY_SELECTOR, "href")
                .stream().map(link -> ScrapeUtils.joinBaseUrlAndLink(BASE_SITE_URL, link))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> scrapeSubCategories(Document document) {
        return ScrapeUtils.getAttributes(document, SUBCATEGORY_SELECTOR, "href")
                .stream().map(link -> ScrapeUtils.joinBaseUrlAndLink(BASE_SITE_URL, link))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> scrapeLinksToProductPages(Document document) {
        return ScrapeUtils.getAttributes(document, PRODUCT_SELECTOR, "href")
                .stream().map(link -> ScrapeUtils.joinBaseUrlAndLink(BASE_SITE_URL, link))
                .collect(Collectors.toList());
    }

    @Override
    public String goToNextPage(Document document) {
        return ScrapeUtils.joinBaseUrlAndLink(BASE_SITE_URL,
                ScrapeUtils.getAttribute(document, NEXT_PAGE_SELECTOR, "href"));
    }

    @Override
    public String goToNextPage(String url) {
        String regex = "page=([0-9]+)";
        String pageIndex = RegexUtils.findText(regex, url, 1);
        if(!pageIndex.isEmpty()) {
            int index = Integer.parseInt(pageIndex);
            index++;
            return url.replaceFirst(regex, "page=" + index);
        }
        return null;
    }
}