package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 Test suite for attribute parser.

 @author Jonathan Hedley, jonathan@hedley.net */
public class AttributeParseTest {

    @Test public void parsesRoughAttributeString() {
        String html = "<a id=\"123\" class=\"baz = 'bar'\" style = 'border: 2px'qux zim foo = 12 mux=18 />";
        // should be: <id=123>, <class=baz = 'bar'>, <qux=>, <zim=>, <foo=12>, <mux.=18>

        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(7, attr.size());
        assertEquals("123", attr.get("id"));
        assertEquals("baz = 'bar'", attr.get("class"));
        assertEquals("border: 2px", attr.get("style"));
        assertEquals("", attr.get("qux"));
        assertEquals("", attr.get("zim"));
        assertEquals("12", attr.get("foo"));
        assertEquals("18", attr.get("mux"));
    }

    @Test public void handlesNewLinesAndReturns() {
        String html = "<a\r\nfoo='bar\r\nqux'\r\nbar\r\n=\r\ntwo>One</a>";
        Element el = Jsoup.parse(html).select("a").first();
        assertEquals(2, el.attributes().size());
        assertEquals("bar\r\nqux", el.attr("foo")); // currently preserves newlines in quoted attributes. todo confirm if should.
        assertEquals("two", el.attr("bar"));
    }

    @Test public void parsesEmptyString() {
        String html = "<a />";
        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(0, attr.size());
    }

    @Test public void canStartWithEq() {
        String html = "<a =empty />";
        // TODO this is the weirdest thing in the spec - why not consider this an attribute with an empty name, not where name is '='?
        // am I reading it wrong? https://html.spec.whatwg.org/multipage/parsing.html#before-attribute-name-state
        Element el = Jsoup.parse(html).getElementsByTag("a").get(0);
        Attributes attr = el.attributes();
        assertEquals(1, attr.size());
        assertTrue(attr.hasKey("=empty"));
        assertEquals("", attr.get("=empty"));
    }

    @Test public void strictAttributeUnescapes() {
        String html = "<a id=1 href='?foo=bar&mid&lt=true'>One</a> <a id=2 href='?foo=bar&lt;qux&lg=1'>Two</a>";
        Elements els = Jsoup.parse(html).select("a");
        assertEquals("?foo=bar&mid&lt=true", els.first().attr("href"));
        assertEquals("?foo=bar<qux&lg=1", els.last().attr("href"));
    }

    @Test public void moreAttributeUnescapes() {
        String html = "<a href='&wr_id=123&mid-size=true&ok=&wr'>Check</a>";
        Elements els = Jsoup.parse(html).select("a");
        assertEquals("&wr_id=123&mid-size=true&ok=&wr", els.first().attr("href"));
    }

    @Test public void parsesBooleanAttributes() {
        String html = "<a normal=\"123\" boolean empty=\"\"></a>";
        Element el = Jsoup.parse(html).select("a").first();

        assertEquals("123", el.attr("normal"));
        assertEquals("", el.attr("boolean"));
        assertEquals("", el.attr("empty"));

        List<Attribute> attributes = el.attributes().asList();
        assertEquals(3, attributes.size(), "There should be 3 attribute present");

        assertEquals(html, el.outerHtml()); // vets boolean syntax
    }

