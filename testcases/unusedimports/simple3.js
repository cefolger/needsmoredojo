/**
 * c and d should be highlighted
 * a and b should not
 */
define([
    'a',
    'd'
], function(a, b, c, d) {
    var used = new a();
    used = new b();
});