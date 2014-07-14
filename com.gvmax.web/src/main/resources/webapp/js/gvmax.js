/**
#-------------------------------------------------------------------------------
# Copyright (c) 2013 Hani Naguib.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the GNU Public License v3.0
# which accompanies this distribution, and is available at
# http://www.gnu.org/licenses/gpl.html
#
# Contributors:
#     Hani Naguib - initial API and implementation
#-------------------------------------------------------------------------------
**/
var user,stats;
var wroot='';
for( var i=0; i<document.location.pathname.split('/').length-2; i++)
	wroot += '../';

$(document).ready(function(){
	gvmaxInit();
});

function gvmaxInit() {
	// Hide error
	$('#serverDown').hide();
	$("#progressbar").progressbar({value:100});

	// Get current user
	$.getJSON(wroot+"api/user.json", function (response) {
		if (response.hasOwnProperty('user')) {
			// Logged in
			user = response.user;
			stats = response.stats;
			$('#accountLink').html("My Account");
			$('#accountLink').attr("href", wroot+"account");
			loginInit();
		} else {
			user = null;
			// Not logged in
			$('#accountLink').html("Sign up");
			$('#accountLink').attr("href", wroot+"signup");
			loginInit();
		}
	}).error( function(error) {
		$('#serverDown').show();
	}).complete(function () {
		loginInit();
		if (typeof pageInit == "function") {
			pageInit();
		}
	});
}

// ----------------------------
// GENERAL DIALOGS
// ----------------------------

function wait(message) {
	$("#waitBox-msg").html(message);
	$("#waitBox").dialog({
		closeOnEscape: false,
		modal: true,
		title: "Please wait....",
		open: function(event, ui) { $(".ui-dialog-titlebar-close").hide(); }
	});
	$("#waitBox").dialog("open");
}

function waitOver() {
	$("#waitBox").dialog("close");
}

function showMessage(dialogTitle,message,callback) {
	$("#messageBox-msg").html(message);
	$("#messageBox").dialog({
		resizable: false,
		dialogClass: 'alert',
		modal: true,
		title: dialogTitle,
		buttons: {
			Ok: function() {
				$(this).dialog('close');
				if (typeof callback == "function")
					callback();
			}
		}
	});
	$("#messageBox").dialog("open");
}
