package me.yoryor.ci;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yaoyao on 2017/6/23.
 */
public class WebHookVertcle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WebHookVertcle.class);
    private String script;

    public WebHookVertcle(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.createHttpServer().requestHandler(res -> {
            res.bodyHandler(buffer -> {
                JsonObject pushEvent = buffer.toJsonObject();
                vertx.executeBlocking(future -> {
                    execShell(pushEvent);
                    future.complete();
                }, ret -> {
                    LOG.info(this.getScript() + "has been executed.");
                });

            });
        }).listen(8070);
        super.start(startFuture);
    }

    private void execShell(JsonObject pushEvent) {
        String userName = pushEvent.getString("user_name", "");
        String userEmail = pushEvent.getString("user_email", "");
        String gitHttpUrl = pushEvent.getJsonObject("repository").getString("git_http_url");
        LOG.info(Json.encodePrettily(pushEvent));
        String target = String.format("%s --commit-user %s --commit-user-email %s", this.getScript(), userName, userEmail);
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(target);
            StringBuffer output = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            LOG.info("### " + output);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }


}
