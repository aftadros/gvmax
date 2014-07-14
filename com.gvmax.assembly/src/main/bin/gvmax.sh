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
JCP=~/.gvmax/$1:~/.gvmax:lib/*
java -cp "$JCP" com.gvmax.assembly.Main $* --pid ~/.gvmax/$1.pid

