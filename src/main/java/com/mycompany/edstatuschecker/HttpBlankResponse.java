
// https://stackoverflow.com/questions/63397838/create-a-dummy-java-net-http-httpresponse-without-making-an-http-request
//
// argh... why is this sort of thing so complex to actually do? Why all this boilerplate crap!?!
package com.mycompany.edstatuschecker;

import java.net.URI;
import javax.net.ssl.SSLSession;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class HttpBlankResponse<T> implements HttpResponse<T> {

    private int code = 500;
    private String message;

    public HttpBlankResponse() {
        this.message = "Opps! Error undefined";
    }

    public HttpBlankResponse(String message) {
        this.message = message;
    }

    public HttpBlankResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Version version() {

        return Version.HTTP_1_1;
    }

    @Override
    public int statusCode() {
        return this.code;
    }

    @Override
    public HttpRequest request() {
        return null;
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    private String getJsonBody() {
        return "{\n"
                + "\"status\": \"ERROR_RESPONSE\",\n"
                + "\"message\": \"" + this.message + "\"\n"
                + "}";
    }

    @Override
    public Optional<String> connectionLabel() {
        return HttpResponse.super.connectionLabel(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public T body() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public URI uri() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
