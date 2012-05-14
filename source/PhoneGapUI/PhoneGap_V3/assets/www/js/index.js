var preventTouchMove;
preventTouchMove = function(event) {
	event.preventDefault();
};

var settingButton;

$(document).ready(function() {
	initPage();
	initTabs();
	initImagesManagement();
	initGlobalFooter ();
});

function initPage (){
	settingButton = $('#div_global_setting img');
	settingButton.bind('tap', function(){
		ontap_settingButton($(this));
	});
};

function ontap_settingButton (sender){
	togglePlaylistContentControler();
}