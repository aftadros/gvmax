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
function pageInit() {
	if (user != null) {
		window.location = "../account";
	}

	$("#gvPassword").click(gvPasswordClicked);
	gvPasswordClicked();
	$("#signupForm").validate({
		rules: {
			email: {             // compound rule
				required: true
      		},
      		password: "required",
      		password2: {
      			pass2: true
      		},
      		accept: {
      			required: true
      		}
    	},
    	messages: {
    		accept: "You must accept terms and conditions."
    	},
    	submitHandler: signup
	});
	jQuery.validator.addMethod(
			"pass2",
			function(value, element) {
				if ($("#gvPassword").is(":checked"))
					return true;
				if($("#password").val() == $("#password2").val())
					return true;
				return false;
			  },
			  "Password fields don't match."
			);
}

function gvPasswordClicked() {
	if ($("#gvPassword").is(":checked")) {
		$("#p2").hide("slow");
		$("#passwordLabel").text("Google Voice Password");
	} else {
		$("#p2").show("slow");
		$("#passwordLabel").text("GVMax Password");
	}
}

function signup() {
	wait("Signing up...</br>This can take up to 2 minutes to complete.</br>Be patient :)");
	$.ajax( {
		type 	: "POST",
		url		: "../api/signup.json",
		data	: $("#signupForm").serialize(),
		dataType: "json",
		success: function(response) {
			waitOver();
			if (response.info.blacklisted) {
				showMessage("Account BLACKLISTED",
					    "Your account has been blacklisted. Please contact me at gvmax@gvmax.com",
					    function () { window.location = "../account"; }
				);
				return;
			}
			if (response.info.alreadyRegistered) {
				window.location = "../account";
				return;
			}
			if (response.info.filtersCreated) {
				showMessage("Welcome to GVMax",
						    "Your account has been created and GVMax has setup all necessary filters. Welcome to GVMax",
						    function () { window.location = "../account"; }
				);
				return;
			}
			if (response.info.gvPassword) {
				showMessage("Welcome to GVMax",
							"Your account has been created, but GVMax was unable to setup the email filters required for it to function correctly. Please follow the instructions <a href='../filters' target='_blank'>found here</a> to manually create your filters",
						    function () { window.location = "../account"; });
				return;
			}
			showMessage("Welcome to GVMax",
					"Your account has been created, you now need to manually create the email filters required for GVMax to function correctly. Please follow the instructions <a href='../filters' target='_blank'>found here</a> to manually create your filters",
				    function () { window.location = "../account"; } );
		},
		error: function(xhr) {
			waitOver();
			showMessage("Unable to signup", xhr.responseText);
		}
	});
}
