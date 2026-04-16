package com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.admin;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.forsoftwaredevelopers.audio_stream_api.application.command.DeleteVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ListPendingVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.MarkAsBlackListCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PlayVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ReceiveVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.RejectVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.ResumeVoiceMessageCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.service.MarkAsBlackListService;
import com.forsoftwaredevelopers.audio_stream_api.application.service.VoiceMessageService;
import com.forsoftwaredevelopers.audio_stream_api.application.service.VoiceMessageSummaryService;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.VoiceMessage;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.result.ResultHttpMapper;

@RestController
@RequestMapping("api/admin/messages")
public class AdminMessageController {

    private final VoiceMessageService voiceMessageService;
    private final VoiceMessageSummaryService voiceMessageSummaryService;
    private final MarkAsBlackListService markAsBlackListService;
    private final ResultHttpMapper resultHttpMapper;

    public AdminMessageController(
            VoiceMessageService voiceMessageService,
            VoiceMessageSummaryService voiceMessageSummaryService,
            MarkAsBlackListService markAsBlackListService,
            ResultHttpMapper resultHttpMapper) {
        this.voiceMessageService = voiceMessageService;
        this.voiceMessageSummaryService = voiceMessageSummaryService;
        this.markAsBlackListService = markAsBlackListService;
        this.resultHttpMapper = resultHttpMapper;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VoiceMessage>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String streamId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        var command = ListPendingVoiceMessageCommand.of(status, streamId, username, startDate, endDate, page, size);
        return ResponseEntity.ok(voiceMessageService.list(command));
    }

    @PostMapping("/play/{id}")
    public ResponseEntity<?> play(@PathVariable String id) {
        var result = voiceMessageService.play(new PlayVoiceMessageCommand(id));
        return resultHttpMapper.toResponse(result);
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable String id) {
        var result = voiceMessageService.reject(new RejectVoiceMessageCommand(id));
        return resultHttpMapper.toResponse(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        var result = voiceMessageService.delete(new DeleteVoiceMessageCommand(id));
        return resultHttpMapper.toResponse(result);
    }

    @PostMapping("/resume/{id}")
    public ResponseEntity<?> resume(@PathVariable String id) {
        var result = voiceMessageSummaryService.resume(new ResumeVoiceMessageCommand(id));
        return resultHttpMapper.toResponse(result);
    }

    @PostMapping("/blacklist/{id}")
    public ResponseEntity<?> markAsBlackList(
            @PathVariable String id,
            @RequestBody MarkAsBlackListCommand command) {
        var result = markAsBlackListService.markAsBlackList(
            new MarkAsBlackListCommand(
                id,
                command.ip(),
                command.username(),
                command.email()
            )
        );
        return resultHttpMapper.toResponse(result);
    }

    @PostMapping("/receive")
    public ResponseEntity<?> receive(@RequestBody ReceiveVoiceMessageCommand command) {
        var result = voiceMessageService.recieve(command);
        
        return resultHttpMapper.toResponse(result);
    }

}