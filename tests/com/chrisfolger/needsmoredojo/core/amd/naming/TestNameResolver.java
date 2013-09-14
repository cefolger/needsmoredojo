package com.chrisfolger.needsmoredojo.core.amd.naming;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestNameResolver
{
    private Map<String, String> exceptions;

    @Before
    public void setup()
    {
        exceptions = new HashMap<String, String>();
    }

    @Test
    public void dojoModuleWithHyphenConvertedCorrectly()
    {
        assertEquals("domConstruct", NameResolver.defineToParameter("dojo/dom-construct", exceptions));
    }

    @Test
    public void testModuleWithUnderscore()
    {
        assertEquals("WidgetBase", NameResolver.defineToParameter("dijit/_WidgetBase", exceptions));
    }

    @Test
    public void testBasicModule()
    {
        assertEquals("BorderContainer", NameResolver.defineToParameter("dijit/layout/BorderContainer", exceptions));
    }

    @Test
    public void testBaseFx()
    {
        assertEquals("baseFx", NameResolver.defineToParameter("dojo/_base/fx", exceptions));
    }

    @Test
    public void testTextPlugin()
    {
        assertEquals("amoduleTemplate", NameResolver.defineToParameter("dojo/text!./AModule.html", exceptions));
    }

    @Test
    public void testI18nPlugin()
    {
        assertEquals("resources", NameResolver.defineToParameter("dojo/i18n!nls/resources", exceptions));
    }

    @Test
    public void testException()
    {
        // has is an explicit exception
        exceptions.put("dojo/sniff", "has");
        assertEquals("has", NameResolver.defineToParameter("dojo/sniff", exceptions));
    }

    @Test
    public void testHyphenatedModuleConversions()
    {
        assertEquals("dom-class", NameResolver.getPossibleHyphenatedModule("domClass"));
        assertEquals("dom-attr", NameResolver.getPossibleHyphenatedModule("domAttr"));
        assertEquals("some-module", NameResolver.getPossibleHyphenatedModule("someModule"));
    }

    @Test
    public void testGetModuleName()
    {
        assertEquals("d", NameResolver.getModuleName("a/b/c/d"));
        assertEquals("d", NameResolver.getModuleName("a/b/d!foo"));
    }

    @Test
    public void testGetModuleName_quotesAreStripped()
    {
        assertEquals("d", NameResolver.getModuleName("\"a/b/c/d\""));
    }

    @Test
    public void testConversionOfCustomPlugins()
    {
        assertEquals("foo", NameResolver.defineToParameter("foo!bar", exceptions));
    }

    @Test
    public void defineToParameter_quotesAreStripped()
    {
        assertEquals("Foo", NameResolver.defineToParameter("\"Foo\"", exceptions));
    }
}
