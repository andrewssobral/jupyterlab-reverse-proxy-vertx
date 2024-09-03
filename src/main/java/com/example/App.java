package com.example;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class App {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        WebClientOptions webClientOptions = new WebClientOptions()
                .setDefaultPort(9999)
                .setDefaultHost("localhost")
                .setKeepAlive(true);
        WebClient webClient = WebClient.create(vertx, webClientOptions);

        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultPort(9999)
                .setDefaultHost("localhost"));

        // Add body handler to parse form data
        router.route().handler(BodyHandler.create());

        // Handle all requests
        router.route().handler(ctx -> {
            String path = ctx.request().path();
            String method = ctx.request().method().name();
            
            System.out.println("Received request: " + method + " " + path);
            System.out.println("Headers: " + ctx.request().headers());

            // Forward the request
            webClient.request(ctx.request().method(), path)
                    .putHeaders(ctx.request().headers())
                    .sendBuffer(ctx.getBody(), ar -> {
                        if (ar.succeeded()) {
                            handleSuccessfulResponse(ar.result(), ctx.response());
                        } else {
                            handleFailedResponse(ar.cause(), ctx);
                        }
                    });
        });

        // WebSocket handling
        server.webSocketHandler(webSocket -> {
            String path = webSocket.path();
            System.out.println("WebSocket connection received: " + path);

            WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setPort(9999)
                .setHost("localhost")
                .setURI(path)
                .setHeaders(webSocket.headers());

            httpClient.webSocket(options, ws -> {
                if (ws.succeeded()) {
                    System.out.println("WebSocket connection established");
                    setupWebSocket(webSocket, ws.result());
                } else {
                    System.err.println("Failed to establish WebSocket connection: " + ws.cause().getMessage());
                    webSocket.reject();
                }
            });
        });

        server.requestHandler(router).listen(8081, result -> {
            if (result.succeeded()) {
                System.out.println("Reverse proxy server is running on port 8081");
            } else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }

    private static void handleSuccessfulResponse(io.vertx.ext.web.client.HttpResponse<io.vertx.core.buffer.Buffer> response, io.vertx.core.http.HttpServerResponse clientResponse) {
        System.out.println("Successfully received response");
        System.out.println("Response status code: " + response.statusCode());
        System.out.println("Response headers: " + response.headers());

        // Forward all headers except those we want to modify
        response.headers().forEach(entry -> {
            if (!entry.getKey().equalsIgnoreCase("Content-Security-Policy") &&
                !entry.getKey().equalsIgnoreCase("Set-Cookie")) {
                clientResponse.putHeader(entry.getKey(), entry.getValue());
            }
        });

        // Set a more permissive Content Security Policy
        clientResponse.putHeader("Content-Security-Policy", 
            "default-src 'self' 'unsafe-inline' 'unsafe-eval' data: blob:; " +
            "img-src 'self' data: blob:; " +
            "style-src 'self' 'unsafe-inline'; " +
            "script-src 'self' 'unsafe-inline' 'unsafe-eval';");

        // Modify the Set-Cookie header to remove the expiration
        response.headers().getAll("Set-Cookie").forEach(cookie -> {
            String modifiedCookie = cookie.replaceFirst("expires=[^;]+;", "");
            clientResponse.headers().add("Set-Cookie", modifiedCookie);
        });

        clientResponse.setStatusCode(response.statusCode());

        if (response.body() != null) {
            clientResponse.end(response.body());
        } else {
            clientResponse.end();
        }
    }

    private static void handleFailedResponse(Throwable cause, io.vertx.ext.web.RoutingContext ctx) {
        System.err.println("Failed to get response: " + cause.getMessage());
        ctx.response().setStatusCode(500).end("Internal Server Error");
    }

    private static void setupWebSocket(io.vertx.core.http.ServerWebSocket clientWs, io.vertx.core.http.WebSocket serverWs) {
        clientWs.binaryMessageHandler(serverWs::writeBinaryMessage);
        serverWs.binaryMessageHandler(clientWs::writeBinaryMessage);

        clientWs.textMessageHandler(serverWs::writeTextMessage);
        serverWs.textMessageHandler(clientWs::writeTextMessage);

        clientWs.closeHandler(v -> serverWs.close());
        serverWs.closeHandler(v -> clientWs.close());

        clientWs.exceptionHandler(e -> {
            System.err.println("Client WebSocket error: " + e.getMessage());
            serverWs.close();
        });
        serverWs.exceptionHandler(e -> {
            System.err.println("WebSocket error: " + e.getMessage());
            clientWs.close();
        });
    }
}