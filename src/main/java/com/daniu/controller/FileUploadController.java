package com.daniu.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class FileUploadController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/test")
    public String progressBarTest(Model model) {
        model.addAttribute("ratio", "0");
        return "test";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String filesUpload(@RequestParam String file) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(file, emitter);
        CompletableFuture.supplyAsync(() -> {
            try {
                //模拟文件处理进度
                int progress = 0;
                while (progress < 100) {
                    Thread.sleep(1000);
                    progress += 20;
                    emitter.send(SseEmitter.event().data(progress)); // 发送进度更新
                    System.out.println("已处理" + progress + "%");
                }
            } catch (Exception e) {
                System.err.println("向 SSE 发送消息时出现错误: " + e);
                emitter.completeWithError(e); // 发送错误消息并完成 SSE
            }
            return file;
        }).thenAccept(output -> completeEmitter(emitter, "文件处理完成", file));
        return file;
    }

    @GetMapping(value = "/progress/{emitterId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter getProgress(@PathVariable String emitterId) {
        if (emitterId == null) return null;
        return getProgressEmitter(emitterId);
    }

    private SseEmitter getProgressEmitter(String emitterId) {
        SseEmitter emitter = emitters.get(emitterId);
        if (emitter == null) {
            emitter = createErrorMessageEmitter(emitterId);
        }
        return emitter;
    }

    private SseEmitter createErrorMessageEmitter(String emitterId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        completeEmitter(emitter, "无效的进度查询", emitterId);
        return emitter;
    }

    private void completeEmitter(SseEmitter emitter, String message, String emitterId) {
        try {
            emitter.send(SseEmitter.event().data(message));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        } finally {
            emitters.remove(emitterId);
        }
    }

}
