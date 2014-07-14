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
function loginInit() {
	$("#loginDialog").dialog({modal:true, autoOpen:false, minWidth: 350 });
	$("#forgotPasswordDialog").dialog({modal:true, autoOpen:false});
	if (user != null) {
		$('#loginButton').html("Logout");
		$("#loginButton").click(logout);
	} else {
		$('#loginButton').html("Login");
		$("#loginButton").click(function() {
			$("#loginDialog").dialog("open");
		});
		$("#loginForm").validate({
			rules: {
				email: {
					required: true
				},
				password: "required"
			},
			messages: {
				email : "",
				password: ""
			},
			submitHandler: login
		});
		$("#loginCancelButton").click(function(event) {
			event.preventDefault();
			$("#loginDialog").dialog("close");
		});

		$("#forgotPasswordDialog").dialog({modal:true, autoOpen:false});
		$('a#forgotPasswordLink').click(function(event){
			event.preventDefault();
			$("#forgotPasswordDialog").dialog("open");
		})
		$('#forgotPasswordCancelButton').click(function(){
			$('#forgotPasswordDialog').dialog("close");
		})
		$("#forgotPasswordForm").validate({
			submitHandler: forgotPassword
		});

	}
	$('#loginButton').show();
}

function login() {
	wait("Logging In...");
	$.ajax( {
			type     : "POST",
			url      : wroot+"api/login.json",
			data  	 : $("#loginForm").serialize(),
			dataType : "json",
			success:
				function(response) {
					waitOver();
					window.location = wroot+"account";
				},
			error:
				function(xhr) {
					waitOver();
					showMessage("Unable to login",xhr.responseText);
				}
	});
	return false;
}

function logout() {
	$.ajax( {
		type 	: "GET",
		url		: wroot+"api/logout",
	}).done(function() {
		location.reload();
	});
}

function forgotPassword(form) {
	wait("Processing....","Please wait.....");
	$.ajax( {
		type			: "POST",
		url				: wroot+"api/forgotPassword.json",
		data 			: $("#forgotPasswordForm").serialize(),
		dataType		: "json",
		success			:
			function (response) {
				waitOver();
				showMessage("Password retrieval","An email has been sent to your account with details.");
				$("#forgotPasswordDialog").dialog("close");
			},
		error			:
			function(xhr) {
				waitOver();
				showMessage("Error",xhr.responseText);
			}
	});
	return false;
}
