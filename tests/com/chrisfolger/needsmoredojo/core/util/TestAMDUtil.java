package com.chrisfolger.needsmoredojo.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAMDUtil
{
    @Test
    public void dojoModuleWithHyphenConvertedCorrectly()
    {
        assertEquals("domConstruct", AMDUtil.defineToParameter("dojo/dom-construct"));
    }

    @Test
    public void testModuleWithUnderscore()
    {
        assertEquals("WidgetBase", AMDUtil.defineToParameter("dijit/_WidgetBase"));
    }

    @Test
    public void testBasicModule()
    {
        assertEquals("BorderContainer", AMDUtil.defineToParameter("dijit/layout/BorderContainer"));
    }

    @Test
    public void testTextPlugin()
    {
        assertEquals("aModuleTemplate", AMDUtil.defineToParameter("dojo/text!./AModule.html"));
    }
}
