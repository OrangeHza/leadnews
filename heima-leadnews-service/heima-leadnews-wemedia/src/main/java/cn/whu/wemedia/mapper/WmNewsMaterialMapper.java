package cn.whu.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.whu.model.wemedia.pojos.WmNewsMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {
     /** MP不支持这种批量保存，所以需要自己写sql了
      * 某个文章发布时存储他的所有图片关联关系：1个文章id  ----   多个图片id
      * @param materialIds
      * @param newsId
      * @param type
      */
     void saveRelations(@Param("materialIds") List<Integer> materialIds,@Param("newsId") Integer newsId, @Param("type")Short type);
}