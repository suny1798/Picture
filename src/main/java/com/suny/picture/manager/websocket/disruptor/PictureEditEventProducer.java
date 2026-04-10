package com.suny.picture.manager.websocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.suny.picture.manager.websocket.model.PictureEditRequestMessage;
import com.suny.picture.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    /**
     * 发布图片编辑事件的方法
     *
     * @param pictureEditRequestMessage 图片编辑请求消息
     * @param session                   WebSocket会话
     * @param user                      用户信息
     * @param pictureId                 图片ID
     */
    public void publishEvent(PictureEditRequestMessage pictureEditRequestMessage, WebSocketSession session, User user, Long pictureId) {
        // 获取Disruptor的环形缓冲区
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 获取可以生成的位置
        long next = ringBuffer.next();
        // 获取该位置的事件对象
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);

        // 设置事件的相关属性
        pictureEditEvent.setSession(session);  // 设置WebSocket会话
        pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);  // 设置编辑请求消息
        pictureEditEvent.setUser(user);  // 设置用户信息
        pictureEditEvent.setPictureId(pictureId);  // 设置图片ID
        // 发布事件，使其可以被消费者处理
        ringBuffer.publish(next);
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void close() {
        pictureEditEventDisruptor.shutdown();
    }
}
