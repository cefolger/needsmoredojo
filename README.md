Needs More Dojo
=============

#### About

This is an IntelliJ/WebStorm plugin to make working with the [dojo toolkit](http://dojotoolkit.org//) easier. 

#### Installation

Download from the JetBrains plugin repository, or alternatively: clone the repo and select 'install from disk' in the plugin menu. Use dist/needsmoredojo[version].jar

#### Usage

The plugin adds the following options under the Code menu:
- **Organize AMD Imports**: Sorts imports alphabetically, removes duplicates, and normalizes quotes
- **Add new AMD Import**: pops up a dialog. Type in the name of a dojo module OR the full path of your module. A second dialog will popup giving a list of suggestions.
The module will then be inserted in the define argument list and corresponding function parameter. For example:
    - Press Ctrl+Shift+O, 2
    - Enter 'BorderContainer', press enter
    - 'dijit/layout/BorderContainer' is the first choice presented. Press enter
    - The import will be inserted in both the define's argument list and the function's parameter list

- **Remove unused imports**: Removes imports that have been crossed out

These three items are supported only if your sources are on the same level as the dojo sources. In the next version I hope to remove that restriction.
- **Import non-dojo module**: Press Ctrl+Shift+O, 2 and type one of your source module names. The path will be resolved and the import inserted for you.
- **Go to attach point (appears under Navigate)**. Inside a module that uses _TemplatedMixin, use this option with the caret over an attach point reference.
The attach point will be looked up in the widget's template file (specified by the templateString property) and highlighted. Press Esc to remove the highlighting
- **Navigate -> Declaration** is supported for keys imported via the dojo/i18n! plugin. In the example below, the resources module has been imported via dojo/i18n!
and can be jumped to.

<pre>
    resources['website.maintoolbar.gocontact']
</pre>

The following items are added to the Refactor menu:
- **Convert class module to util module**: Converts a normal dojo class created using declare to a pattern that will
allow only one instance to be created.
- **Convert util module to class module**: The reverse of the first conversion.

A class module looks like the following: 

<pre>
define([..], function(..., {
    return declare(....); 
}); 
</pre>

A util module looks like this: 

<pre>
define([..], function(.., {
  var util = declare(...); 
  
  util.method1 = ...
  
  return util; 
}); 
</pre>

Two inspections are enabled: 
- **Check for unused imports**: Marks unused imports with strikethrough
- **Check for mismatched imports**: Check if naming between an import and its corresponding function parameter are consistent

These inspections also appear in the 'Inspections' menu, under **JavaScript -> Needs More Dojo**

#### License

Licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) license

#### Development

You will need:
- to have checked out the [IntelliJ Community Edition SDK](http://www.jetbrains.org/pages/viewpage.action?pageId=983225)
- have access to the JavaScript plugin in plugins/JavaScript
- [Mockito](https://code.google.com/p/mockito/) for running unit tests

Simply clone the repository from GitHub or fork it for your modifications.