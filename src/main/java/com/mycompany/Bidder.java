package com.mycompany;


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

import java.util.Date;
import java.util.UUID;


public class Bidder extends Verticle {

    private static final SyslogIF syslog = Syslog.getInstance("tcp");
    private static final String NO_BID="{\n" +
            "  \n" +
            "  \"string\": \"no bid\"\n" +
            "}";
        static {



        syslog.getConfig().setHost("10.0.2.35");
        syslog.getConfig().setPort(51515);

    }



    public void start() {

        HttpServer listen = vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            private HttpServerRequest req;

            public void handle(HttpServerRequest req) {
                parse(req);
                req.response().setStatusCode(202);
                req.response().end(NO_BID);
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


                        }

                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }
                }
                if ("application/json".equals(contentType)) {
                    try {
                        StringBuilder sb =new StringBuilder();
                        String uuid = "\"uuid\":\""+ UUID.randomUUID().toString()+"\"";
                        String date = "\"date\":\""+ new Date().toString()+"\"";
                        sb.append("{").append(uuid).append(",").append(date).append(",").append(buffer.toString().substring(1));
                        syslog.debug(sb.toString().replace("\n","") );
                        /*

                        String s = buffer.toString();
                        Object obj = JSONValue.parse(s);
                        JSONObject jobj=(JSONObject)obj;
                        jobj=(JSONObject)jobj.get("site");
                        jobj=(JSONObject)jobj.get("publisher");




                        syslog.debug(String.valueOf(jobj.get("id")));
                        */

                    } catch (Exception e) {
                        System.out.println("exception occurred ==>" + e);
                    }



                }

            }

        });

    }




}

