describe('general tests for the admin2 page', function() {
	
  beforeEach(function(){
	  browser.get('http://localhost:8888/admin2/');
  });

  it('should be able to add translators', function() {
    element(by.id('translators')).click();
    element(by.id('edit-translators')).click();
    element(by.model('translator.value.name')).sendKeys('MrTranslator');
    element(by.model('translator.value.email')).sendKeys('test@test.org');
    element(by.css('input[value="Save"]')).click();
    
    var el = element(by.binding('t.value.name'));
    expect(el.getText()).toEqual('MrTranslator');
    });  
  
});
