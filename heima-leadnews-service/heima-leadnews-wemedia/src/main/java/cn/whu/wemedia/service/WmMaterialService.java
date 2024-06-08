package cn.whu.wemedia.service;

import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.wemedia.dtos.WmMaterialDto;
import cn.whu.model.wemedia.pojos.WmMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {

    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 素材列表查询
     * @param dto
     * @return
     */
    public ResponseResult findList(WmMaterialDto dto);

}
