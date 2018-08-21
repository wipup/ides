var auth2 = null;
var googleUser = null;
var ignoreGoogleListener = false;
var appStart = function() {
	try{
		gapi.load('auth2', initSigninV2);
	} catch (e) {
	}
};

var initSigninV2 = function() {
	auth2 = gapi.auth2.init();
	auth2.isSignedIn.listen(signinChanged);
	if (auth2.isSignedIn.get() == true) {
		auth2.signIn();
	}
	refreshValues();
};

var refreshValues = function() {
	if (auth2) {
		googleUser = auth2.currentUser.get();
	}
}

var signinChanged = function(signedIn) {
	refreshValues();
	if (signedIn) {
		var profile = googleUser.getBasicProfile();
		var authResponse = googleUser.getAuthResponse();
		var id_token = authResponse.id_token;
		var email = null;
		if (profile){
			email = profile.getEmail();
		} 
		idesLogin([ {
			name : name_param,
			value : id_token
		},{
			name: email_param,
			value : email
		} ]);
	} else {
		//signed out
		if (!ignoreGoogleListener){
			idesLogout();
		}
		ignoreGoogleListener = false;
	}
};


function disconnectGoogle(){
	if (auth2){
		auth2.signOut(); 
		auth2.disconnect(); 
	} else {
		idesLogout();
	}
}

function signOut(){
	ignoreGoogleListener = false;
	disconnectGoogle();
}

function handleSignIn(xhr, status, args) {
	if (!args){
		disconnectGoogle();
	}
	
	var signedIn = args[callback_signin_success]; 
	if (!signedIn) {
		gapi.signin2.render('googleSignIn', {});
		ignoreGoogleListener = true;
		disconnectGoogle();
	}
}

$(document).ready(function(){
	appStart();
});