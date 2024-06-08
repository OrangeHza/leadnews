package cn.whu.wemedia.service.impl;

import cn.whu.file.service.FileStorageService;
import cn.whu.model.common.dtos.PageResponseResult;
import cn.whu.model.common.dtos.ResponseResult;
import cn.whu.model.common.enums.AppHttpCodeEnum;
import cn.whu.model.wemedia.dtos.WmMaterialDto;
import cn.whu.model.wemedia.pojos.WmMaterial;
import cn.whu.utils.thread.WmThreadLocalUtil;
import cn.whu.wemedia.mapper.WmMaterialMapper;
import cn.whu.wemedia.service.WmMaterialService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * @param multipartFile 这个名称别瞎写，这是springMVC的普通参数传递，参数名和前端name得保持一致
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        // 1. 检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 上传图片到minIO中
        // 2.1 生成图片唯一名称
        String filename = UUID.randomUUID().toString().replace("-", "");
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));//包括.
        String filePath = null;
        try {
            filePath = fileStorageService.uploadImgFile("", filename + postfix, multipartFile.getInputStream());
            log.info("上传图片到minIO, 上传成功后的filePath:{}", filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("WmMaterialServiceImpl-上传文件失败");
        }

        // 3. 保存到db中
        // 3.1 创建wmMaterial对象
        WmMaterial wmMaterial = new WmMaterial();
        // 3.2 填充数据 db里面有的，除了id都要set上
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId()); // 用户id在ThreadLocal中 拦截器拦截请求 在请求头中获取并放到ThreadLocal中的
        wmMaterial.setUrl(filePath);
        wmMaterial.setType((short) 0); // 0 图片   1 视频
        wmMaterial.setIsCollection((short) 0); // 0 未收藏  默认值0
        wmMaterial.setCreatedTime(new Date());
        // 3.3 保存到db  也就是db内新增一条记录
        save(wmMaterial);

        // 4. 返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 素材列表查询
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmMaterialDto dto) {
        // 1. 检查参数
        dto.checkParam();

        // 2. 分页查询 【跟普通查询方法类似，方法名page,参数除了lqw多了一个iPage 返回值也是iPage 而已】
        // 2.1 分页信息
        IPage iPage = new Page(dto.getPage(), dto.getSize());//页码 当前页size
        // 2.2 查询条件
        LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
        // 1) 是否查询收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1) {
            lqw.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        // 2) 当然只能查询当前用户的
        lqw.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());
        // 3) 按时间倒序
        lqw.orderByDesc(WmMaterial::getCreatedTime);
        // 2.3 分页查询
        iPage = this.page(iPage, lqw); // 查询结果也是iPage 不过会将查询到的记录存放到iPage.records属性中


        // 3. 结果返回
        // PageResponseResult子类多了3个返回参数:currentPage、size、total 也是分页查询所需要的
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) iPage.getTotal());
        responseResult.setData(iPage.getRecords()); // 按照返回值的格式返回对应信息
        return responseResult;
    }
}
