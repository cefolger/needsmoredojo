package com.chrisfolger.needsmoredojo.core.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestAMDUtil
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
        assertEquals("domConstruct", AMDUtil.defineToParameter("dojo/dom-construct", exceptions));
    }

    @Test
    public void testModuleWithUnderscore()
    {
        assertEquals("WidgetBase", AMDUtil.defineToParameter("dijit/_WidgetBase", exceptions));
    }

    @Test
    public void testBasicModule()
    {
        assertEquals("BorderContainer", AMDUtil.defineToParameter("dijit/layout/BorderContainer", exceptions));
    }

    @Test
    public void testBaseFx()
    {
        assertEquals("baseFx", AMDUtil.defineToParameter("dojo/_base/fx", exceptions));
    }

    @Test
    public void testTextPlugin()
    {
        assertEquals("amoduleTemplate", AMDUtil.defineToParameter("dojo/text!./AModule.html", exceptions));
    }

    @Test
    public void testI18nPlugin()
    {
        assertEquals("resources", AMDUtil.defineToParameter("dojo/i18n!nls/resources", exceptions));
    }

    @Test
    public void testException()
    {
        // has is an explicit exception
        exceptions.put("dojo/sniff", "has");
        assertEquals("has", AMDUtil.defineToParameter("dojo/sniff", exceptions));
    }
}
