/* TODO: does this assume jQuery or something? */
var md = require('markdown-it')("commonmark");
var contentIn = document.getElementById("Content");
var contentOut = document.getElementById("markdown-preview");
contentIn.oninput=function(){
    contentOut.innerHTML = md.render(contentIn.innerHTML);
};
