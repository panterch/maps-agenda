exports.config = {
  framework: 'jasmine',
  //sauceUser: "P1nkSheep",
  //sauceKey: "da965573-6cb5-495c-8d00-49d15ef65866",
  seleniumAddress: 'http://localhost:4444/wd/hub',
  specs: ['*_spec.js'],
  plugins : [{
      path: '/usr/local/lib/node_modules/protractor-istanbul-plugin',
      outputPath: '/home/oli/studium/Semesterarbeit/maps-agenda/Maps Agenda/war/tests',
      logAssertions: true,
      failAssertions: true
    }],

  onPrepare: function() {
    browser.driver.get('http://localhost:8888/admin2/');
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
        return /admin2/.test(url);
      });
    }, 10000);	
  }
}
