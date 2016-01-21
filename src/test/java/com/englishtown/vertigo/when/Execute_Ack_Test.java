package com.englishtown.vertigo.when;

import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertigo.when.impl.DefaultWhenVertigoFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import net.kuujo.vertigo.builder.NetworkBuilder;
import net.kuujo.vertigo.io.VertigoMessage;
import net.kuujo.vertigo.network.Network;

import javax.inject.Inject;

public class Execute_Ack_Test extends VertigoTestBase {

    static When when = WhenFactory.createSync();
    static WhenVertigoFactory whenVertigoFactory = new DefaultWhenVertigoFactory(when);

    @Override
    protected Network createNetwork() {
        NetworkBuilder builder = Network.builder();
        builder
                .connect("A").identifier(StartComponent.class.getName()).port("out")
                .to("B").identifier(TargetComponent.class.getName()).port("in");
        return builder.build();
    }

    public static class StartComponent extends StartComponentBase {

        public StartComponent() {
            super(whenVertigoFactory);
        }

        @Override
        protected void testStart(Handler<AsyncResult<Void>> completeHandler) {
            when().resolve(VOID)
                    .then(r -> whenComponent()
                            .output()
                            .port("out")
                            .send("Word"))
                    .then(onSuccess(completeHandler), onReject(completeHandler));
        }

    }

    public static class TargetComponent extends WhenComponentBase implements Handler<VertigoMessage<Object>> {
        @Inject
        public TargetComponent() {
            super(whenVertigoFactory);
        }

        @Override
        public void start() throws Exception {
            super.start();
            component().input().port("in").handler(this);
        }

        @Override
        public void handle(VertigoMessage<Object> event) {

            when()
                    .resolve(event)
                    .then(onSuccess(event), onReject(event));

        }

    }

}