var readOnlyEditorOptions = {
  modules: { toolbar: false },
  readOnly: true,
  theme: 'snow'
};

var scrollbarWidth = 0;
var fixedModal = {};

$.ajaxSetup({
    timeout: 60*1000 //Time in milliseconds
    //Timeout = 1 minute
});

function disableAllWidget(){
	$("a").attr("onclick", "").attr("href", "#").click(function(e) {
	    e.preventDefault();
	});
	
	$("button").each(function(e){
		var widget = PrimeFaces.getWidgetById(this.id);
		if (widget){
			widget.disable();
		}
	});
}

function copyToClipboard(id){
	$(id).get(0).select();
	document.execCommand("copy");
}

function removeDataTableInlineWidth(){
	$('th.ui-resizable-column, th.column-md-btn').removeAttr('style');
}

//https://stackoverflow.com/questions/1144783/how-to-replace-all-occurrences-of-a-string-in-javascript
function escapeRegExp(str) {
    return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

function convertToCode(s){
	var output = [];
	for(var i = 0; i < s.length; i++){
		output.push(s.charCodeAt(i));
	}
	return JSON.stringify(output);
}

function highlightText(txt){
	var open = '<span class="search-highlight">';
	var close = '</span>';
	$('.highlight-target').each(function(){		
		var e = $(this);
		if (e.find('.search-highlight, .search-highlight-editor').length > 0){
			//if already highlighted, ignore
			return;
		}
		var isEditor = false;
		var textEditor = e.find("div.ql-editor");
		var oldText;
		
		if (textEditor.length > 0){
			isEditor = true;			
			e = PrimeFaces.getWidgetById(this.id).editor;
			oldText = e.getText();
		} else {
			oldText = e.html();
		}
		
		var regex = new RegExp(escapeRegExp(txt), 'gi');
		var replaceTarget = [];
		var matches = regex.exec(oldText);
		
		while(matches){
			var pair = {first: matches.index, last: regex.lastIndex, text: matches[0]};
			replaceTarget.push(pair);
			matches = regex.exec(oldText);
		}
		
		$.each(replaceTarget.reverse(), function(){
			if (isEditor){
				e.formatText(this.first, this.text.length, {'background' : 'yellow'});
			} else {
				var head = oldText.slice(0, this.first);
				var tail = oldText.slice(this.last);
				oldText = [head, open, this.text, close, tail].join('');
			}
		});
		
		if (isEditor){
			$(this).children().addClass("search-highlight-editor");
		} else {
			e.html(oldText);
		}
	});
}

function getScrollBarWidth() {
	// Taken from Joshua Bambrick's post on stackoverflow:
	// https://stackoverflow.com/a/19015262
	var $outer = $('<div>').css({
		visibility : 'hidden',
		width : 100,
		overflow : 'scroll'
	}).appendTo('body');

	var widthWithScroll = $('<div>').css({
		width : '100%'
	}).appendTo($outer).outerWidth();

	$outer.remove();
	return 100 - widthWithScroll;
}

function enableWidgetBySelector(selector, enable){
	$(selector).each(function(index, e){
		if (enable){
			PrimeFaces.getWidgetById(e.id).enable();	
		} else {
			PrimeFaces.getWidgetById(e.id).disable();
		}
	});
}

function closeModalWhenClickMask(enable) {
	if (enable) {
		$("body").on("click", '.ui-dialog-mask', function() {
			idModal = this.id;
			idModal = idModal.replace("_modal", "");
			PrimeFaces.getWidgetById(idModal).hide();
		});
	} else {
		$("body").off("click", '.ui-dialog-mask');
	}
}

function parseCssPxInt(cssPx) {
	return parseInt(cssPx.replace("px", ""));
}


function fixSmallModalSize(modalId) {
	fixedModal[modalId] = true; //use new function but ignore check
	fixModalSize(modalId);
}

function refreshModalSetting(){
	fixedModal = {};
}

function fixModalSize(modalId) {
	var firstTime = false;
	if (!fixedModal[modalId]){
		fixedModal[modalId] = true;
		firstTime = true;
	}
	
	var widget = PrimeFaces.getWidgetById(modalId);
	var windowWidth = window.innerWidth;
	var windowHeight = window.innerHeight;
	
	var estScrollbar = scrollbarWidth;

	var id = PrimeFaces.escapeClientId(modalId);
	var modal = $(id);

	var top = parseCssPxInt(modal.css("top"));
	var left = parseCssPxInt(modal.css("left"));
	
	var modalWidth = parseCssPxInt(modal.css("width"));
	var modalHeight = parseCssPxInt(modal.css("height"));
	
	
	if (firstTime 
			|| top >= (windowHeight * 0.95)
			|| left >= (windowWidth * 0.95)
			|| modalWidth + left >= windowWidth 
			|| modalHeight + top >= windowHeight){
	} else {
		return;
	}
	
	top = 0;
	left = 0;
	
	if (windowWidth > 640){
		//mid-large viewport
		top = 10;
		left = 10;
	}
	
	modal.css("top", top + "px");
	modal.css("left", left + "px");
	
	modalWidth = windowWidth - (left * 2);
	modal.css("width", modalWidth + "px");

	modalHeight = windowHeight  - (top * 2);
	modal.css("height", modalHeight + "px");

	var header = $(widget.titlebar);
	var content = $(widget.content);
	var footer = $(widget.footer);

	var headerHeight = header.height() + parseCssPxInt(header.css("marginTop")) + parseCssPxInt(header.css("marginBottom")) + parseCssPxInt(header.css("paddingTop")) + parseCssPxInt(header.css("paddingBottom"));
	var footerHeight = footer.height() + parseCssPxInt(footer.css("marginTop")) + parseCssPxInt(footer.css("marginBottom")) + parseCssPxInt(footer.css("paddingTop")) + parseCssPxInt(footer.css("paddingBottom"));
	
	var magic = 20; //padding, margin, etc.
	var contentHeight = (modalHeight - (headerHeight) - (footerHeight)) - magic + "px";
	content.css("height", contentHeight);

	var contentWidth = modalWidth - (estScrollbar * 2) + "px";
	content.css("width", contentWidth);

}

function enablePageScrollbar(enable){
	if (enable){
		$("html, body").css("overflow", "auto");
	} else {
		$("html, body").css("overflow", "hidden");
	}
}

function hideModalByWidget(w){
	var widget = PF(w);
	if (widget){
		widget.hide();
	}
}

$(document).ready(function() {
	closeModalWhenClickMask(true);
	scrollbarWidth = getScrollBarWidth();
});