exports.config = {
  framework: 'jasmine',
  seleniumAddress: 'http://localhost:4444/wd/hub',
  specs: ['*_spec.js'],

  onPrepare: function() {
    browser.driver.get('http://localhost:8888/admin/');
    var email = browser.driver.findElement(by.id('email'));
    
    if (email){
    	email.sendKeys('test@example.com');
    	browser.driver.findElement(by.id('isAdmin')).click();
    	browser.driver.findElement(by.id('btn-login')).click();
    }

    // Login takes some time, so wait until it's done.
    // For the test app's login, we know it's done when it redirects to
    // index.html.
    return browser.driver.wait(function() {
      return browser.driver.getCurrentUrl().then(function(url) {
        return /admin/.test(url);
      });
    }, 10000);	
  }
}
