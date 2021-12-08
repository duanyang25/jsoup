package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.MultiLocaleExtension.MultiLocaleTest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 Tag tests.
 @author Jonathan Hedley, jonathan@hedley.net */
public class TagTest {
    @Test public void isCaseSensitive() {
        Tag p1 = Tag.valueOf("P");
        Tag p2 = Tag.valueOf("p");
        assertNotEquals(p1, p2);
    }

    @MultiLocaleTest
    public void canBeInsensitive(Locale locale) {
        Locale.setDefault(locale);

        Tag script1 = Tag.valueOf("script", ParseSettings.htmlDefault);
        Tag script2 = Tag.valueOf("SCRIPT", ParseSettings.htmlDefault);
        assertSame(script1, script2);
    }

    @Test public void trims() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf(" p ");
        assertEquals(p1, p2);
    }

    @Test public void equality() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        assertEquals(p1, p2);
        assertSame(p1, p2);
    }

    @Test public void divSemantics() {
        Tag div = Tag.valueOf("div");

        assertTrue(div.isBlock());
        assertTrue(div.formatAsBlock());
    }

    @Test public void pSemantics() {
        Tag p = Tag.valueOf("p");

        assertTrue(p.isBlock());
        assertFalse(p.formatAsBlock());
    }

    @Test public void imgSemantics() {
        Tag img = Tag.valueOf("img");
        assertTrue(img.isInline());
        assertTrue(img.isSelfClosing());
        assertFalse(img.isBlock());
    }

    @Test public void defaultSemantics() {
        Tag foo = Tag.valueOf("FOO"); // not defined
        Tag foo2 = Tag.valueOf("FOO");

        assertEquals(foo, foo2);
        assertTrue(foo.isInline());
        assertTrue(foo.formatAsBlock());
    }

    @Test public void valueOfChecksNotNull() {
        assertThrows(IllegalArgumentException.class, () -> Tag.valueOf(null));
    }

    @Test public void valueOfChecksNotEmpty() {
        assertThrows(IllegalArgumentException.class, () -> Tag.valueOf(" "));
    }

    @Test public void knownTags() {
        assertTrue(Tag.isKnownTag("div"));
        assertFalse(Tag.isKnownTag("explain"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the tag containing symbols like `:`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleSymbolTags() {
        String h = "<!doctype html>\n" +
                "<html lang=\"de\">\n" +
                "    <head>\n" +
                "\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\t<test:h1>UnboundPrefix</test:h1>\n" +
                "\t<svg width=\"180\" height=\"180\" xlink:href=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "      \t</svg>\n" +
                "    </body>\n" +
                "</html>\n";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(0);
        assertEquals("testU00003Ah1", rv.tag().unicodeName());
        assertEquals("test:h1", rv.tagName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the tag containing symbols like `:`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleSymbolTags2() {
        String h = "<!doctype html>\n" +
                "<html lang=\"de\">\n" +
                "    <head>\n" +
                "\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\t<a:h1>UnboundPrefix</a:h1>\n" +
                "\t<svg width=\"180\" height=\"180\" xlink:href=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "      \t</svg>\n" +
                "    </body>\n" +
                "</html>\n";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(0);
        assertEquals("aU00003Ah1", rv.tag().unicodeName());
        assertEquals("a:h1", rv.tagName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the tag containing symbols like `:`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleSymbolTags3() {
        String h = "<a:h1>UnboundPrefix</a:h1>";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(0);
        assertEquals("aU00003Ah1", rv.tag().unicodeName());
        assertEquals("a:h1", rv.tagName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagDot() {
        String h = "<a.>UnboundPrefix</a.>";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        assertEquals("aU00002E", tag.unicodeName());
        assertEquals("a.", tag.getName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagDot2() {
        String h = "<a.>UnboundPrefix</a.>";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        String convetedKey = tag.convertSymbol("a.");
        assertEquals("aU00002E", convetedKey);
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagComma() {
        String h = "<a,>UnboundPrefix</a,>";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        assertEquals("aU00002C", tag.unicodeName());
        assertEquals("a,", tag.getName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagComma2() {
        String h = "<a,>UnboundPrefix</a,>";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        String convetedKey = tag.convertSymbol("a,");
        assertEquals("aU00002C", convetedKey);
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagDash() {
        String h = "<a->UnboundPrefix</a->";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        assertEquals("aU00002D", tag.unicodeName());
        assertEquals("a-", tag.getName());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void convertTagDash2() {
        String h = "<a->UnboundPrefix</a->";
        Document doc = Jsoup.parse(h);
        Tag tag = doc.select("body").get(0).children().get(0).tag();

        String convetedKey = tag.convertSymbol("a-");
        assertEquals("aU00002D", convetedKey);
    }
}
