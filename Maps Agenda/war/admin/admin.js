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
  var elements = document.getElementsByClassName("menu_item");
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
  selector.style.width = item.offsetWidth;
  selector.style.height = item.offsetHeight;
  selector.style.top = item.offsetTop;
  selector.style.left = item.offsetLeft;

  // Make the iframe point to the actual JSP.
  document.getElementById('content-frame').src = "/admin/" + item.id + ".jsp";

  return false;
}
