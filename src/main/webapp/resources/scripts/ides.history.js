//this feature is incompleted and should be developed in the near future. It is only used on modal windows in Idea List page.

var supportHistory = false;
if (window.history){
	supportHistory = true;
}

function pushHistory(x){
	var stateObj = {url: x};
	window.history.pushState(stateObj, "", "#" + x);
}

function popHistory(){	
	window.history.go(-1);
}