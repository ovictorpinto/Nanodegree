/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.github.ovictorpinto.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

@Api(
        name = "jokeApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.ovictorpinto.github.com",
                ownerName = "backend.ovictorpinto.github.com",
                packagePath = ""))
public class JokeEndpoint {

    @ApiMethod(name = "generate")
    public Joke generate() {
        Joke joke = new Joke();
        joke.setDescription("O povo brasileiro é um grande palhaço");
        return joke;
    }

}
