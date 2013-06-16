/**
 * only b should be highlighted
 * bug: symptom of multiple defines
 */
define([
    'a',
    'b'
], function(a, b) {
    var used = new a();
});

/**
 * should not throw exception
 */
define([], function() {});

/**
 * should be highlighted
 */
define([
    'unused'
], function(unused) {
});

/**
 * should not be highlighted
 */
define([
    'used'
], function(Used) {
    var used = new Used();
});





/**
 * c and d should be highlighted
 */
define([
    'a',
    'b'
], function(a, b, c, d) {
    var used = new a();
    used = new b();
});

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