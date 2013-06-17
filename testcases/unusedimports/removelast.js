/*
    the comma after the WidgetsInTemplate mixin import should be removed
*/
define([
    'dijit/_WidgetsInTemplateMixin',
    'dijit/layout/ContentPane'
], function(WidgetsInTemplateMixin, ContentPane) {
    return declare([WidgetsInTemplateMixin], {
    });
});