package com.mycompany;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import org.vertx.java.core.Handler;

import org.vertx.java.core.http.*;

import net.minidev.json.*;
import org.vertx.java.platform.Verticle;

import org.vertx.java.core.buffer.Buffer;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class RouteMatchExample extends Verticle {
   // private static final Logger m_logger = LoggerFactory.getLogger(RouteMatchExample.class);
    private static final SyslogIF syslog = Syslog.getInstance("tcp");

    String hostName = "10.0.1.170";
    int portNumber = 51515;
    Socket kkSocket = null;
    PrintWriter out = null;

    static {
         // some syslog cloud services may use this field to transmit a secret key


        syslog.getConfig().setHost("10.0.1.170");
        syslog.getConfig().setPort(51515);

    }



    public void start() {




      //  try {
       //     Socket kkSocket = new Socket(hostName, portNumber, true);
       //     out = new PrintWriter(kkSocket.getOutputStream());
       //     BufferedReader in = new BufferedReader(
       //             new InputStreamReader(kkSocket.getInputStream()));
      //  } catch (Exception e) {
      //      System.out.println("error 1: " + e.getMessage());
      //  }

        HttpServer listen = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest req) {
                parse(req);

              //  req.response().putHeader("Content-Length",String.valueOf("hello noam"));
                req.response().end("Hello World");
            }
        }).listen(8081);




    }



    private void parse(final HttpServerRequest req)
    {
        HttpServerRequest id = req.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                String contentType = req.headers().get("Content-Type");
                if ("application/xml".equals(contentType)) {
                    try {

                        VTDGen vg = new VTDGen();
                        vg.setDoc(buffer.getBytes());
                        vg.parse(false);
                        VTDNav vn = vg.getNav();
                        if (vn.matchElement("id")) {

                          //  m_logger.debug(vn.toString(vn.getText()));
                        }

                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }
                }
                if ("application/json".equals(contentType)) {
                    try {
                        String s = buffer.toString();
                        Object obj = JSONValue.parse(s);
                        JSONObject jobj=(JSONObject)obj;

                      // m_logger.debug(String.valueOf(jobj.get("id")));
                       // out.write( String.valueOf(jobj.get("id"))+"\n");
                        //out.flush();

                        syslog.debug(String.valueOf(jobj.get("id")));


                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }



                }

            }

        });

    }




}