    @Test public void dropsSlashFromAttributeName() {
        String html = "<img /onerror='doMyJob'/>";
        Document doc = Jsoup.parse(html);
        assertFalse(doc.select("img[onerror]").isEmpty(), "SelfClosingStartTag ignores last character");
        assertEquals("<img onerror=\"doMyJob\">", doc.body().html());

        doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<img onerror=\"doMyJob\" />", doc.html());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixofXlink() {
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

        Element rv = doc.select("body").get(0).children().get(1);

        assertEquals("xlinkU00003Ahref", rv.attributes().asList().get(2).getConvertedKey());

        assertEquals("UnboundPrefix", rv.attributes().asList().get(2).getValue());
        assertEquals("UnboundPrefix", rv.attributes().get("xlink:href"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixofXlink2() {
        String h = "<!doctype html>\n" +
                "<html lang=\"de\">\n" +
                "    <head>\n" +
                "\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\t<test:h1>UnboundPrefix</test:h1>\n" +
                "\t<svg width=\"180\" height=\"180\" xlink:actuate=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "      \t</svg>\n" +
                "    </body>\n" +
                "</html>\n";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(1);

        assertEquals("xlinkU00003Aactuate", rv.attributes().asList().get(2).getConvertedKey());

        assertEquals("UnboundPrefix", rv.attributes().asList().get(2).getValue());
        assertEquals("UnboundPrefix", rv.attributes().get("xlink:actuate"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixofXlink3() {
        String h = "<!doctype html>\n" +
                "<html lang=\"de\">\n" +
                "    <head>\n" +
                "\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\t<test:h1>UnboundPrefix</test:h1>\n" +
                "\t<svg width=\"180\" height=\"180\" xlink:space=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "      \t</svg>\n" +
                "    </body>\n" +
                "</html>\n";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(1);

        assertEquals("xlinkU00003Aspace", rv.attributes().asList().get(2).getConvertedKey());

        assertEquals("UnboundPrefix", rv.attributes().asList().get(2).getValue());
        assertEquals("UnboundPrefix", rv.attributes().get("xlink:space"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixofXlink4() {
        String h = "<!doctype html>\n" +
                "<html lang=\"de\">\n" +
                "    <head>\n" +
                "\n" +
                "    </head>\n" +
                "    <body>\n" +
                "\t<test:h1>UnboundPrefix</test:h1>\n" +
                "\t<svg width=\"180\" height=\"180\" xmlns:xlink=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "      \t</svg>\n" +
                "    </body>\n" +
                "</html>\n";

        Document doc = Jsoup.parse(h);

        Element rv = doc.select("body").get(0).children().get(1);

        assertEquals("xmlnsU00003Axlink", rv.attributes().asList().get(2).getConvertedKey());

        assertEquals("UnboundPrefix", rv.attributes().asList().get(2).getValue());
        assertEquals("UnboundPrefix", rv.attributes().get("xmlns:xlink"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixofXlink5() {
        String h = "<svg width=\"180\" height=\"180\" xmlns:xlink=\"UnboundPrefix\">\n" +
                "        \t<rect x=\"20\" y=\"20\" rx=\"20\" ry=\"20\" width=\"100\" height=\"100\" style=\"fill:lightgray; stroke:#1c87c9; stroke-width:4;\"/>\n" +
                "\t</svg>\n";

        Document doc = Jsoup.parse(h);

        Elements rv = doc.select("svg");

        assertTrue(rv.hasAttr("xmlns:xlink"));

        assertEquals("xmlnsU00003Axlink", rv.first().attributes().asList().get(2).getConvertedKey());
        assertEquals("UnboundPrefix", rv.first().attributes().asList().get(2).getValue());
        assertEquals("UnboundPrefix", rv.first().attributes().get("xmlns:xlink"));
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixDot() {
        Attribute attr = new Attribute("x.", "");

        assertEquals("x.", attr.getKey());
        assertEquals("xU00002E", attr.getConvertedKey());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixDot2() {
        Attribute attr = new Attribute("a", "");
        String convetedKey = attr.convertSymbol("x.");

        assertEquals("xU00002E", convetedKey);
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixComma() {
        Attribute attr = new Attribute("abc:a", "");

        assertEquals("abc:a", attr.getKey());
        assertEquals("abcU00003Aa", attr.getConvertedKey());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixComma2() {
        Attribute attr = new Attribute("a", "");
        String convetedKey = attr.convertSymbol("abc:a");

        assertEquals("abcU00003Aa", convetedKey);
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixDash() {
        Attribute attr = new Attribute("a-a", "");

        assertEquals("a-a", attr.getKey());
        assertEquals("aU00002Da", attr.getConvertedKey());
    }

    // CS427 Issue link: https://github.com/jhy/jsoup/issues/1341
    // Test the attribute name like `xlink:href`
    // Issue #1341
    // https://github.com/jhy/jsoup/issues/1341
    @Test public void handleUnboundPrefixDash2() {
        Attribute attr = new Attribute("a", "");
        String convetedKey = attr.convertSymbol("a-a");

        assertEquals("aU00002Da", convetedKey);
    }
}
