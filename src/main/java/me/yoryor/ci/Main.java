package me.yoryor.ci;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by yaoyao on 2017/6/23.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final CLI cli = cli();

    public static void main(String[] args) {
        // cmd procession
        CommandLine cmd = cli.parse(Arrays.asList(args), false);
        if (!cmd.isValid()) {
            System.out.println(usage());
            System.exit(1);
        }
        String scriptPath = cmd.getOptionValue("script");
        if (scriptPath != null) {
            // deploy verticles
            VertxOptions vertxOptions = new VertxOptions();
            vertxOptions.setMaxEventLoopExecuteTime(Integer.MAX_VALUE);
            vertxOptions.setMaxWorkerExecuteTime(Integer.MAX_VALUE);
            Vertx vertx = Vertx.vertx(vertxOptions);
            vertx.deployVerticle(new WebHookVertcle(scriptPath), ar -> {
                if (ar.succeeded()) {
                    LOG.info("Bee Bee Bee...");
                }
            });
        } else {
            System.err.println("Can not get your script file.");
        }
    }

    private static String usage() {
        StringBuilder sb = new StringBuilder();
        cli.usage(sb);
        return sb.toString();
    }

    private static CLI cli() {
        return CLI.create("bee").setSummary("Simple tool for web hook deployment.")
                .addOption(new Option()
                        .setLongName("script")
                        .setShortName("s")
                        .setDescription("The path of your deploy script."));
    }
}
