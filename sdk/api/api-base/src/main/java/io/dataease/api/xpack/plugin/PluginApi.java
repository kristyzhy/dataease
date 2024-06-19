package io.dataease.api.xpack.plugin;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.plugin.dto.PluginEditor;
import io.dataease.api.xpack.plugin.vo.PluginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "系统设置:插件管理")
@ApiSupport(order = 2)
public interface PluginApi {

    @GetMapping("/query")
    List<PluginVO> query();

    @PostMapping("/install")
    void install(@RequestPart(value = "file") MultipartFile file);

    @PostMapping("/uninstall/{id}")
    void uninstall(@PathVariable("id") String id);

    void update(@RequestPart("request") PluginEditor request, @RequestPart(value = "file") MultipartFile file);

}
