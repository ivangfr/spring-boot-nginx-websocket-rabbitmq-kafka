package com.ivanfranchin.newsapp.reaction;

import com.ivanfranchin.newsapp.reaction.event.ReactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.function.cloudevent.CloudEventMessageBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ReactionWebsocketController {

    private final StreamBridge streamBridge;

    @MessageMapping("/reaction")
    public void receiveReaction(ReactionEvent reactionEvent) {
        log.info("Received ReactionEvent from Websocket: {}", reactionEvent);
        streamBridge.send("reactions-out-0",
                CloudEventMessageBuilder
                        .withData(reactionEvent)
                        .setHeader("partitionKey", reactionEvent.newsId())
                        .build());
    }
}
