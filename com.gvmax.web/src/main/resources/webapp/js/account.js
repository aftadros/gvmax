/**
/#-------------------------------------------------------------------------------
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
function pageInit() {
	if (user == null) {
		window.location = "../signup";
	}
	// Button hooks
	if (user.gvPassword) {
		$("#changePasswordButton").hide();
		$("#enableGVButton").hide();
		$("#createFilterButton").hide();
	}
	$("#unregisterButton").click(unregister);
	$("#changePasswordButton").click(changePassword);
	$("#enableGVButton").click(enableGV);
	$("#createFilterButton").click(createFilter);
	$("#changeMonitorsButton").click(changeMonitors);
	$("#changeNotifiersButton").click(changeNotifiers);
	// Grab User Info

	fillData(user,stats);
}

function fillData(user,stats) {
	$("#user_email").html(user.email);
	$("#user_pin").html(user.pin);
	$("#user_pin_email").html(user.pin+"@my.gvmax.com");

	$("#stats_smsInCount").html(stats.smsInCount);
	$("#stats_vmInCount").html(stats.vmInCount);
	$("#stats_mcInCount").html(stats.mcInCount);
	$("#stats_smsOutCount").html(stats.smsOutCount);

	$("#monitorSMS").attr("checked",user.monitorSMS);
	$("#monitorVM").attr("checked",user.monitorVM);
	$("#monitorMC").attr("checked",user.monitorMC);

	$("#changeNotifiersForm").populate(user);
	$("#sendGTalk").attr("checked",user.sendGTalk);
	$("#sendProwl").attr("checked",user.sendProwl);
	$("#sendEmail").attr("checked",user.sendEmail);
	$("#sendPost").attr("checked",user.sendPost);
	$("#sendTwitter").attr("checked",user.sendTwitter);
	$("#sendSMS").attr("checked",user.sendSMS);
	$("#sendHowl").attr("checked",user.sendHowl);
	$("#sendAutoResponse").attr("checked",user.sendAutoResponse);
	$("#gTalkPassword").attr("value", user.gTalkPassword);
	$("#howlPassword").attr("value", user.howlPassword);

	var monitors = "";
	if (user.monitorSMS)
		monitors = "[SMS] ";
	if (user.monitorVM)
		monitors = monitors + "[VOICEMAIL] ";
	if (user.monitorMC)
		monitors = monitors + "[MISSED CALLS]";
	if (monitors == "")
		monitors = "NO MONITORS";
	$("#user_monitors").html(monitors);

	var notifiers = "";
	if (user.sendGTalk)
		notifiers += "[GTALK] ";
	if (user.sendProwl)
		notifiers += "[PROWL] ";
	if (user.sendEmail)
		notifiers += "[EMAIL] ";
	if (user.sendPost)
		notifiers += "[POST] ";
	if (user.sendTwitter)
		notifiers += "[TWITTER] ";
	if (user.sendSMS)
		notifiers += "[SMS] ";
	if (user.sendHowl)
		notifiers += "[HOWL] ";
	if (user.sendAutoResponse)
		notifiers += "[AUTO RESPONSE] ";
	$("#user_notifiers").html(notifiers);

}

function enableGV() {
	$("#cancelEnableGVButton").click(function(event) {
		event.preventDefault();
		$("#enableGVDialog").dialog("close");
	});
	$("#enableGVForm").validate({
		rules: {
			password: "required"
		},
		messages: {
			password : ""
		},
		submitHandler: doEnableGV
	});
	$("#enableGVDialog").dialog({modal:true, autoOpen:true,minWidth:400});
}

function doEnableGV() {
	wait("Enabling Google Voice");
	$.ajax( {
		type 	: "POST",
		url		: "../api/enableGV.json",
		data	: $("#enableGVForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			window.location = "../account";
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to enable Google Voice",xhr.responseText)
		}
	});
}

function createFilter() {
	$("#cancelCreateFilterButton").click(function(event) {
		event.preventDefault();
		$("#createFilterDialog").dialog("close");
	});
	$("#createFilterForm").validate({
		rules: {
			password: "required"
		},
		messages: {
			password : ""
		},
		submitHandler: doCreateFilter
	});
	$("#createFilterDialog").dialog({modal:true, autoOpen:true,minWidth:400});
}

function doCreateFilter() {
	wait("Creating Filter...</br>This can take up to 2 minutes to complete.</br>Be patient :)</br>");
	$.ajax( {
		type 	: "POST",
		url		: "../api/createFilter.json",
		data	: $("#createFilterForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			$("#createFilterDialog").dialog("close");
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to create filters",xhr.responseText);
		}
	});
}

function unregister() {
	$("#messageBox-msg").text("This action will delete your account.");
	$("#messageBox").dialog({
		resizable: false,
		dialogClass: 'alert',
		modal: true,
		title: "Are you sure",
		buttons: {
			Cancel: function() {
				$(this).dialog('close');
			},
			Ok: function() {
				$(this).dialog('close');
				wait("Unregistering");
				$.ajax( {
					type 	: "POST",
					url		: "../api/unregister.json",
					dataType: "json",
					success: function(response) {
						waitOver();
						window.location = "../signup";
					},
					error: function(xhr) {
						waitOver();
						showMessage("Unable to unregister",xhr.responseText);
					}
				});
			}
		}
	});
}

function changePassword() {
	$("#cancelChangePasswordButton").click(function(event) {
		event.preventDefault();
		$("#changePasswordDialog").dialog("close");
	});
	$("#changePasswordForm").validate({
		rules: {
			old_password: "required",
			new_password: "required",
			new_password_2: {
				equalTo : "#new_password"
			}
		},
		messages: {
			old_password : "",
			new_password: "",
		},
		submitHandler: doChangePassword
	});
	$("#changePasswordDialog").dialog({modal:true, autoOpen:true,minWidth:400});
}

function doChangePassword() {
	wait("Changing password");
	$.ajax( {
		type 	: "POST",
		url		: "../api/changePassword.json",
		data	: $("#changePasswordForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			$("#changePasswordDialog").dialog("close");
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to change password",xhr.responseText);
		}
	});
}

function changeMonitors() {
	$("#cancelChangeMonitorsButton").click(function(event) {
		event.preventDefault();
		$("#changeMonitorsDialog").dialog("close");
	});
	$("#changeMonitorsForm").validate({
		submitHandler: doChangeMonitors
	});
	$("#changeMonitorsDialog").dialog({modal:true, autoOpen:true});
}

function doChangeMonitors(form) {
	wait("Updating Monitors");
	$.ajax( {
		type 	: "POST",
		url		: "../api/monitors.json",
		data	: $("#changeMonitorsForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			window.location = "../account";
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to change monitors",xhr.responseText);
		}
	});
	return false;
}

function changeNotifiers() {
	$("#cancelChangeNotifiersButton").click(function(event) {
		event.preventDefault();
		$("#changeNotifiersDialog").dialog("close");
	});
	$("#changeNotifiersForm").validate({
		submitHandler: doChangeNotifiers
	});
	$("#changeNotifiersDialog").dialog({modal:true, autoOpen:true,width:800, height:570});
}

function doChangeNotifiers() {
	wait("Updating Notifiers");
	$.ajax( {
		type 	: "POST",
		url		: "../api/notifiers.json",
		data	: $("#changeNotifiersForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			window.location = "../account";
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to change notifiers",xhr.responseText);
		}
	});
	return false;
}
