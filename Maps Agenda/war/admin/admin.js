function getElementsByClassName(node, classname) {
    var a = [];
    var re = new RegExp('(^| )'+classname+'( |$)');
    var els = node.getElementsByTagName("*");
    for(var i=0,j=els.length; i<j; i++)
        if(re.test(els[i].className))a.push(els[i]);
    return a;
}

// Function to attach an event to an element. IE compatible.
function AttachEvent(element, type, handler) {  
  if (element.addEventListener) 
    element.addEventListener(type, handler, false);  
  else 
    element.attachEvent("on"+type, handler);  
}  

// When the page is loaded, make the menu work.
function onLoadAdmin() {
  // Attach the onClick handlers to the menu items.
  var elements = getElementsByClassName(document.body, "menu_item");
  for (var i = 0; i < elements.length; i++) {
    AttachEvent(elements[i], "click", itemClickHandler);
  }

  // Select the first menu item.
  itemClick(elements[0]);
}

// Function that translates the event to the item that fired it.
function itemClickHandler(e) {
  e = e || window.event;  
  return itemClick(e.target || e.srcElement);
}

// An item of the menu has been clicked.
function itemClick(item) {
  // Move the selector.
  var selector = document.getElementById("menu_selector");  
  selector.style.width = item.offsetWidth + 'px';
  selector.style.height = item.offsetHeight + 'px';
  selector.style.top = item.offsetTop + 'px';
  selector.style.left = item.offsetLeft + 'px';

  // Make the iframe point to the actual JSP.
  document.getElementById('content-frame').src = "/admin/" + item.id + ".jsp";

  return false;
}
