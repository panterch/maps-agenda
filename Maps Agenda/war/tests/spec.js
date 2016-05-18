describe('general tests main app', function() {
	
  beforeEach(function(){
	  browser.get('http://localhost:8888/maps/');
  });
  
  var addEvents = function(){
	  browser.get('http://localhost:8888/admin/');
	  
  };
  it('should have a title', function() {
    

    expect(browser.getTitle()).toEqual('Maps Agenda');
  });
  it('should have a language', function() {
	    var lang = element(by.binding("l.name_br"));
	    expect(lang.getText()).toEqual('DEUTSCH');
  });
  
  it('should have events when adding them', function() {
	  	addEvents();
	    var lang = element(by.binding("l.name_br"));
	    expect(lang.getText()).toEqual('DEUTSCH');
  });
  
 function addEvents(){
	 browser.get('http://localhost:8888/admin/');
//	 browser.driver.findElement(by.id('events')).click();
//	 element.(by.css('#new-event-link a')).click();
	 var today = new Date();
	 element(by.css('input[name="day"]')).sendKeys(today.getDate());
	 element(by.css('input[name="title"]')).sendKeys('E2E TestEvent');
	 element(by.css('textarea[name="desc"]')).sendKeys('This is a Test Event, please ignore');
	 element(by.css('input[name="loc"]')).sendKeys('Paradeplatz');
	 element(by.css('input[type="submit"]')).click();
	 
 }
  
});
