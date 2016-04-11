describe('general tests main app', function() {
  it('should have a title', function() {
    browser.get('http://localhost:8888/maps/');

    expect(browser.getTitle()).toEqual('Maps Agenda');
  });
  it('should have a language', function() {
	    browser.get('http://localhost:8888/maps/');

	    expect(browser.getTitle()).toEqual('Maps Agenda');
	  });
});