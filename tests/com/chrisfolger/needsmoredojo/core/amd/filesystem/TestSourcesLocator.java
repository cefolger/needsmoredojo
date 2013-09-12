package com.chrisfolger.needsmoredojo.core.amd.filesystem;

import com.chrisfolger.needsmoredojo.core.amd.SourceLibrary;
import com.chrisfolger.needsmoredojo.core.amd.importing.ImportResolver;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSourcesLocator
{
    private SourcesLocator locator;
    private List<SourceLibrary> libraries;

    @Before
    public void setup()
    {
        locator = new SourcesLocator();
        libraries = new ArrayList<SourceLibrary>();

        // libraries are guaranteed to be sorted by length
        libraries.add(new SourceLibrary("dijit", "js/dijit", false));
        libraries.add(new SourceLibrary("dojox", "js/dojox", false));
        libraries.add(new SourceLibrary("dgrid", "js/dgrid", false));
        libraries.add(new SourceLibrary("dojo", "js/dojo", false));
        libraries.add(new SourceLibrary("util", "js/util", false));
    }

    @Test
    public void dojoxIsChosenAsFirstLibraryForModuleInDojox()
    {
        SourceLibrary result = locator.getFirstLibraryThatIncludesFile("/website/static/js/dojox/drawing/plugins/drawing/Grid.js", libraries.toArray(new SourceLibrary[0]));

        assertEquals("dojox", result.getName());
    }

    @Test
    public void correctLibraryIsChosenForFile()
    {
        SourceLibrary result = locator.getFirstLibraryThatIncludesFile("/website/static/js/dijit/module.js", libraries.toArray(new SourceLibrary[0]));

        assertEquals("dijit", result.getName());
    }
}
