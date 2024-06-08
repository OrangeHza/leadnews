package cn.whu.wemedia.controller.v1;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.dtos.WmMaterialDto;
import cn.whu.wemedia.service.WmMaterialService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/material") // 最后的/都不要
public class WmMaterialController {

    @Resource
    private WmMaterialService wmMaterialService;

    @PostMapping("upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("list")
    public ResponseResult findList(@RequestBody WmMaterialDto dto){//@RequestBody 接受json参数，json对象属性名和Dto对象属性名一致
        return wmMaterialService.findList(dto);
    }

}
