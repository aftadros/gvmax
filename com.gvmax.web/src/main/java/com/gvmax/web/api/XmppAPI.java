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
package com.gvmax.web.api;

import static com.gvmax.web.api.APIUtil.internalError;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.gvmax.common.model.XMPPAction;
import com.gvmax.common.util.Enc;
import com.gvmax.web.GVMaxServiceImpl;

@Controller
public class XmppAPI {
    private GVMaxServiceImpl service;
    private String relayEncKey;

    public XmppAPI() {}

    @Autowired
    public XmppAPI(GVMaxServiceImpl service, @Value("${relay.encKey}") String relayEncKey) {
        this.service = service;
        this.relayEncKey = relayEncKey;
    }

    @RequestMapping(value = "/xmppIn", method = RequestMethod.POST)
    @Timed @ExceptionMetered
    public ModelMap xmppIn(@RequestParam(value = "reqId") String reqId, @RequestParam(value = "from") String from, @RequestParam(value = "to") String to, @RequestParam(value = "msg") String msg, HttpSession session, HttpServletResponse resp) {

        try {
            Enc enc = new Enc(relayEncKey, 128);
            //reqId = enc.decrypt(reqId);
            from = enc.decrypt(from);
            to = enc.decrypt(to);
            msg = enc.decrypt(msg);
            if (from == null || to == null || msg == null) {
                internalError(new Exception("Invalid request"), resp);
                return null;
            }
            XMPPAction action = new XMPPAction(from, to.split(","), msg);
            service.sendXMPPIn(action);
            return new ModelMap("result", "ok");
        } catch (IOException e) {
            internalError(e, resp);
        }
        return null;
    }

}
