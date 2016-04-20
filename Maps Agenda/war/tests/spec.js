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
  
  
});
