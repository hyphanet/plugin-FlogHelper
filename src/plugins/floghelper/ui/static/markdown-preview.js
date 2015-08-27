'use strict';
var md = window.markdownit();
md.configure("commonmark");
var contentIn = $("#Content")
var contentOut = $("#markdown-preview")

function render(){
    contentOut.html(md.render(contentIn.val()));
};

// TODO: debounce
contentIn.keyup(render);

render();

