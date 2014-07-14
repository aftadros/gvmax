/*******************************************************************************
 * Copyright (c) 2013 Hani Naguib.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Hani Naguib - initial API and implementation
 ******************************************************************************/
package com.gvmax.common.util;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testNull() {
        assertNull(StringUtil.split(null, 0));
    }

}
