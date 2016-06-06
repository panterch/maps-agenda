describe('general tests for the admin2 page', function() {

	beforeEach(function() {
		browser.get('http://localhost:8888/admin2/');
		browser.driver.manage().window().maximize();
	});

	it('should be able to add translators', function() {
		element(by.id('translators')).click();
		element(by.id('edit-translators')).click();
		element(by.model('translator.value.name')).sendKeys('MrTranslator');
		element(by.model('translator.value.email')).sendKeys('test@test.org');
		element(by.css('select option[value="0"]')).click();
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-translators')).click();

		var el = element.all(by.repeater('t in translators')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'MrTranslator';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to edit translators', function() {
		element(by.id('translators')).click();

		//find the added entry from the previous testcase and click it to edit it
		element.all(by.repeater('t in translators')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'MrTranslator';
							});
				}).first().click();

		element(by.model('translator.value.name')).clear();
		element(by.model('translator.value.name')).sendKeys(
				'MrChangedTranslator');
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-translators')).click();

		var el = element.all(by.repeater('t in translators')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'MrChangedTranslator';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to delete translators', function() {
		element(by.id('translators')).click();

		//find the edited entry from the previous testcase and delete it
		var delElement = element.all(by.repeater('t in translators')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'MrChangedTranslator';
							});
				}).first();
		delElement.$$('td').last().click();
		element(by.id('save-translators')).click();

		var el = element.all(by.repeater('t in translators')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'MrChangedTranslator';
							});
				});
		expect(el.count()).toEqual(0);
	});

	it('should be able to add new language', function() {
		element(by.id('languages')).click();
		element(by.id('edit-languages')).click();
		element(by.model('language.value.code')).sendKeys('xx');
		element(by.model('language.value.name')).sendKeys('Testlanguage');
		element(by.model('language.value.germanName')).sendKeys('Testsprache');
		element(by.model('language.value.days[0]')).sendKeys('Mo');
		element(by.model('language.value.days[1]')).sendKeys('Di');
		element(by.model('language.value.days[2]')).sendKeys('Mi');
		element(by.model('language.value.days[3]')).sendKeys('Do');
		element(by.model('language.value.days[4]')).sendKeys('Fr');
		element(by.model('language.value.days[5]')).sendKeys('Sa');
		element(by.model('language.value.days[6]')).sendKeys('So');
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-languages')).click();

		var el = element.all(by.repeater('l in languages')).filter(
				function(element, index) {
					return element.$$('td').get(0).getText().then(
							function(text) {
								return text === 'xx';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to edit languages', function() {
		element(by.id('languages')).click();

		//find the added entry from the previous testcase and click it to edit it
		element.all(by.repeater('l in languages')).filter(
				function(element, index) {
					return element.$$('td').get(0).getText().then(
							function(text) {
								return text === 'xx';
							});
				}).first().click();

		element(by.model('language.value.name')).clear();
		element(by.model('language.value.name')).sendKeys('Testlanguage2');
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-languages')).click();

		var el = element.all(by.repeater('l in languages')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'Testlanguage2';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to delete languages', function() {
		element(by.id('languages')).click();

		//find the edited entry from the previous testcase and delete it
		var delElement = element.all(by.repeater('l in languages')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'Testlanguage2';
							});
				}).first();
		delElement.$$('td').last().click();
		element(by.id('save-languages')).click();

		var el = element.all(by.repeater('l in languages')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'Testlanguage2';
							});
				});
		expect(el.count()).toEqual(0);
	});

	it('should be able to add phrases', function() {
		element(by.id('phrases')).click();
		element(by.id('edit-phrases')).click();
		element(by.model('de_phrase.value.key')).sendKeys('testkey');
		element(by.model('de_phrase.value.group')).sendKeys('testgroup');
		element(by.model('de_phrase.value.phrase')).sendKeys('testphrase');
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-phrases')).click();

		var el = element.all(by.repeater('p in de_phrases')).filter(
				function(element, index) {
					return element.$$('td').get(0).getText().then(
							function(text) {
								return text === 'testkey';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to edit phrases', function() {
		element(by.id('phrases')).click();

		//find the added entry from the previous testcase and click it to edit it
		element.all(by.repeater('p in de_phrases')).filter(
				function(element, index) {
					return element.$$('td').get(0).getText().then(
							function(text) {
								return text === 'testkey';
							});
				}).first().click();

		element(by.model('de_phrase.value.phrase')).clear();
		element(by.model('de_phrase.value.phrase')).sendKeys('Modified Testphrase');
		element(by.css('input[value="Save"]')).click();
		element(by.id('save-phrases')).click();

		var el = element.all(by.repeater('p in de_phrases')).filter(
				function(element, index) {
					return element.$$('td').get(2).getText().then(
							function(text) {
								return text === 'Modified Testphrase';
							});
				});
		expect(el.count()).toEqual(1);
	});

	it('should be able to delete phrases', function() {
		element(by.id('phrases')).click();

		//find the edited entry from the previous testcase and delete it
		var delElement = element.all(by.repeater('p in de_phrases')).filter(
				function(element, index) {
					return element.$$('td').get(2).getText().then(
							function(text) {
								return text === 'Modified Testphrase';
							});
				}).first();
		delElement.$$('td').last().click();
		element(by.id('save-phrases')).click();

		var el = element.all(by.repeater('p in de_phrases')).filter(
				function(element, index) {
					return element.$$('td').get(1).getText().then(
							function(text) {
								return text === 'Modified Testphrase';
							});
				});
		expect(el.count()).toEqual(0);
	});

});
