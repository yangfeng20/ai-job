package com.maple.ai.job.hunting.model.openai;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 消息
 *
 * @author maple
 * @since 2025/02/12
 */
@Data
@NoArgsConstructor
public class OpenaiMessage implements Message {

    private String role;

    private String content;

    public OpenaiMessage(MessageType messageType, String content) {
        this.role = messageType.getValue();
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public List<Media> getMedia() {
        return List.of();
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of();
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.fromValue(role);
    }
}
