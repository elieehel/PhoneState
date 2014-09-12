
var exec = require('cordova/exec'),
    cordova = require('cordova');

var PhoneState = function() {
	this.channels = {
		watchingnetwork: cordova.addWindowEventHandler("watchingnetwork")
	};
	for (var key in this.channels) {
		this.channels[key].onHasSubscribersChange = PhoneState.subChange;
	}
};


PhoneState.subChange = function() {
	exec(phonestate.change, phonestate.error, "PhoneState", "start", ["asd"]);
};

PhoneState.prototype.error = function(e) {
};

PhoneState.prototype.change = function(c) {
	phonestate.state = c;
	cordova.fireWindowEvent("watchingnetwork", c);
};

/*window.echo = function(str, callback) {
        exec(callback, function(err) {
            callback('Nothing to echo. '+err);
        }, "PhoneState", "echo", [str]);
    };*/

var phonestate = new PhoneState();
module.exports = phonestate;