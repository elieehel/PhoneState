
var exec = require('cordova/exec'),
    cordova = require('cordova');

var PhoneState = function() {
	console.log("PHONESTATE BEING INITIATED");
	this.channels = {
		watchingnetwork: cordova.addWindowEventHandler("watchingnetwork")
	};
	for (var key in this.channels) {
		this.channels[key].onHasSubscribersChange = PhoneState.subChange;
	}
};


PhoneState.subChange = function() {
	console.log("Trying to start listener");
	exec(phonestate.change, phonestate.error, "PhoneState", "start", ["asd"]);
};

PhoneState.prototype.forceStart = function() {
	console.log("Trying to start listener");
	exec(phonestate.change, phonestate.error, "PhoneState", "start", ["asd"]);
};

PhoneState.prototype.error = function(e) {
	//console.log("ERROR IN PHONESTATE");
	//console.log(e);
};

PhoneState.prototype.change = function(c) {
	//console.log("CHANGE IN PHONESTATE");
	//console.log(c);
	phonestate.state = c;
	cordova.fireWindowEvent("watchingnetwork", c);
};


/*window.echo = function(str, callback) {
        exec(callback, function(err) {
            callback('Nothing to echo. '+err);
        }, "PhoneState", "echo", [str]);
    };*/

var phonestate = new PhoneState();
phonestate.pluginReload = function() {
	exec(function() {console.log("RELOADED");}, function(err) {console.log("reload error"); console.log(err);}, "PhoneState", "resetplugin", ["asd"]);
};

phonestate.iOSState = function() {
	exec(phonestate.change, phonestate.error, "PhoneState", "start", ["asd"]);
};

phonestate.getNumber = function(func) {
	exec(func, function(err) {console.log("error GETTING THAT NUBMER"); console.log(err);}, "PhoneState", "getnumber", ["asd"]);	
};

phonestate.splashReset = function() {
	exec(function() {console.log("HIDDEN");}, function(err) {console.log("reload error"); console.log(err);}, "PhoneState", "hide", ["asd"]);
};
module.exports = phonestate;
