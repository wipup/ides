var supportHistory = false;
if (window.history){
	supportHistory = true;
}

function removeHash(x){
	if (!x){
		x = "";
	}
	window.location.hash = x;
}